import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';
import { MessageService } from 'primeng/api';
import { ImportService } from '../../../../core/services/import.service';
import { AccountService } from '../../../../core/services/account.service';
import { CurrencyService } from '../../../../core/services/currency.service';
import { AccountResponse } from '../../../../core/models/account.model';
import { CsvUploadResponse, CsvRowPreview } from '../../../../core/models/import.model';
import { ColDef, GridOptions, themeQuartz } from 'ag-grid-community';
import { StatusCellRendererComponent } from '../cell-renderers/status-cell-renderer';

@Component({
  selector: 'app-import-wizard',
  standalone: false,
  templateUrl: './import-wizard.html',
  styleUrl: './import-wizard.scss'
})
export class ImportWizard implements OnInit {
  activeStep: number = 1;
  accounts: AccountResponse[] = [];
  accountOptions: { label: string; value: number }[] = [];
  selectedAccountId: number | null = null;
  selectedFile: File | null = null;
  uploading = false;
  importing = false;
  importComplete = false;
  importResult: any = null;
  skipDuplicates = true;

  uploadResponse: CsvUploadResponse | null = null;

  // AG Grid
  mintedTheme = themeQuartz.withParams({
    backgroundColor: 'var(--minted-bg-card)',
    foregroundColor: 'var(--minted-text-primary)',
    borderColor: 'var(--minted-border)',
    browserColorScheme: 'inherit',
    headerBackgroundColor: 'var(--minted-bg-card)',
    headerFontSize: 12,
    headerFontWeight: 600,
    headerTextColor: 'var(--minted-text-muted)',
    oddRowBackgroundColor: 'var(--minted-bg-card)',
    rowHoverColor: 'var(--minted-bg-hover)',
    selectedRowBackgroundColor: 'var(--minted-accent-subtle)',
    accentColor: 'var(--minted-accent)',
    fontFamily: '"Inter", -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif',
    fontSize: 14,
    rowHeight: 60,
    headerHeight: 48,
    spacing: 6,
    wrapperBorderRadius: 8,
    cellHorizontalPadding: 16,
    headerColumnBorder: false,
    headerColumnResizeHandleColor: 'transparent',
    columnBorder: false,
    rowBorder: { color: 'var(--minted-border-light)', width: 1, style: 'solid' },
  });

  columnDefs: ColDef[] = [
    { headerName: '#', field: 'rowNumber', width: 70, sortable: false },
    {
      headerName: 'Status',
      field: 'status',
      width: 120,
      cellRenderer: StatusCellRendererComponent,
      sortable: false
    },
    { headerName: 'Date', field: 'date', width: 130 },
    {
      headerName: 'Amount',
      field: 'amount',
      width: 130,
      cellClass: 'minted-sensitive',
      valueFormatter: (params) => {
        const val = parseFloat(params.value);
        return isNaN(val) ? params.value : this.currencyService.format(val);
      }
    },
    { headerName: 'Type', field: 'type', width: 100 },
    { headerName: 'Description', field: 'description', flex: 1, minWidth: 200 },
    { headerName: 'Category', field: 'categoryName', width: 160 },
    {
      headerName: 'Error',
      field: 'errorMessage',
      width: 200,
      cellStyle: { color: 'var(--minted-danger)' }
    }
  ];

  defaultColDef: ColDef = {
    sortable: true,
    filter: false,
    resizable: true,
  };

  gridOptions: GridOptions = {
    pagination: true,
    paginationPageSize: 25,
    paginationPageSizeSelector: [25, 50, 100],
    domLayout: 'normal',
    animateRows: true,
    overlayNoRowsTemplate: '<span class="ag-overlay-no-rows-center">Upload a CSV file to preview rows.</span>',
  };

  rowData: CsvRowPreview[] = [];

  constructor(
    private importService: ImportService,
    private accountService: AccountService,
    private messageService: MessageService,
    private router: Router,
    private cdr: ChangeDetectorRef,
    public currencyService: CurrencyService
  ) {}

  ngOnInit(): void {
    this.loadAccounts();
  }

  loadAccounts(): void {
    this.accountService.getAll().subscribe({
      next: (data) => {
        this.accounts = data;
        this.accountOptions = data.map(a => ({ label: a.name, value: a.id }));
        this.cdr.detectChanges();
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to load accounts'
        });
      }
    });
  }

  downloadTemplate(): void {
    this.importService.downloadTemplate().subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'minted_import_template.csv';
        a.click();
        window.URL.revokeObjectURL(url);
        this.messageService.add({
          severity: 'success',
          summary: 'Downloaded',
          detail: 'Template CSV downloaded'
        });
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to download template'
        });
      }
    });
  }

  onFileSelect(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.selectedFile = input.files[0];
      this.cdr.detectChanges();
    }
  }

  uploadFile(): void {
    if (!this.selectedFile || !this.selectedAccountId) return;

    this.uploading = true;
    this.importService.uploadCsv(this.selectedFile, this.selectedAccountId).subscribe({
      next: (response) => {
        this.uploadResponse = response;
        this.rowData = response.rows;
        this.uploading = false;
        this.messageService.add({
          severity: 'success',
          summary: 'Upload Complete',
          detail: `Parsed ${response.totalRows} rows: ${response.validRows} valid, ${response.errorRows} errors, ${response.duplicateRows} duplicates`
        });
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.uploading = false;
        this.messageService.add({
          severity: 'error',
          summary: 'Upload Failed',
          detail: err?.error?.message || 'Failed to upload and validate CSV'
        });
        this.cdr.detectChanges();
      }
    });
  }

  confirmImport(): void {
    if (!this.uploadResponse) return;

    this.importing = true;
    this.importService.confirmImport({
      importId: this.uploadResponse.importId,
      skipDuplicates: this.skipDuplicates
    }).subscribe({
      next: (result) => {
        this.importing = false;
        this.importComplete = true;
        this.importResult = result;
        this.messageService.add({
          severity: 'success',
          summary: 'Import Started',
          detail: 'Your transactions are being imported in the background'
        });
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.importing = false;
        this.messageService.add({
          severity: 'error',
          summary: 'Import Failed',
          detail: err?.error?.message || 'Failed to start import'
        });
        this.cdr.detectChanges();
      }
    });
  }

  navigateToHistory(): void {
    this.router.navigate(['/import/jobs']);
  }

  navigateToStatements(): void {
    this.router.navigate(['/statements']);
  }

  navigateToJobDetail(id: number): void {
    this.router.navigate(['/import/jobs', id]);
  }

  getSelectedAccountName(): string {
    if (!this.selectedAccountId) return '';
    const account = this.accounts.find(a => a.id === this.selectedAccountId);
    return account ? account.name : '';
  }

  getRowsToImport(): number {
    if (!this.uploadResponse) return 0;
    if (this.skipDuplicates) {
      return this.uploadResponse.validRows;
    }
    return this.uploadResponse.validRows + this.uploadResponse.duplicateRows;
  }

  formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 B';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i];
  }
}
