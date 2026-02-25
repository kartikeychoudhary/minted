import { Component } from '@angular/core';
import { ICellRendererAngularComp } from 'ag-grid-angular';
import { ICellRendererParams } from 'ag-grid-community';

export interface ActionsCallbacks {
  onEdit: (data: any) => void;
  onDelete: (data: any) => void;
  onSplit?: (data: any) => void;
}

@Component({
  selector: 'app-actions-cell-renderer',
  standalone: false,
  template: `
    <div class="flex items-center justify-end gap-2" (click)="$event.stopPropagation()">
      <button
        pButton
        type="button"
        icon="pi pi-users"
        class="p-button-text p-button-rounded p-button-sm"
        [ngClass]="{'split-active': params.data?.isSplit}"
        (click)="onSplitClick()"
        [pTooltip]="params.data?.isSplit ? 'Already split' : 'Split'"
        tooltipPosition="top">
      </button>
      <button
        pButton
        type="button"
        icon="pi pi-pencil"
        class="p-button-text p-button-rounded p-button-sm"
        (click)="onEditClick()"
        pTooltip="Edit"
        tooltipPosition="top">
      </button>
      <button
        pButton
        type="button"
        icon="pi pi-trash"
        class="p-button-text p-button-rounded p-button-danger p-button-sm"
        (click)="onDeleteClick()"
        pTooltip="Delete"
        tooltipPosition="top">
      </button>
    </div>
  `,
  styles: [`
    :host {
      display: flex;
      align-items: center;
      width: 100%;
      height: 100%;
    }

    ::ng-deep .p-button-sm {
      width: 2.25rem;
      height: 2.25rem;
      padding: 0;
      border-radius: 50% !important;
    }

    ::ng-deep .p-button-text {
      color: var(--minted-text-secondary);
      background: var(--minted-bg-surface) !important;
      border: 1px solid var(--minted-border-light) !important;
    }

    ::ng-deep .p-button-text:hover {
      background-color: var(--minted-bg-hover) !important;
      border-color: var(--minted-accent) !important;
      color: var(--minted-accent);
    }

    ::ng-deep .p-button-text.split-active {
      color: var(--minted-accent) !important;
      border-color: var(--minted-accent) !important;
      background: var(--minted-accent-subtle) !important;
    }

    ::ng-deep .p-button-text.p-button-danger {
      background: var(--minted-bg-surface) !important;
      border: 1px solid var(--minted-border-light) !important;
    }

    ::ng-deep .p-button-text.p-button-danger:hover {
      background-color: var(--minted-danger-subtle) !important;
      border-color: var(--minted-danger) !important;
      color: var(--minted-danger);
    }
  `]
})
export class ActionsCellRendererComponent implements ICellRendererAngularComp {
  params!: ICellRendererParams & { callbacks?: ActionsCallbacks };

  agInit(params: ICellRendererParams): void {
    this.params = params as ICellRendererParams & { callbacks?: ActionsCallbacks };
  }

  refresh(params: ICellRendererParams): boolean {
    this.params = params as ICellRendererParams & { callbacks?: ActionsCallbacks };
    return true;
  }

  onEditClick(): void {
    if (this.params.callbacks?.onEdit) {
      this.params.callbacks.onEdit(this.params.data);
    }
  }

  onDeleteClick(): void {
    if (this.params.callbacks?.onDelete) {
      this.params.callbacks.onDelete(this.params.data);
    }
  }

  onSplitClick(): void {
    if (this.params.callbacks?.onSplit) {
      this.params.callbacks.onSplit(this.params.data);
    }
  }
}
