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
  templateUrl: './user-actions-cell-renderer.component.html',
  styleUrls: ['./user-actions-cell-renderer.component.scss']
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
