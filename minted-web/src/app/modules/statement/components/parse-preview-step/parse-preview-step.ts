import { Component, Input, Output, EventEmitter, OnInit, ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';
import { MessageService, ConfirmationService } from 'primeng/api';
import { ColDef, GridOptions, GridApi, GridReadyEvent, themeQuartz } from 'ag-grid-community';
import { StatementService } from '../../../../core/services/statement.service';
import { CategoryService } from '../../../../core/services/category.service';
import { CategoryResponse } from '../../../../core/models/category.model';
import { CreditCardStatement, ParsedTransactionRow } from '../../../../core/models/statement.model';

@Component({
  selector: 'app-parse-preview-step',
  standalone: false,
  templateUrl: './parse-preview-step.html',
  styleUrl: './parse-preview-step.scss'
})
export class ParsePreviewStep implements OnInit {
  @Input() statement!: CreditCardStatement;
  @Output() statementUpdated = new EventEmitter<CreditCardStatement>();

  rows: ParsedTransactionRow[] = [];
  categories: CategoryResponse[] = [];
  categoryNames: string[] = [];
  loading = true;
  importing = false;
  skipDuplicates = true;
  private gridApi!: GridApi;

  mintedTheme = themeQuartz.withParams({
    backgroundColor: 'var(--minted-bg-card)',
    foregroundColor: 'var(--minted-text-primary)',
    borderColor: 'var(--minted-border)',
    headerBackgroundColor: 'var(--minted-bg-card)',
    headerFontSize: 12,
    headerFontWeight: 600,
    headerTextColor: 'var(--minted-text-muted)',
    rowHoverColor: 'var(--minted-surface)',
    fontFamily: '"Inter", sans-serif',
    rowHeight: 48,
    headerHeight: 44,
  });

  colDefs: ColDef[] = [
    {
      field: 'isDuplicate',
      headerName: '',
      width: 50,
      cellRenderer: (params: any) => params.value
        ? '<span title="Possible duplicate" style="color: var(--minted-warning); font-size: 16px;">&#9888;</span>'
        : ''
    },
    { field: 'transactionDate', headerName: 'Date', width: 120, sort: 'asc' },
    { field: 'description', headerName: 'Description', flex: 1, editable: true },
    {
      field: 'amount', headerName: 'Amount', width: 120,
      cellClass: 'minted-sensitive',
      valueFormatter: (p: any) => p.value != null ? '₹' + Number(p.value).toLocaleString('en-IN', { minimumFractionDigits: 2 }) : '',
      cellStyle: (p: any) => ({ color: p.data?.type === 'EXPENSE' ? 'var(--minted-error)' : 'var(--minted-success)' })
    },
    {
      field: 'type', headerName: 'Type', width: 100, editable: true,
      cellEditor: 'agSelectCellEditor',
      cellEditorParams: { values: ['EXPENSE', 'INCOME'] }
    },
    {
      field: 'categoryName', headerName: 'Category', width: 160, editable: true,
      cellEditor: 'agSelectCellEditor',
      cellEditorParams: () => ({ values: this.categoryNames }),
      cellRenderer: (params: any) => {
        const ruleIcon = params.data?.mappedByRule
          ? '<i class="pi pi-tag" title="Category assigned by merchant mapping" style="color:var(--minted-accent);margin-right:4px;font-size:10px"></i>'
          : '';
        return `${ruleIcon}${params.value || ''}`;
      },
      onCellValueChanged: (params: any) => {
        const cat = this.categories.find(c => c.name === params.newValue);
        if (cat) {
          params.data.matchedCategoryId = cat.id;
        }
      }
    },
    { field: 'notes', headerName: 'Notes', width: 150, editable: true }
  ];

  gridOptions: GridOptions = {
    domLayout: 'autoHeight',
    animateRows: true,
    getRowStyle: (params: any) => {
      if (params.data?.isDuplicate) {
        return { background: 'var(--minted-warning-subtle)', borderLeft: '3px solid var(--minted-warning)' };
      }
      return undefined;
    },
    overlayNoRowsTemplate: '<span>No transactions parsed from this statement.</span>'
  };

  constructor(
    private statementService: StatementService,
    private categoryService: CategoryService,
    private messageService: MessageService,
    private confirmationService: ConfirmationService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadCategories();
    this.loadParsedRows();
  }

  loadCategories(): void {
    this.categoryService.getAll().subscribe({
      next: (cats) => {
        this.categories = cats;
        this.categoryNames = cats.map(c => c.name);
      }
    });
  }

  loadParsedRows(): void {
    this.loading = true;
    this.statementService.getParsedRows(this.statement.id).subscribe({
      next: (data) => {
        this.rows = data;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  onGridReady(params: GridReadyEvent): void {
    this.gridApi = params.api;
  }

  get totalCount(): number {
    return this.rows.length;
  }

  get duplicateCount(): number {
    return this.rows.filter(r => r.isDuplicate).length;
  }

  get importCount(): number {
    return this.skipDuplicates ? this.totalCount - this.duplicateCount : this.totalCount;
  }

  confirmImport(): void {
    this.confirmationService.confirm({
      message: `Import ${this.importCount} transactions${this.skipDuplicates && this.duplicateCount > 0 ? ` (skipping ${this.duplicateCount} duplicates)` : ''}?`,
      accept: () => {
        this.importing = true;
        this.statementService.confirmImport({
          statementId: this.statement.id,
          skipDuplicates: this.skipDuplicates
        }).subscribe({
          next: () => {
            this.importing = false;
            this.messageService.add({
              severity: 'success',
              summary: 'Import Complete',
              detail: `Transactions imported successfully.`
            });
            // Reload statement to get step 4
            this.statementService.getStatement(this.statement.id).subscribe({
              next: (stmt) => {
                this.statementUpdated.emit(stmt);
                this.cdr.detectChanges();
              }
            });
          },
          error: (err) => {
            this.importing = false;
            this.messageService.add({
              severity: 'error',
              summary: 'Import Failed',
              detail: err.error?.message || 'Failed to import transactions.'
            });
            this.cdr.detectChanges();
          }
        });
      }
    });
  }
}
