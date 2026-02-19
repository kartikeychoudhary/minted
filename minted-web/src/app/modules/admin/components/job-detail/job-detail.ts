import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AdminService } from '../../../../core/services/admin.service';
import { JobExecution } from '../../../../core/models/admin.model';

@Component({
  selector: 'app-job-detail',
  standalone: false,
  templateUrl: './job-detail.html',
  styleUrl: './job-detail.scss'
})
export class JobDetail implements OnInit {
  job: JobExecution | null = null;
  loading = true;
  error = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private adminService: AdminService,
    private cdr: ChangeDetectorRef
  ) { }

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      const id = parseInt(idParam, 10);
      if (!isNaN(id)) {
        this.loadJobExecution(id);
      } else {
        this.error = 'Invalid job ID parameter';
        this.loading = false;
      }
    } else {
      this.error = 'No job ID provided';
      this.loading = false;
    }
  }

  loadJobExecution(id: number): void {
    this.loading = true;
    this.adminService.getJobExecution(id).subscribe({
      next: (data) => {
        this.job = data;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.error = 'Failed to load job execution details. They may not exist.';
        this.loading = false;
        this.cdr.detectChanges();
        console.error(err);
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/admin/jobs']);
  }

  getStatusStyle(status: string): any {
    switch (status) {
      case 'COMPLETED': return { 'background-color': 'var(--minted-success-subtle)', 'color': 'var(--minted-success)' };
      case 'FAILED': return { 'background-color': 'var(--minted-danger-subtle)', 'color': 'var(--minted-danger)' };
      case 'RUNNING': return { 'background-color': 'var(--minted-info-subtle)', 'color': 'var(--minted-info)' };
      case 'SKIPPED': return { 'background-color': 'var(--minted-accent-subtle)', 'color': 'var(--minted-accent)' };
      case 'PENDING': return { 'background-color': 'var(--minted-bg-surface)', 'color': 'var(--minted-text-secondary)' };
      default: return { 'background-color': 'var(--minted-bg-surface)', 'color': 'var(--minted-text-secondary)' };
    }
  }

  getTriggerStyle(trigger: string): any {
    return trigger === 'MANUAL'
      ? { 'background-color': 'var(--minted-accent-subtle)', 'color': 'var(--minted-accent)' }
      : { 'background-color': 'var(--minted-info-subtle)', 'color': 'var(--minted-info)' };
  }

  getStepStatusColor(status: string): string {
    switch (status) {
      case 'COMPLETED': return 'var(--minted-success)';
      case 'FAILED': return 'var(--minted-danger)';
      case 'RUNNING': return 'var(--minted-info)';
      case 'SKIPPED': return 'var(--minted-accent)';
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
      return jsonStr; // return raw if not valid json
    }
  }
}
