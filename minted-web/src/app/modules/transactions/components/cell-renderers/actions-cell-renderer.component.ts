import { Component } from '@angular/core';
import { ICellRendererAngularComp } from 'ag-grid-angular';
import { ICellRendererParams } from 'ag-grid-community';

export interface ActionsCallbacks {
  onEdit: (data: any) => void;
  onDelete: (data: any) => void;
}

@Component({
  selector: 'app-actions-cell-renderer',
  standalone: false,
  template: `
    <div class="flex items-center justify-end gap-2">
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
      width: 2rem;
      height: 2rem;
      padding: 0;
    }

    ::ng-deep .p-button-text {
      color: #6c757d;
    }

    ::ng-deep .p-button-text:hover {
      background-color: #f8f9fa;
      color: #c48821;
    }

    ::ng-deep .p-button-text.p-button-danger:hover {
      background-color: #fee;
      color: #dc3545;
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
}
