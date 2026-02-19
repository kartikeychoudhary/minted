import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';
import { AdminService } from '../../../../core/services/admin.service';
import { JobExecution } from '../../../../core/models/admin.model';
import { ColDef, GridOptions, themeQuartz } from 'ag-grid-community';

@Component({
  selector: 'app-jobs-list',
  standalone: false,
  templateUrl: './jobs-list.html',
  styleUrl: './jobs-list.scss',
})
export class JobsList implements OnInit {
  jobs: JobExecution[] = [];
  loading: boolean = false;

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
    checkboxCheckedBackgroundColor: 'var(--minted-accent)',
    checkboxCheckedBorderColor: 'var(--minted-accent)',
    checkboxUncheckedBackgroundColor: 'transparent',
    checkboxUncheckedBorderColor: 'var(--minted-border)',
  });

  columnDefs: ColDef[] = [
    { field: 'id', headerName: 'ID', width: 80 },
    { field: 'jobName', headerName: 'Job Name', flex: 1, cellClass: 'font-medium' },
    {
      field: 'status',
      headerName: 'Status',
      width: 140,
      cellRenderer: (params: any) => {
        const status = params.value;
        let bgClass = 'var(--minted-bg-surface)';
        let textClass = 'var(--minted-text-secondary)';
        if (status === 'COMPLETED') { bgClass = 'var(--minted-success-subtle)'; textClass = 'var(--minted-success)'; }
        if (status === 'FAILED') { bgClass = 'var(--minted-danger-subtle)'; textClass = 'var(--minted-danger)'; }
        if (status === 'RUNNING') { bgClass = 'var(--minted-info-subtle)'; textClass = 'var(--minted-info)'; }
        return `<span class="px-2.5 py-1 rounded-full text-xs font-semibold" style="background-color: ${bgClass}; color: ${textClass};">${status}</span>`;
      }
    },
    {
      field: 'triggerType',
      headerName: 'Trigger',
      width: 120,
      cellRenderer: (params: any) => {
        const trigger = params.value;
        const bgClass = trigger === 'MANUAL' ? 'var(--minted-accent-subtle)' : 'var(--minted-info-subtle)';
        const textClass = trigger === 'MANUAL' ? 'var(--minted-accent)' : 'var(--minted-info)';
        return `<span class="px-2 py-0.5 rounded text-xs font-medium" style="background-color: ${bgClass}; color: ${textClass};">${trigger}</span>`;
      }
    },
    {
      field: 'startTime',
      headerName: 'Started',
      width: 180,
      valueFormatter: params => new Date(params.value).toLocaleString()
    },
    {
      field: 'endTime',
      headerName: 'Ended',
      width: 180,
      valueFormatter: params => params.value ? new Date(params.value).toLocaleString() : '-'
    },
    {
      field: 'totalSteps',
      headerName: 'Steps',
      width: 100,
      valueFormatter: params => `${params.data.completedSteps}/${params.value}`
    },
    {
      headerName: 'Actions',
      width: 120,
      cellClass: 'flex items-center',
      cellRenderer: (params: any) => {
        return `<button class="text-primary hover:underline font-medium flex items-center gap-1 text-sm outline-none cursor-pointer border-none bg-transparent" data-action="view-details">
                  Details <span class="material-icons text-sm">chevron_right</span>
                </button>`;
      }
    }
  ];

  gridOptions: GridOptions = {
    pagination: true,
    paginationPageSize: 15,
    paginationPageSizeSelector: [15, 30, 50],
    domLayout: 'normal',
    rowSelection: 'single',
    animateRows: true,
    overlayNoRowsTemplate: '<span class="ag-overlay-no-rows-center">No job executions found.</span>',
    onCellClicked: (params) => {
      const event = params.event as MouseEvent;
      const target = event.target as HTMLElement;
      if (target.closest('button[data-action="view-details"]')) {
        this.router.navigate(['/admin/jobs', params.data.id]);
      }
    }
  };

  constructor(
    private adminService: AdminService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) { }

  ngOnInit(): void {
    this.loadJobs();
  }

  loadJobs(): void {
    this.loading = true;
    this.adminService.getJobs(0, 500).subscribe({
      next: (res) => {
        this.jobs = res.content || [];
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  triggerJob(): void {
    this.loading = true;
    this.adminService.triggerJob('RECURRING_TRANSACTION_PROCESSOR').subscribe({
      next: () => {
        setTimeout(() => {
          this.loadJobs();
          this.cdr.detectChanges();
        }, 1000);
      },
      error: () => {
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }
}
