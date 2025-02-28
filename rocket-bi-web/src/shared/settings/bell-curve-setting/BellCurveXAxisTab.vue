<template>
  <PanelHeader header="X Axis" target-id="x-axis-tab">
    <div class="x-axis-tab">
      <ToggleSetting
        id="x-axis-enable"
        :value="enabled"
        class="mb-3 group-config"
        :label="`${configSetting['xaxis.enabled'].label}`"
        :hint="`${configSetting['xaxis.enabled'].hint}`"
        @onChanged="handleAxisEnabled"
      />
      <div :style="axisSettingStyle">
        <!--      Prefix setting-->
        <!--      Postfix setting-->
        <div class="row-config-container">
          <InputSetting
            id="x-axis-prefix-input"
            :value="prefixText"
            class="mr-2"
            :label="`${configSetting['label.prefix.text'].label}`"
            :hint="`${configSetting['label.prefix.text'].hint}`"
            :placeholder="`${configSetting['label.prefix.text'].placeHolder}`"
            size="half"
            :maxLength="defaultSetting.prefixMaxLength"
            @onChanged="handlePrefixSaved"
          />
          <InputSetting
            id="x-axis-postfix-input"
            :value="postfixText"
            :label="`${configSetting['label.postfix.text'].label}`"
            :hint="`${configSetting['label.postfix.text'].hint}`"
            :placeholder="`${configSetting['label.postfix.text'].placeHolder}`"
            size="half"
            @onChanged="handlePostfixSaved"
            :maxLength="defaultSetting.suffixMaxLength"
          />
        </div>
        <DropdownSetting
          id="x-axis-category-font-family"
          :options="fontOptions"
          :value="categoryFont"
          class="mb-2"
          :label="`${configSetting['xaxis.label.fontFamily'].label}`"
          :hint="`${configSetting['xaxis.label.fontFamily'].hint}`"
          size="full"
          @onChanged="handleCategoryFontChanged"
        />
        <div class="row-config-container">
          <ColorSetting
            id="x-axis-category-font-color"
            :default-color="defaultSetting.categoryColor"
            :value="categoryColor"
            class="mr-2"
            size="small"
            :label="`${configSetting['xaxis.label.color'].label}`"
            :hint="`${configSetting['xaxis.label.color'].hint}`"
            @onChanged="handleCategoryColorChanged"
          />
          <DropdownSetting
            id="x-axis-category-font-size"
            :options="fontSizeOptions"
            :value="categoryFontSize"
            size="small"
            :label="`${configSetting['xaxis.label.fontSize'].label}`"
            :hint="`${configSetting['xaxis.label.fontSize'].hint}`"
            @onChanged="handleCategoryFontSizeChanged"
          />
        </div>
        <ToggleSetting
          id="x-axis-title-enable"
          :value="titleEnabled"
          class="mb-3 group-config"
          :label="`${configSetting['xaxis.title.text.enabled'].label}`"
          :hint="`${configSetting['xaxis.title.text.enabled'].hint}`"
          @onChanged="handleTitleEnabled"
        />
        <InputSetting
          :style="titleSettingStyle"
          id="x-axis-title-input"
          :label="`${configSetting['xaxis.title.text'].label}`"
          :hint="`${configSetting['xaxis.title.text'].hint}`"
          :placeholder="`${configSetting['xaxis.title.text'].placeHolder}`"
          :value="title"
          class="mb-3"
          size="full"
          @onChanged="handleTitleSaved"
        />
        <DropdownSetting
          :style="titleSettingStyle"
          id="x-axis-title-font-family"
          :options="fontOptions"
          :value="titleFont"
          class="mb-2"
          :label="`${configSetting['xaxis.title.fontFamily'].label}`"
          :hint="`${configSetting['xaxis.title.fontFamily'].hint}`"
          size="full"
          @onChanged="handleTitleFontChanged"
        />
        <div class="row-config-container" :style="titleSettingStyle">
          <ColorSetting
            id="x-axis-title-font-color"
            :default-color="defaultSetting.color"
            :value="titleColor"
            :label="`${configSetting['xaxis.title.color'].label}`"
            :hint="`${configSetting['xaxis.title.color'].hint}`"
            class="mr-2"
            size="small"
            @onChanged="handleTitleColorChanged"
          />
          <DropdownSetting
            id="x-axis-title-font-size"
            :options="fontSizeOptions"
            :label="`${configSetting['xaxis.title.fontSize'].label}`"
            :hint="`${configSetting['xaxis.title.fontSize'].hint}`"
            :value="titleFontSize"
            size="small"
            @onChanged="handleTitleFontSizeChanged"
          />
        </div>
        <div v-if="enableSettingGridLine" class="mb-3">
          <ToggleSetting
            id="x-axis-grid-enable"
            :value="gridEnabled"
            class="mb-3 group-config"
            :label="`${configSetting['xaxis.grid.enabled'].label}`"
            :hint="`${configSetting['xaxis.grid.enabled'].hint}`"
            @onChanged="handleGridEnabled"
          />
          <div :style="gridLineChildrenSettingStyle" class="row-config-container">
            <ColorSetting
              id="x-axis-grid-line-color"
              :default-color="defaultSetting.gridLineColor"
              :value="gridLineColor"
              class="mr-2"
              size="half"
              :label="`${configSetting['xaxis.grid.color'].label}`"
              :hint="`${configSetting['xaxis.grid.color'].hint}`"
              @onChanged="handleGridColorChanged"
            />
            <InputSetting
              id="x-axis-grid-line-width"
              :label="`${configSetting['xaxis.grid.width'].label}`"
              :hint="`${configSetting['xaxis.grid.width'].hint}`"
              :placeholder="`${configSetting['xaxis.grid.width'].placeHolder}`"
              :value="gridLineWidth"
              size="small"
              type="number"
              @onChanged="handleGridLineWidthChanged"
            />
          </div>
          <DropdownSetting
            id="x-axis-grid-line-dash-style"
            :options="dashOptions"
            :style="gridLineChildrenSettingStyle"
            :value="gridLineDashStyle"
            size="full"
            :label="`${configSetting['xaxis.grid.dash'].label}`"
            :hint="`${configSetting['xaxis.grid.dash'].hint}`"
            @onChanged="handleGridLineDashStyleChanged"
          />
        </div>
      </div>
      <RevertButton class="mb-3" style="text-align: right" @click="handleRevert" />
    </div>
  </PanelHeader>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-property-decorator';
