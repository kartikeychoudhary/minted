import { Component } from '@angular/core';
import { ICellRendererAngularComp } from 'ag-grid-angular';
import { ICellRendererParams } from 'ag-grid-community';

export interface SplitActionsCallbacks {
  onEdit: (data: any) => void;
  onDelete: (data: any) => void;
}

@Component({
  selector: 'app-split-actions-cell-renderer',
  standalone: false,
  template: `
    <div class="flex items-center justify-end gap-1" (click)="$event.stopPropagation()">
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

    :host .p-button.p-button-rounded {
      width: 2rem;
      height: 2rem;
      padding: 0;
      border-radius: 8px !important;
      border: 1px solid transparent !important;
      background: transparent !important;
      color: var(--minted-text-muted);
      box-shadow: none !important;
    }

    :host .p-button.p-button-rounded:hover {
      background: var(--minted-bg-hover) !important;
      border-color: var(--minted-border) !important;
      color: var(--minted-text-primary);
    }

    :host .p-button.p-button-rounded.p-button-danger:hover {
      background: var(--minted-danger-subtle) !important;
      border-color: transparent !important;
      color: var(--minted-danger);
    }
  `]
})
export class SplitActionsCellRendererComponent implements ICellRendererAngularComp {
  params!: ICellRendererParams & { callbacks?: SplitActionsCallbacks };

  agInit(params: ICellRendererParams): void {
    this.params = params as ICellRendererParams & { callbacks?: SplitActionsCallbacks };
  }

  refresh(params: ICellRendererParams): boolean {
    this.params = params as ICellRendererParams & { callbacks?: SplitActionsCallbacks };
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
}
