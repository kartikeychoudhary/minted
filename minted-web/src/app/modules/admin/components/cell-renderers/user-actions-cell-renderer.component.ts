import { Component } from '@angular/core';
import { ICellRendererAngularComp } from 'ag-grid-angular';
import { ICellRendererParams } from 'ag-grid-community';
import { AdminUserResponse } from '../../../../core/models/user.model';

export interface UserActionsCallbacks {
  onToggle: (user: AdminUserResponse) => void;
  onResetPassword: (user: AdminUserResponse) => void;
  onDelete: (user: AdminUserResponse) => void;
}

@Component({
  selector: 'app-user-actions-cell-renderer',
  standalone: false,
  template: `
    <div class="flex items-center gap-1" *ngIf="user" (click)="$event.stopPropagation()">
      <p-button
        [label]="user.isActive ? 'Disable' : 'Enable'"
        [severity]="user.isActive ? 'danger' : 'success'"
        [outlined]="true"
        size="small"
        (onClick)="onToggleClick()">
      </p-button>
      <p-button
        icon="pi pi-key"
        [rounded]="true"
        [text]="true"
        size="small"
        pTooltip="Reset Password"
        tooltipPosition="top"
        (onClick)="onResetClick()">
      </p-button>
      <p-button
        icon="pi pi-trash"
        [rounded]="true"
        [text]="true"
        severity="danger"
        size="small"
        pTooltip="Delete User"
        tooltipPosition="top"
        (onClick)="onDeleteClick()">
      </p-button>
    </div>
  `,
  styles: [`
    :host {
      display: flex;
      align-items: center;
      width: 100%;
      height: 100%;
      padding: 8px 0;
    }
  `]
})
export class UserActionsCellRendererComponent implements ICellRendererAngularComp {
  params!: ICellRendererParams & { callbacks?: UserActionsCallbacks };
  user: AdminUserResponse | null = null;

  agInit(params: ICellRendererParams): void {
    this.params = params as ICellRendererParams & { callbacks?: UserActionsCallbacks };
    this.user = params.data;
  }

  refresh(params: ICellRendererParams): boolean {
    this.params = params as ICellRendererParams & { callbacks?: UserActionsCallbacks };
    this.user = params.data;
    return true;
  }

  onToggleClick(): void {
    if (this.user && this.params.callbacks?.onToggle) {
      this.params.callbacks.onToggle(this.user);
    }
  }

  onResetClick(): void {
    if (this.user && this.params.callbacks?.onResetPassword) {
      this.params.callbacks.onResetPassword(this.user);
    }
  }

  onDeleteClick(): void {
    if (this.user && this.params.callbacks?.onDelete) {
      this.params.callbacks.onDelete(this.user);
    }
  }
}
