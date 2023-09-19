/*
 * @author: tvc12 - Thien Vi
 * @created: 5/5/21, 2:57 PM
 */

import { FormulaSuggestionModule, FunctionInfo } from '@/screens/chart-builder/config-builder/database-listing/FormulaSuggestionStore';
import { Column, TableSchema } from '@core/common/domain/model';
import { FormulaController } from '@/shared/fomula/FormulaController';
import { Log } from '@core/utils';
import { FormulaUtils } from '@/shared/fomula/FormulaUtils';

export class MeasureController implements FormulaController {
  private languageRegister: any | null = null;
  private tokensProvider: any | null = null;

  private readonly allFunctions: FunctionInfo[];
  private readonly tableSchema: TableSchema;

  constructor(allFunctions: FunctionInfo[], tableSchema: TableSchema) {
    this.allFunctions = allFunctions;
    this.tableSchema = tableSchema;
  }

  formulaName(): string {
    return 'measure-field';
  }

  getTheme(themeType: 'light' | 'dark' | 'custom'): string {
    return `formula-theme-${themeType}`;
  }

  init(monaco: any): void {
    monaco.languages.register({ id: this.formulaName() });

    this.tokensProvider = this.initTokenProvider(monaco);

    this.languageRegister = monaco.languages.registerCompletionItemProvider(this.formulaName(), {
      triggerCharacters: [',', '('],
      provideCompletionItems: () => {
        const suggestionFunctions = FormulaUtils.createSuggestKeywords(this.allFunctions);
        const suggestionFields = FormulaUtils.createSuggestionColumnData(this.tableSchema.columns, this.tableSchema.dbName, this.tableSchema.name);
        return { suggestions: suggestionFunctions.concat(suggestionFields) };
      }
    });
  }

  dispose(): void {
    this.languageRegister?.dispose();
    this.tokensProvider?.dispose();
  }

  // see more option in https://microsoft.github.io/monaco-editor/monarch.html
  private initTokenProvider(monaco: any): any {
    // register color
    return monaco.languages.setMonarchTokensProvider(this.formulaName(), {
      keywords: this.allFunctions.map(_ => _.name),
      fields: this.fieldNames(),
      operators: [
        '=',
        '>',
        '<',
        '!',
        '~',
        '?',
        ':',
        '==',
        '<=',
        '>=',
        '!=',
        '&&',
        '||',
        '++',
        '--',
        '+',
        '-',
        '*',
        '/',
        '&',
        '|',
        '^',
        '%',
        '<<',
        '>>',
        '>>>',
        '+=',
        '-=',
        '*=',
        '/=',
        '&=',
        '|=',
        '^=',
        '%=',
        '<<=',
        '>>=',
        '>>>='
      ],
      symbols: /[=><!~?:&|+\-*/^%]+/,
      tokenizer: {
        root: [
          [
            /[0-9a-z_$][\w$]*/,
            {
              cases: {
                '@keywords': 'keyword',
                '@fields': 'field'
              }
            }
          ],
          [/\[.*?]/, 'field'],

          // numbers
          [/\d*\.\d+([eE][-+]?\d+)?\b/, 'number.float'],
          [/0[xX][0-9a-fA-F]+/, 'number.hex'],
          [/\d+\b/, 'number'],

          // delimiter: after number because of .\d floats
          [/[;,.]/, 'delimiter']
        ]
      }
    });
  }

  private fieldNames(): string[] {
    return this.tableSchema.columns.map(col => col.displayName);
  }
}