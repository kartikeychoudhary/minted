import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ImportService } from '../../../../core/services/import.service';
import { BulkImportResponse } from '../../../../core/models/import.model';
import { Subscription, interval } from 'rxjs';
import { takeWhile } from 'rxjs/operators';

@Component({
  selector: 'app-import-job-detail',
  standalone: false,
  templateUrl: './import-job-detail.html',
  styleUrl: './import-job-detail.scss'
})
export class ImportJobDetail implements OnInit, OnDestroy {
  importData: BulkImportResponse | null = null;
  jobData: any = null;
  loading = true;
  error = '';
  private importId!: number;
  private pollSubscription: Subscription | null = null;
  private isPolling = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private importService: ImportService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.importId = parseInt(idParam, 10);
      if (!isNaN(this.importId)) {
        this.loadImportDetails();
      } else {
        this.error = 'Invalid import ID';
        this.loading = false;
      }
    } else {
      this.error = 'No import ID provided';
      this.loading = false;
    }
  }

  ngOnDestroy(): void {
    this.stopPolling();
  }

  loadImportDetails(): void {
    this.loading = true;
    this.importService.getImportById(this.importId).subscribe({
      next: (data) => {
        this.importData = data;
        this.loading = false;

        if (data.jobExecutionId) {
          this.loadJobDetails();
        }

        // Start polling if importing
        if (data.status === 'IMPORTING' && !this.isPolling) {
          this.startPolling();
        }

        this.cdr.detectChanges();
      },
      error: (err) => {
        this.error = 'Failed to load import details';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  loadJobDetails(): void {
    this.importService.getImportJobDetails(this.importId).subscribe({
      next: (data) => {
        this.jobData = data;
        this.cdr.detectChanges();
      },
      error: () => {
        // Job details may not be available yet
        this.cdr.detectChanges();
      }
    });
  }

  startPolling(): void {
    this.isPolling = true;
    this.pollSubscription = interval(5000).pipe(
      takeWhile(() => this.isPolling)
    ).subscribe(() => {
      this.importService.getImportById(this.importId).subscribe({
        next: (data) => {
          this.importData = data;
          if (data.jobExecutionId) {
            this.loadJobDetails();
          }
          if (data.status !== 'IMPORTING') {
            this.stopPolling();
          }
          this.cdr.detectChanges();
        }
      });
    });
  }

  stopPolling(): void {
    this.isPolling = false;
    if (this.pollSubscription) {
      this.pollSubscription.unsubscribe();
      this.pollSubscription = null;
    }
  }

  goBack(): void {
    this.router.navigate(['/import/jobs']);
  }

  getStatusStyle(status: string): any {
    switch (status) {
      case 'COMPLETED': return { 'background-color': 'var(--minted-success-subtle)', 'color': 'var(--minted-success)' };
      case 'FAILED': return { 'background-color': 'var(--minted-danger-subtle)', 'color': 'var(--minted-danger)' };
      case 'IMPORTING':
      case 'RUNNING': return { 'background-color': 'var(--minted-info-subtle)', 'color': 'var(--minted-info)' };
      case 'VALIDATED':
      case 'PENDING': return { 'background-color': 'var(--minted-bg-surface)', 'color': 'var(--minted-text-secondary)' };
      default: return { 'background-color': 'var(--minted-bg-surface)', 'color': 'var(--minted-text-secondary)' };
    }
  }

  getStepStatusColor(status: string): string {
    switch (status) {
      case 'COMPLETED': return 'var(--minted-success)';
      case 'FAILED': return 'var(--minted-danger)';
      case 'RUNNING': return 'var(--minted-info)';
      case 'PENDING': return 'var(--minted-text-muted)';
      default: return 'var(--minted-text-muted)';
    }
  }

  formatDuration(startStr: string, endStr?: string): string {
    if (!endStr) return 'Running...';
    const start = new Date(startStr).getTime();
    const end = new Date(endStr).getTime();
    const diffMs = end - start;
    if (diffMs < 1000) return `${diffMs} ms`;
    return `${(diffMs / 1000).toFixed(1)} s`;
  }

  formatJSON(jsonStr?: string): string {
    if (!jsonStr) return '{}';
    try {
      return JSON.stringify(JSON.parse(jsonStr), null, 2);
    } catch {
      return jsonStr;
    }
  }

  formatFileSize(bytes: number): string {
    if (!bytes || bytes === 0) return '0 B';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i];
  }
}
