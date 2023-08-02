package co.datainsider.bi.engine.redshift

import co.datainsider.bi.client.JdbcClient.Record
import co.datainsider.bi.client.{HikariClient, JdbcClient}
import co.datainsider.bi.engine.clickhouse.DataTable
import co.datainsider.bi.engine.mysql.MysqlConnection
import co.datainsider.bi.engine.{ClientManager, DataStream, Engine, SqlParser}
import co.datainsider.bi.repository.FileStorage
import co.datainsider.bi.repository.FileStorage.FileType.FileType
import co.datainsider.bi.util.Implicits
import co.datainsider.datacook.pipeline.ExecutorResolver
import co.datainsider.datacook.pipeline.operator.OperatorService
import co.datainsider.jobworker.repository.writer.DataWriter
import co.datainsider.schema.domain.TableSchema
import co.datainsider.schema.domain.column._
import co.datainsider.schema.repository.{DDLExecutor, DataRepository}
import com.twitter.inject.{Injector, Logging}
import com.twitter.util.Future
import datainsider.client.exception.DbExecuteError

import java.sql.{ResultSet, ResultSetMetaData}
import java.util.Properties
import scala.collection.mutable.ArrayBuffer
import scala.jdk.CollectionConverters.mapAsJavaMapConverter

/**
  * created 2023-06-27 2:13 PM
  *
  * @author tvc12 - Thien Vi
  */