import { AxisSetting, ChartOption, QuerySettingClassName, SettingKey } from '@core/common/domain';
import { QuerySetting } from '@core/common/domain/model/query/QuerySetting.ts';
import { FontFamilyOptions } from '@/shared/settings/common/options/FontFamilyOptions';
import { SecondaryFontSizeOptions } from '@/shared/settings/common/options/FontSizeOptions';
import { DashOptions } from '@/shared/settings/common/options/DashOptions';
import { ListUtils } from '@/utils';
import { enableCss } from '@/shared/settings/common/install';
import PanelHeader from '@/screens/chart-builder/setting-modal/PanelHeader.vue';

@Component({ components: { PanelHeader } })
export default class BellXAxisTab extends Vue {
  private readonly configSetting = window.chartSetting['xaxis.tab'];

  @Prop({ required: false, type: Array })
  private readonly setting!: AxisSetting[];
  @Prop({ required: false, type: Object })
  private readonly query!: QuerySetting;
  private readonly defaultSetting = {
    visible: true,
    categoryFont: ChartOption.getSecondaryFontFamily(),
    categoryColor: ChartOption.getPrimaryTextColor(),
    categoryFontSize: '11px',
    titleEnabled: true,
    titleFont: ChartOption.getSecondaryFontFamily(),
    titleColor: ChartOption.getPrimaryTextColor(),
    titleFontSize: '11px',
    title: 'Bell curve',
    gridLineColor: ChartOption.getGridLineColor(),
    gridLineDashStyle: 'Solid',
    gridLineWidth: '0',
    bellTitleEnabled: true,
    prefixMaxLength: 10,
    suffixMaxLength: 10,
    prefixText: '',
    postfixText: ''
  };

  private get enabled(): boolean {
    if (this.setting && this.setting[1]) {
      return this.setting[1].visible ?? this.defaultSetting.visible;
    }
    return this.defaultSetting.visible;
  }

