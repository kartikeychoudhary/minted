import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';
import { ImportService } from '../../../../core/services/import.service';
import { BulkImportResponse } from '../../../../core/models/import.model';
import { ColDef, GridOptions, themeQuartz } from 'ag-grid-community';
import { StatusCellRendererComponent } from '../cell-renderers/status-cell-renderer';

@Component({
  selector: 'app-import-jobs',
  standalone: false,
  templateUrl: './import-jobs.html',
  styleUrl: './import-jobs.scss'
})
export class ImportJobs implements OnInit {
  imports: BulkImportResponse[] = [];
  loading = true;

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
    { field: 'id', headerName: 'ID', width: 80 },
    { field: 'fileName', headerName: 'File Name', flex: 1, minWidth: 200, cellClass: 'font-medium' },
    { field: 'accountName', headerName: 'Account', width: 160 },
    {
      field: 'status',
      headerName: 'Status',
      width: 140,
      cellRenderer: StatusCellRendererComponent,
      sortable: false
    },
    {
      headerName: 'Rows',
      width: 120,
      valueGetter: (params) => `${params.data.importedRows}/${params.data.totalRows}`
    },
    {
      field: 'createdAt',
      headerName: 'Created',
      width: 180,
      valueFormatter: (params) => params.value ? new Date(params.value).toLocaleString() : '-'
    },
    {
      headerName: 'Actions',
      width: 120,
      cellClass: 'flex items-center',
      cellRenderer: (params: any) => {
        return `<button class="text-primary hover:underline font-medium flex items-center gap-1 text-sm outline-none cursor-pointer border-none bg-transparent" data-action="view-details">
                  Details <i class="pi pi-chevron-right text-sm"></i>
                </button>`;
      }
    }
  ];

  defaultColDef: ColDef = {
    sortable: true,
    filter: false,
    resizable: true,
  };

  gridOptions: GridOptions = {
    pagination: true,
    paginationPageSize: 15,
    paginationPageSizeSelector: [15, 30, 50],
    domLayout: 'normal',
    rowSelection: 'single',
    animateRows: true,
    overlayNoRowsTemplate: '<span class="ag-overlay-no-rows-center">No imports found. Start a new import to see history here.</span>',
    onCellClicked: (params) => {
      const event = params.event as MouseEvent;
      const target = event.target as HTMLElement;
      if (target.closest('button[data-action="view-details"]')) {
        this.router.navigate(['/import/jobs', params.data.id]);
      }
    }
  };

  constructor(
    private importService: ImportService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadImports();
  }

  loadImports(): void {
    this.loading = true;
    this.importService.getUserImports().subscribe({
      next: (data) => {
        this.imports = data;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  navigateToWizard(): void {
    this.router.navigate(['/import']);
  }

  goBack(): void {
    this.router.navigate(['/import']);
  }
}