class RedshiftEngine(
    clientManager: ClientManager,
    maxQueryRows: Int = 10000,
    poolSize: Int = 10,
    timeoutMs: Int = 30000
) extends Engine[RedshiftConnection]
    with Logging {

  implicit def createClient(source: RedshiftConnection, poolSize: Int = 10): JdbcClient = {
    val properties = new Properties()
    properties.putAll(source.properties.asJava)
    val driverClassName = Some("com.amazon.redshift.jdbc42.Driver")
    val client = HikariClient(
      source.jdbcUrl,
      source.username,
      source.password,
      maxPoolSize = Some(poolSize),
      properties = Some(properties),
      driverClassName = driverClassName
    )
    client
  }

  def getClient(source: RedshiftConnection): JdbcClient = {
    clientManager.get(source)(() => createClient(source, poolSize))
  }

  override def execute(source: RedshiftConnection, sql: String, doFormatValues: Boolean): Future[DataTable] =
    Future {
      getClient(source)
        .executeQuery(sql)(rs => {
          val (colNames, colTypes) = getColMetaData(rs)
          val records = ArrayBuffer[Array[Object]]()

          while (rs.next()) {
            val record = ArrayBuffer[Object]()
            colNames.foreach(colName => record += rs.getObject(colName))
            records += record.toArray
          }

          DataTable(colNames, colTypes, records.toArray)
        })
    }

  override def executeHistogramQuery(source: RedshiftConnection, histogramSql: String): Future[DataTable] = {
    execute(source, histogramSql)
  }

  override def getDDLExecutor(source: RedshiftConnection): DDLExecutor = {
    val client: JdbcClient = getClient(source)
    new RedshiftDDLExecutor(client)
  }

  override def exportToFile(
      source: RedshiftConnection,
      sql: String,
      destPath: String,
      fileType: FileType
  ): Future[String] =
    Future {
      getClient(source)
        .executeQuery(sql)(rs => FileStorage.exportToFile(toStream(rs), fileType, destPath))
    }

  override def testConnection(source: RedshiftConnection): Future[Boolean] =
    Future {
      getClient(source).testConnection(timeoutMs)
    }

  override def getSqlParser(): SqlParser = RedshiftParser

  override def detectExpressionColumn(
      source: RedshiftConnection,
      dbName: String,
      tblName: String,
      newExpr: String,
      existingExpressions: Map[String, String]
  ): Future[Column] = {
    val query = s"select $newExpr from $dbName.$tblName where false"
    getDDLExecutor(source)
      .detectColumns(query)
      .map(columns => {
        columns.headOption match {
          case Some(column) => column
          case None         => throw DbExecuteError(s"fail to detect column for expression: $newExpr")
        }
      })
  }

  override def detectAggregateExpressionColumn(
      source: RedshiftConnection,
      dbName: String,
      tblName: String,
      newExpr: String,
      existingExpressions: Map[String, String]
  ): Future[Column] = {
    val query =
      s"""
         |select $newExpr, 'dummy_col' as c
         |from $dbName.$tblName
         |where false
         |group by c
         |""".stripMargin

    getDDLExecutor(source)
      .detectColumns(query)
      .map(columns => {
        columns.headOption match {
          case Some(column) => column
          case None         => throw DbExecuteError(s"fail to detect column for aggregate expression: $newExpr")
        }
      })
  }

  override def createWriter(source: RedshiftConnection): DataWriter = ???

  override def getPreviewExecutorResolver(source: RedshiftConnection, operatorService: OperatorService)(
      injector: Injector
  ): ExecutorResolver = ???

  override def getExecutorResolver(source: RedshiftConnection, operatorService: OperatorService)(
      injector: Injector
  ): ExecutorResolver = ???

  override def getDataRepository(source: RedshiftConnection): DataRepository = ???

  override def write(source: RedshiftConnection, schema: TableSchema, records: Seq[Record]): Future[Int] =
    Implicits.async {
      val insertQuery =
        s"""
         |insert into ${schema.dbName}.${schema.name} (${schema.columns.map(_.name).mkString(", ")})
         |values (${Seq.fill(schema.columns.length)("?").mkString(", ")})
         |""".stripMargin

      records
        .map(record => RedshiftUtils.toCorrespondingRecord(schema.columns, record))
        .grouped(1000)
        .map(batch => {
          try {
            getClient(source).executeBatchUpdate(insertQuery, batch.toArray)
          } catch {
            case e: Throwable =>
              logger.error(s"insert to redshift error: ${e.getMessage}", e)
              0
          }
        })
        .sum
    }

  private def getColMetaData(rs: ResultSet): (Array[String], Array[String]) = {
    val metadata: ResultSetMetaData = rs.getMetaData
    val colCount = metadata.getColumnCount
    val colNames = ArrayBuffer[String]()
    val colTypes = ArrayBuffer[String]()

    for (i <- 1 to colCount) {
      colNames += metadata.getColumnLabel(i)
      colTypes += metadata.getColumnTypeName(i)
    }

    (colNames.toArray, colTypes.toArray)
  }

  private def toStream(rs: ResultSet): DataStream = {
    val columns: Seq[Column] = RedshiftUtils.parseColumns(rs.getMetaData)
    val stream = new Iterator[Record] {
      override def hasNext: Boolean = rs.next()

      override def next(): Record = toRecord(columns, rs)
    }

    DataStream(columns, stream)
  }

  private def toRecord(columns: Seq[Column], rs: ResultSet): Record = {
    columns
      .map(col => {
        try {
          col match {
            case c: BoolColumn      => rs.getBoolean(c.name)
            case c if isInt(c)      => rs.getInt(c.name)
            case c if isLong(c)     => rs.getLong(c.name)
            case c if isDouble(c)   => rs.getDouble(c.name)
            case c if isDate(c)     => rs.getDate(c.name)
            case c if isDateTime(c) => rs.getTimestamp(c.name)
            case c: StringColumn    => rs.getString(c.name)
            case _ @c               => rs.getString(c.name)
          }
        } catch {
          case e: Throwable => null
        }
      })
      .toArray
  }

  private def isInt(column: Column): Boolean = {
    column.isInstanceOf[Int8Column] || column.isInstanceOf[Int16Column] || column.isInstanceOf[Int32Column] ||
    column.isInstanceOf[UInt8Column] || column.isInstanceOf[UInt16Column] || column.isInstanceOf[UInt32Column]
  }

  private def isLong(column: Column): Boolean = {
    column.isInstanceOf[Int64Column] || column.isInstanceOf[UInt64Column]
  }

  private def isDouble(column: Column): Boolean = {
    column.isInstanceOf[DoubleColumn] || column.isInstanceOf[FloatColumn]
  }

  private def isDate(column: Column): Boolean = {
    column.isInstanceOf[DateColumn]
  }

  private def isDateTime(column: Column): Boolean = {
    column.isInstanceOf[DateTimeColumn] || column.isInstanceOf[DateTime64Column]
  }

}