  private get categoryFont(): string {
    if (this.setting && this.setting[1]) {
      return this.setting[1].labels?.style?.fontFamily ?? this.defaultSetting.categoryFont;
    }
    return this.defaultSetting.categoryFont;
  }

  private get categoryColor(): string {
    if (this.setting && this.setting[1]) {
      return this.setting[1].labels?.style?.color ?? this.defaultSetting.categoryColor;
    }
    return this.defaultSetting.categoryColor;
  }

  private get categoryFontSize(): string {
    if (this.setting && this.setting[1]) {
      return this.setting[1].labels?.style?.fontSize ?? this.defaultSetting.categoryFontSize;
    }
    return this.defaultSetting.categoryFontSize;
  }

  private get titleEnabled(): boolean {
    if (this.setting && this.setting[1]) {
      return this.setting[1].title?.enabled ?? this.defaultSetting.titleEnabled;
    }
    return this.defaultSetting.titleEnabled;
  }

  private get titleFont(): string {
    if (this.setting && this.setting[1]) {
      return this.setting[1].title?.style?.fontFamily ?? this.defaultSetting.titleFont;
    }
    return this.defaultSetting.titleFont;
  }

  private get titleColor(): string {
    if (this.setting && this.setting[1]) {
      return this.setting[1].title?.style?.color ?? this.defaultSetting.titleColor;
    }
    return this.defaultSetting.titleColor;
  }

  private get titleFontSize(): string {
    if (this.setting && this.setting[1]) {
      return this.setting[1].title?.style?.fontSize ?? this.defaultSetting.titleFontSize;
    }
    return this.defaultSetting.titleFontSize;
  }

  private get fontOptions() {
    return FontFamilyOptions;
  }

  private get fontSizeOptions() {
    return SecondaryFontSizeOptions;
  }

  private get title(): string {
    if (this.setting && this.setting[1]) {
      return this.setting[1].title?.text ?? this.defaultSetting.title;
    }
    return this.defaultSetting.title;
  }

  private get gridLineColor(): string {
    if (this.setting && this.setting[1]) {
      return this.setting[1].gridLineColor ?? this.defaultSetting.gridLineColor;
    }
    return this.defaultSetting.gridLineColor;
  }

  private get gridLineWidth(): string {
    if (this.setting && this.setting[1]) {
      return `${this.setting[1].gridLineWidth}` ?? this.defaultSetting.gridLineWidth;
    }
    return this.defaultSetting.gridLineWidth;
  }

  private get gridLineDashStyle(): string {
    if (this.setting && this.setting[1]) {
      return this.setting[1].gridLineDashStyle ?? this.defaultSetting.gridLineDashStyle;
    }
    return this.defaultSetting.gridLineDashStyle;
  }

  private get dashOptions() {
    return DashOptions;
  }

  private get widthOptions() {
    return ListUtils.generate(10, index => {
      const key = index + 1;
      return {
        displayName: key.toString(),
        id: key
      };
    });
  }

  private get gridEnabled(): boolean {
    if (this.setting && this.setting[1]) {
      return this.setting[1].gridLineWidth != 0;
    }
    return false;
  }
  private get enableSettingGridLine(): boolean {
    const isScatter: boolean = this.query.className == QuerySettingClassName.Scatter;
    const isBubble: boolean = this.query.className == QuerySettingClassName.Bubble;
    return isScatter || isBubble;
  }

  private get prefixText(): string {
    return this.setting[1]?.prefix?.text ?? this.defaultSetting.prefixText;
  }

  private get postfixText(): string {
    return this.setting[1]?.postfix?.text ?? this.defaultSetting.postfixText;
  }

  created() {
    if (!this.setting) {
      this.handleRevert();
    }
  }

  private handleAxisEnabled(enabled: boolean) {
    return this.$emit('onChanged', 'xAxis[1].visible', enabled);
  }

  private handleCategoryFontChanged(newFont: string) {
    return this.$emit('onChanged', 'xAxis[1].labels.style.fontFamily', newFont);
  }

  private handleCategoryFontSizeChanged(newFontSize: string) {
    return this.$emit('onChanged', 'xAxis[1].labels.style.fontSize', newFontSize);
  }

