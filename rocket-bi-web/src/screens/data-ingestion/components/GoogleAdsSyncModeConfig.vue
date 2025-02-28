<template>
  <div>
    <label>Sync Mode</label>
    <div class="input">
      <BFormRadioGroup plain id="sync-mode-radio-group" v-model="syncJob.syncMode" name="radio-sub-component">
        <BFormRadio :id="genCheckboxId('full-sync')" :value="syncMode.FullSync">Full sync</BFormRadio>
        <BFormRadio :id="genCheckboxId('incremental-sync')" :value="syncMode.IncrementalSync">Incremental sync </BFormRadio>
      </BFormRadioGroup>
    </div>
    <label class="my-1">Sync from</label>
    <DiDatePicker class="last-sync-value-date-picker" :date.sync="lastSyncValueAsDate" placement="bottom" />
    <div v-if="incrementalValueError" class="error-message px-0 mb-1">{{ incrementalValueError }}</div>
  </div>
</template>

<script lang="ts">
import { Component, Prop, PropSync, Vue, Watch } from 'vue-property-decorator';
import { GoogleAdsJob, SyncMode } from '@core/data-ingestion';
import DynamicSuggestionInput from './DynamicSuggestionInput.vue';
import DiInputComponent from '@/shared/components/DiInputComponent.vue';
import DiDatePicker from '@/shared/components/DiDatePicker.vue';
import { DateTimeUtils } from '@/utils';
import { Log } from '@core/utils';
import moment from 'moment';

@Component({
  components: { DiDatePicker, DiInputComponent, DynamicSuggestionInput }
})
export default class GoogleAdsSyncModeConfig extends Vue {
  protected readonly syncMode = SyncMode;
  protected readonly MIN_LAST_SYNC_VALUE: Date = new Date(2019, 0, 1);
  protected static readonly DEFAULT_LAST_SYNC_VALUE: string = DateTimeUtils.formatDate(new Date(2019, 0, 1));
  protected static readonly DEFAULT_COLUMN_SYNC_VALUE: string = 'segments.date';
  protected incrementalValueError = '';

  @PropSync('job')
  syncJob!: GoogleAdsJob;

  @Prop()
  isValidate!: boolean;

  mounted() {
    this.syncJob.startDate = this.syncJob.startDate || GoogleAdsSyncModeConfig.DEFAULT_LAST_SYNC_VALUE;
  }

  @Watch('syncJob.syncMode')
  onSyncModeChange() {
    Log.debug('Watch::onSyncModeChange::', this.syncJob.lastSyncedValue || GoogleAdsSyncModeConfig.DEFAULT_LAST_SYNC_VALUE);
    if (this.syncJob.syncMode === SyncMode.IncrementalSync) {
      this.syncJob.withIncrementalColumn(this.syncJob.incrementalColumn ?? GoogleAdsSyncModeConfig.DEFAULT_COLUMN_SYNC_VALUE);
    }
    // Log.debug('Watch::onSyncModeChange::', this.syncJob.incrementalColumn, this.syncJob.lastSyncedValue);
  }

  @Watch('syncJob.lastSyncedValue')
  onLastSyncValueChanged() {
    this.incrementalValueError = '';
  }

  public validSyncMode(): boolean {
    if (!this.syncJob.startDate) {
      this.incrementalValueError = 'Incremental Date is required!';
      return false;
    }
    return true;
  }

  protected get lastSyncValueAsDate(): Date {
    return this.syncJob.startDate ? moment(this.syncJob.startDate).toDate() : this.MIN_LAST_SYNC_VALUE;
  }

  protected set lastSyncValueAsDate(date: Date) {
    this.syncJob.startDate = DateTimeUtils.formatDate(date);
  }
}
</script>

<style lang="scss">
.last-sync-value-date-picker {
  .input-container {
    .input-calendar {
      padding-left: 8px !important;
    }
  }
}
</style>
