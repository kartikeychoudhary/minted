import { Component } from '@angular/core';
import { ICellRendererAngularComp } from 'ag-grid-angular';
import { ICellRendererParams } from 'ag-grid-community';

@Component({
  selector: 'app-status-cell-renderer',
  standalone: false,
  template: `
    <span class="px-2.5 py-1 rounded-full text-xs font-semibold inline-flex items-center"
          [ngStyle]="getStatusStyle()">
      {{ params.value }}
    </span>
  `,
  styles: [`
    :host {
      display: flex;
      align-items: center;
      width: 100%;
      height: 100%;
    }
  `]
})
export class StatusCellRendererComponent implements ICellRendererAngularComp {
  params!: ICellRendererParams;

  agInit(params: ICellRendererParams): void {
    this.params = params;
  }

  refresh(params: ICellRendererParams): boolean {
    this.params = params;
    return true;
  }

  getStatusStyle(): Record<string, string> {
    const status = this.params.value;
    switch (status) {
      case 'VALID':
        return { 'background-color': 'var(--minted-success-subtle)', 'color': 'var(--minted-success)' };
      case 'ERROR':
        return { 'background-color': 'var(--minted-danger-subtle)', 'color': 'var(--minted-danger)' };
      case 'DUPLICATE':
        return { 'background-color': 'var(--minted-accent-subtle)', 'color': 'var(--minted-accent)' };
      case 'COMPLETED':
        return { 'background-color': 'var(--minted-success-subtle)', 'color': 'var(--minted-success)' };
      case 'FAILED':
        return { 'background-color': 'var(--minted-danger-subtle)', 'color': 'var(--minted-danger)' };
      case 'IMPORTING':
      case 'RUNNING':
        return { 'background-color': 'var(--minted-info-subtle)', 'color': 'var(--minted-info)' };
      case 'VALIDATED':
      case 'PENDING':
        return { 'background-color': 'var(--minted-bg-surface)', 'color': 'var(--minted-text-secondary)' };
      default:
        return { 'background-color': 'var(--minted-bg-surface)', 'color': 'var(--minted-text-secondary)' };
    }
  }
}