  private handleCategoryColorChanged(newColor: string) {
    return this.$emit('onChanged', 'xAxis[1].labels.style.color', newColor);
  }

  private handleTitleEnabled(enabled: boolean) {
    return this.$emit('onChanged', 'xAxis[1].title.enabled', enabled);
  }

  private handleTitleSaved(newText: string) {
    return this.$emit('onChanged', 'xAxis[1].title.text', newText);
  }

  private handleTitleFontChanged(newFont: string) {
    return this.$emit('onChanged', 'xAxis[1].title.style.fontFamily', newFont);
  }

  private handleTitleColorChanged(newColor: string) {
    return this.$emit('onChanged', 'xAxis[1].title.style.color', newColor);
  }

  private handleTitleFontSizeChanged(newFontSize: string) {
    return this.$emit('onChanged', 'xAxis[1].title.style.fontSize', newFontSize);
  }

  private handleRevert() {
    const settingAsMap: Map<SettingKey, any> = new Map();
    settingAsMap.set('xAxis[1].visible', this.defaultSetting.visible);
    settingAsMap.set('xAxis[1].labels.style.fontFamily', this.defaultSetting.categoryFont);
    settingAsMap.set('xAxis[1].labels.style.fontSize', this.defaultSetting.categoryFontSize);
    settingAsMap.set('xAxis[1].labels.style.color', this.defaultSetting.categoryColor);
    settingAsMap.set('xAxis[1].title.enabled', this.defaultSetting.titleEnabled);
    settingAsMap.set('xAxis[1].title.text', this.defaultSetting.title);
    settingAsMap.set('xAxis[1].title.style.fontFamily', this.defaultSetting.titleFont);
    settingAsMap.set('xAxis[1].title.style.fontSize', this.defaultSetting.titleFontSize);
    settingAsMap.set('xAxis[1].title.style.color', this.defaultSetting.titleColor);
    settingAsMap.set('xAxis[1].gridLineWidth', this.defaultSetting.gridLineWidth);
    settingAsMap.set('xAxis[1].gridLineColor', this.defaultSetting.gridLineColor);
    settingAsMap.set('xAxis[1].gridLineDashStyle', this.defaultSetting.gridLineDashStyle);
    settingAsMap.set('xAxis[1].prefix.text', this.defaultSetting.prefixText);
    settingAsMap.set('xAxis[1].postfix.text', this.defaultSetting.postfixText);
    this.$emit('onMultipleChanged', settingAsMap);
  }

  private handleGridEnabled(enabled: boolean) {
    if (enabled) {
      return this.$emit('onChanged', 'xAxis[1].gridLineWidth', 1);
    } else {
      return this.$emit('onChanged', 'xAxis[1].gridLineWidth', 0);
    }
  }

  private handleGridLineWidthChanged(newWidth: number) {
    if (this.gridEnabled) {
      return this.$emit('onChanged', 'xAxis[1].gridLineWidth', newWidth);
    }
  }

  private handleGridColorChanged(newColor: string) {
    return this.$emit('onChanged', 'xAxis[1].gridLineColor', newColor);
  }

  private handleGridLineDashStyleChanged(newDashStyle: string) {
    return this.$emit('onChanged', 'xAxis[1].gridLineDashStyle', newDashStyle);
  }

  private get gridLineChildrenSettingStyle(): CSSStyleDeclaration {
    return {
      ...enableCss(this.gridEnabled && this.enabled),
      marginBottom: '16px'
    } as CSSStyleDeclaration;
  }

  private get axisSettingStyle(): CSSStyleDeclaration {
    return {
      ...enableCss(this.enabled)
    } as CSSStyleDeclaration;
  }

  private get titleSettingStyle(): CSSStyleDeclaration {
    return {
      ...enableCss(this.enabled && this.titleEnabled)
    } as CSSStyleDeclaration;
  }

  private handlePrefixSaved(newText: string) {
    return this.$emit('onChanged', 'xAxis[1].prefix.text', newText);
  }

  private handlePostfixSaved(newText: string) {
    return this.$emit('onChanged', 'xAxis[1].postfix.text', newText);
  }
}
</script>

<style lang="scss" scoped></style>
