import { DataSources } from '@core/data-ingestion';
import { DataSource } from '@core/data-ingestion/domain/response/DataSource';
import { SourceId } from '@core/common/domain';

export class GoogleAdsSource implements DataSource {
  readonly className = DataSources.GoogleAdsSource;
  id: SourceId;
  orgId: string;
  displayName: string;
  refreshToken: string;
  accessToken: string;
  lastModify: number;

  constructor(id: SourceId, orgId: string, displayName: string, refreshToken: string, accessToken: string, lastModify: number) {
    this.id = id;
    this.orgId = orgId;
    this.displayName = displayName;
    this.refreshToken = refreshToken;
    this.accessToken = accessToken;
    this.lastModify = lastModify;
  }

  static fromObject(obj: any): GoogleAdsSource {
    return new GoogleAdsSource(obj.id, obj.orgId, obj.displayName, obj.refreshToken, obj.accessToken, obj.lastModify);
  }
}