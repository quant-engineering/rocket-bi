package co.datainsider.datacook.pipeline.operator.persist
import co.datainsider.datacook.domain.persist.PersistentType.PersistentType
import co.datainsider.datacook.domain.persist.PersistentTypeRef
import co.datainsider.datacook.pipeline.operator.Operator.OperatorId
import com.fasterxml.jackson.module.scala.JsonScalaEnumeration
import datainsider.client.util.JsonParser

import java.util.Properties

case class PostgresPersistOperator(
    id: OperatorId,
    host: String,
    port: Int,
    username: String,
    password: String,
    // mapping with database
    catalogName: String,
    // mapping with schema name
    databaseName: String,
    tableName: String,
    @JsonScalaEnumeration(classOf[PersistentTypeRef])
    override val persistType: PersistentType,
    extraPropertiesAsJson: Option[String] = None
) extends JdbcPersistOperator {
  def jdbcUrl: String = s"jdbc:postgresql://${host}:${port}/${catalogName}"

  override def properties: Properties = {
    val properties = new Properties()
    properties.setProperty("user", username)
    properties.setProperty("password", password)
    properties.setProperty("useUnicode", "yes")
    properties.setProperty("characterEncoding", "UTF-8")
    properties.setProperty("serverTimezone", "UTC")
    val extraProperties: Properties = JsonParser.fromJson[Properties](extraPropertiesAsJson.getOrElse("{}"))
    properties.putAll(extraProperties)
    properties
  }
}
