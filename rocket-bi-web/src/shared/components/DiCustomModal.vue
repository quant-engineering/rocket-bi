<template>
  <BModal
    :id="id"
    ref="modal"
    :cancelTitle="cancelTitle"
    :okTitle="okTitle"
    :size="size"
    :hide-header-close="hideHeaderClose"
    centered
    :scrollable="scrollable"
    :no-close-on-esc="false"
    :ok-disabled="okDisabled"
    :modal-class="modalClass"
    :dialog-class="dialogClass"
    :hide-footer="hideFooter"
    class="rounded"
    v-bind="$attrs"
    @ok="handleClickOk"
    @cancel="handleCancel"
    @hide="handleClose"
    @close="handleCancel"
    @hidden="handleHidden"
  >
    <template #modal-header="{ close }">
      <slot name="modal-header" :close="close">
        <div class="custom-header d-inline-flex w-100">
          <h6
            class="modal-title cursor-default"
            :class="{
              'modal-title-center': isCenterTitle
            }"
          >
            <slot name="header-icon"></slot>
            <div>{{ title }}</div>
            <slot name="subtitle"></slot>
          </h6>
          <div v-if="!hideHeaderClose" aria-label="Close" class="close" type="button" @click.prevent="close()">
            <BIconX class="button-x btn-icon-border" />
          </div>
        </div>
      </slot>
    </template>
    <template #modal-footer="{ ok, cancel}">
      <slot name="modal-footer" :ok="ok" :cancel="cancel">
        <div class="di-custom-modal--footer" :size="buttonSize">
          <DiButton :title="cancelTitle" id="modal-btn-cancel" class="di-custom-modal--footer--cancel" border secondary @click="cancel"></DiButton>
          <DiButton
            :title="okTitle"
            id="modal-btn-ok"
            :isDisable="isLoading"
            :is-loading="isLoading"
            class="di-custom-modal--footer--ok"
            primary
            @click="ok"
          ></DiButton>
        </div>
      </slot>
    </template>
    <template #default="{ok, cancel}">
      <slot :ok="ok" :cancel="cancel"></slot>
      <div v-show="errorMsg" class="di-custom-modal--error">{{ errorMsg }}</div>
    </template>
  </BModal>
</template>

<script lang="ts">
import { Component, Emit, Prop, Ref, Vue } from 'vue-property-decorator';
import { BModal, BvModalEvent } from 'bootstrap-vue';
import DiButton from '@/shared/components/common/DiButton.vue';
import { Log } from '@core/utils';

@Component({
  components: {
    DiButton
  }
})
export default class DiCustomModal extends Vue {
  private isLoading = false;
  private errorMsg: string | null = null;

  @Prop({ type: String, default: 'custom-modal' })
  private id!: string;

  @Prop({ required: false, type: String, default: '' })
  private title!: string;

  @Prop({ type: String, default: 'Cancel' })
  private cancelTitle!: string;

  @Prop({ type: String, default: 'Ok' })
  private okTitle!: string;

  @Prop({ type: String, default: 'lg' })
  private size!: 'sm' | 'md' | 'lg' | 'xl';

  @Prop({ type: String, required: false, default: '' })
  private readonly modalClass!: string;

  @Prop({ type: String, required: false, default: '' })
  private readonly dialogClass!: string;

  @Prop({ type: Boolean, required: false, default: false })
  private readonly hideHeaderClose!: boolean;

  @Prop({ type: Boolean, required: false, default: false })
  private readonly okDisabled!: boolean;

  @Prop({ type: Boolean, required: false, default: false })
  private readonly scrollable!: boolean;

  @Prop({ type: Boolean, required: false, default: false })
  private readonly hideFooter!: boolean;

  @Prop({ type: Boolean, required: false, default: false })
  private readonly isCenterTitle!: boolean;

  @Prop({ required: false, type: String, default: 'full' })
  private readonly buttonSize!: 'small' | 'full';

  @Ref()
  private modal!: BModal;

  show() {
    this.modal.show();
  }

  hide() {
    this.modal.hide();
  }

  @Emit('onClickOk')
  private handleClickOk(bvModalEvt: MouseEvent) {
    return bvModalEvt;
  }

  @Emit('onCancel')
  private handleCancel(bvModalEvt: MouseEvent) {
    return bvModalEvt;
  }

  @Emit('hidden')
  private handleHidden(bvModalEvt: BvModalEvent) {
    this.isLoading = false;
    this.errorMsg = null;
    return bvModalEvt;
  }

  @Emit('hide')
  handleClose(bvModalEvt: MouseEvent) {
    return bvModalEvt;
  }

  setLoading(isLoading: boolean) {
    this.isLoading = isLoading;
  }

  setError(errorMsg: string | null): void {
    this.errorMsg = errorMsg;
  }
}
</script>

<style lang="scss" scoped>
@import '~@/themes/scss/mixin.scss';

.custom-header {
  min-height: 28px;

  .modal-title {
    @include medium-text(24px, 0.2px, inherit);
  }

  .button-x {
    width: 24px;
    height: 24px;
    color: var(--text-color);
  }
}

::v-deep {
  .modal-footer > button {
    flex-basis: 0;
    flex-grow: 1;
    max-width: 100%;
    height: 42px;
  }
}
</style>

<style lang="scss">
.custom-header {
  .modal-title-center {
    text-align: center;
    width: 100%;
    justify-content: center;
  }
}
.modal-small {
  max-width: 350px !important;

  .modal-header {
    padding: 12px 24px 0 24px !important;
  }

  .modal-body {
    padding: 24px !important;
  }

  .modal-footer {
    padding: 0 24px 24px 24px !important;
  }
}

.di-custom-modal--error {
  color: var(--danger);
  font-size: 14px;
  font-weight: 400;
  line-height: 18px;
  margin-top: 8px;
  width: 100%;
  text-align: left;
  display: -webkit-box;
  line-clamp: 5;
  -webkit-line-clamp: 5;
  -webkit-box-orient: vertical;
  -moz-box-orient: vertical;
  overflow: hidden;
}

.di-custom-modal--footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  //margin-top: 24px;
  width: 100%;

  &[size='full'] {
    .di-custom-modal--footer--cancel,
    .di-custom-modal--footer--ok {
      flex: 1;
      height: 40px;
    }
  }

  &[size='small'] {
    .di-custom-modal--footer--cancel {
      min-width: 83px;
      height: 40px;
    }

    .di-custom-modal--footer--ok {
      min-width: 122px;
      height: 40px;
    }
  }

  //
  .di-button + .di-button {
    margin-left: 16px;
  }
}
</style>
