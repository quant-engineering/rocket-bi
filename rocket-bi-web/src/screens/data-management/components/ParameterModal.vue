<template>
  <DiCustomModal id="param-config-modal" ref="modal" title="Config Parameter" size="md" @onClickOk="onSubmitParam">
    <DiInputComponent
      ref="displayNameInput"
      label="Parameter"
      v-model="param.displayName"
      class="mb-2"
      placeholder="Input parameter name"
      @change="displayNameError = ''"
      @enter="onDisplayNameEnter"
    >
      <template #error v-if="displayNameError">
        <div class="text-danger">{{ displayNameError }}</div>
      </template>
    </DiInputComponent>
    <div class="mt-1 mb-3">{{ description }}</div>
    <div class="mb-3">
      <div>Parameter Type</div>
      <DiDropdown :data="paramTypeOptions" v-model="param.valueType" valueProps="id" labelProps="displayName" @change="onParamTypeChanged" />
    </div>
    <DynamicInputValue :type="param.valueType" :value.sync="param.value" :list="param.list" @enter="onSubmitParam" @setList="setList" />
  </DiCustomModal>
</template>

<script lang="ts">
import { Component, Ref, Vue, Watch } from 'vue-property-decorator';
import { cloneDeep } from 'lodash';
import { createQueryParameter, DIException, ParamValueType, QueryParameter } from '@core/common/domain';
import DiCustomModal from '@/shared/components/DiCustomModal.vue';
import DiInputComponent from '@/shared/components/DiInputComponent.vue';
import DiDropdown from '@/shared/components/common/di-dropdown/DiDropdown.vue';
import { SelectOption } from '@/shared';
import DynamicInputValue from '@/screens/data-management/components/DynamicInputValue.vue';
import moment from 'moment';
import { Log } from '@core/utils';
import { DateTimeUtils, ListUtils, StringUtils } from '@/utils';

@Component({ components: { DiDropdown, DiInputComponent, DiCustomModal, DynamicInputValue } })
export default class ParameterModal extends Vue {
  private readonly paramTypeOptions: SelectOption[] = [
    {
      displayName: 'Text',
      id: ParamValueType.text
    },
    {
      displayName: 'Number',
      id: ParamValueType.number
    },
    {
      displayName: 'Date',
      id: ParamValueType.date
    },
    {
      displayName: 'List',
      id: ParamValueType.list
    }
  ];
  private param: QueryParameter = createQueryParameter();
  private callback: ((param: QueryParameter) => void) | null = null;
  private blacklistNames: Set<string> = new Set();
  @Ref()
  private readonly modal!: DiCustomModal;
  @Ref()
  private readonly displayNameInput!: DiInputComponent;

  @Ref()
  private readonly inputValue!: DynamicInputValue;
  private displayNameError = '';

  show(param: QueryParameter, onSubmitted: (param: QueryParameter) => void, blacklistNames: Set<string> = new Set()) {
    this.reset();
    this.param = cloneDeep(param);
    this.blacklistNames = blacklistNames;
    this.callback = onSubmitted;
    this.modal.show();
  }

  private reset() {
    this.param = createQueryParameter();
    this.blacklistNames = new Set();
    this.displayNameError = '';
    this.callback = null;
  }

  private get description(): string {
    return 'Wrap your parameter in single quotes to use as a value \'{{Parameter}}\' in SQL.';
  }

  private onParamTypeChanged(newType: ParamValueType) {
    this.param.valueType = newType;
    this.param.value = this.getDefaultValue(newType);
    if (newType === ParamValueType.list) {
      this.param.list = [];
    } else {
      this.param.list = null;
    }
  }

  private getDefaultValue(type: ParamValueType) {
    switch (type) {
      case ParamValueType.text:
      case ParamValueType.list:
        return ``;
      case ParamValueType.number:
        return 0;
      case ParamValueType.date:
        return DateTimeUtils.formatDateTime(moment().toDate());
    }
  }

  private onSubmitParam(event?: MouseEvent) {
    try {
      Log.debug('onSubmitParam');
      this.updateList();
      event?.preventDefault();
      this.valid(this.param);
      this.callback ? this.callback(this.param) : void 0;
      this.hide();
    } catch (ex) {
      Log.error(ex);
    }
  }

  private updateList() {
    if (this.param.list) {
      this.param.list = ListUtils.unique(this.param.list);
    }
  }

  hide() {
    this.$nextTick(() => {
      this.modal.hide();
    });
  }

  private valid(param: QueryParameter) {
    this.validDisplayName(param.displayName, this.blacklistNames);
    ///Do something valid
  }

  private validDisplayName(displayName: string, blackList: Set<string>) {
    if (StringUtils.isEmpty(displayName)) {
      this.displayNameError = 'Parameter name is required!';
      this.displayNameInput.focus();
      throw new DIException('Parameter name is required!');
    }
    if (blackList.has(displayName)) {
      this.displayNameError = 'Parameter is existed! Please choose another';
      this.displayNameInput.focus();
      throw new DIException('Parameter is existed!');
    }
    if (`${displayName}`.trim().includes(' ')) {
      this.displayNameError = 'Parameter name contains white space! Please remove white space';
      this.displayNameInput.focus();
      throw new DIException('Parameter name contains white space! Please remove white space');
    }
  }

  private onDisplayNameEnter() {
    this.inputValue.focus();
  }

  private setList(list: string[]) {
    this.param.list = list;
  }
}
</script>

<style lang="scss" scoped></style>
