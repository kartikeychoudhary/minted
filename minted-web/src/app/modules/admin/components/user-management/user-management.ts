import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { AdminService } from '../../../../core/services/admin.service';
import { AdminUserResponse } from '../../../../core/models/user.model';
import { MessageService, ConfirmationService } from 'primeng/api';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ColDef, GridOptions, themeQuartz } from 'ag-grid-community';
import { TagCellRendererComponent } from '../cell-renderers/tag-cell-renderer.component';
import { UserActionsCellRendererComponent } from '../cell-renderers/user-actions-cell-renderer.component';

@Component({
  selector: 'app-user-management',
  standalone: false,
  templateUrl: './user-management.html',
  styleUrl: './user-management.scss'
})
export class UserManagement implements OnInit {
  users: AdminUserResponse[] = [];
  loading = false;
  signupEnabled = false;
  loadingSignup = false;

  showCreateDialog = false;
  createForm: FormGroup;

  showResetDialog = false;
  resetForm: FormGroup;
  resetUserId: number | null = null;
  resetUsername = '';

  roleOptions = [
    { label: 'User', value: 'USER' },
    { label: 'Admin', value: 'ADMIN' }
  ];

  mintedTheme = themeQuartz.withParams({
    backgroundColor: 'var(--minted-bg-card)',
    foregroundColor: 'var(--minted-text-primary)',
    borderColor: 'var(--minted-border)',
    headerBackgroundColor: 'var(--minted-bg-card)',
    headerFontSize: 12,
    headerFontWeight: 600,
    headerTextColor: 'var(--minted-text-muted)',
    rowHoverColor: 'var(--minted-surface)',
    fontFamily: '"Inter", sans-serif',
    rowHeight: 56,
    headerHeight: 44,
  });

  colDefs: ColDef[] = [
    {
      field: 'username',
      headerName: 'Username',
      width: 160,
      cellRenderer: (params: any) =>
        `<span class="font-semibold">${params.value || ''}</span>`
    },
    { field: 'displayName', headerName: 'Display Name', width: 180 },
    { field: 'email', headerName: 'Email', flex: 1, minWidth: 180 },
    {
      field: 'role',
      headerName: 'Role',
      width: 110,
      cellRenderer: TagCellRendererComponent,
      cellRendererParams: {
        getTagConfig: (params: any) => ({
          value: params.value === 'ADMIN' ? 'Admin' : 'User',
          severity: params.value === 'ADMIN' ? 'contrast' : 'secondary',
          rounded: true
        })
      }
    },
    {
      field: 'isActive',
      headerName: 'Status',
      width: 120,
      cellRenderer: TagCellRendererComponent,
      cellRendererParams: {
        getTagConfig: (params: any) => ({
          value: params.value ? 'Active' : 'Disabled',
          severity: params.value ? 'success' : 'danger',
          rounded: true
        })
      }
    },
    {
      field: 'forcePasswordChange',
      headerName: 'Password',
      width: 140,
      cellRenderer: TagCellRendererComponent,
      cellRendererParams: {
        getTagConfig: (params: any) => params.value
          ? { value: 'Must Change', severity: 'warn', rounded: true }
          : null
      }
    },
    {
      field: 'createdAt',
      headerName: 'Created',
      width: 160,
      cellRenderer: (params: any) => {
        if (!params.value) return '';
        const d = new Date(params.value);
        return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
      }
    },
    {
      headerName: 'Actions',
      width: 230,
      sortable: false,
      filter: false,
      cellRenderer: UserActionsCellRendererComponent,
      cellRendererParams: {
        callbacks: {
          onToggle: (user: AdminUserResponse) => this.toggleUserActive(user),
          onResetPassword: (user: AdminUserResponse) => this.openResetDialog(user),
          onDelete: (user: AdminUserResponse) => this.deleteUser(user)
        }
      }
    }
  ];

  gridOptions: GridOptions = {
    domLayout: 'autoHeight'
  };

  constructor(
    private adminService: AdminService,
    private messageService: MessageService,
    private confirmationService: ConfirmationService,
    private fb: FormBuilder,
    private cdr: ChangeDetectorRef
  ) {
    this.createForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(50)]],
      password: ['', [Validators.required, Validators.minLength(8)]],
      displayName: [''],
      email: ['', [Validators.email]],
      role: ['USER']
    });

    this.resetForm = this.fb.group({
      newPassword: ['', [Validators.required, Validators.minLength(8)]]
    });
  }

  ngOnInit(): void {
    this.loadUsers();
    this.loadSignupSetting();
  }

  loadUsers(): void {
    this.loading = true;
    this.adminService.getUsers().subscribe({
      next: (data) => {
        this.users = data;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  loadSignupSetting(): void {
    this.loadingSignup = true;
    this.adminService.getSetting('SIGNUP_ENABLED').subscribe({
      next: (setting) => {
        this.signupEnabled = setting.settingValue === 'true';
        this.loadingSignup = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.loadingSignup = false;
        this.cdr.detectChanges();
      }
    });
  }

  toggleSignup(): void {
    const newValue = this.signupEnabled ? 'true' : 'false';
    this.adminService.updateSetting('SIGNUP_ENABLED', newValue).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Updated',
          detail: `Public registration ${this.signupEnabled ? 'enabled' : 'disabled'}`
        });
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.signupEnabled = !this.signupEnabled;
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: err.error?.message || 'Failed to update setting'
        });
        this.cdr.detectChanges();
      }
    });
  }

  openCreateDialog(): void {
    this.createForm.reset({ role: 'USER' });
    this.showCreateDialog = true;
  }

  createUser(): void {
    if (this.createForm.invalid) return;

    this.adminService.createUser(this.createForm.value).subscribe({
      next: () => {
        this.messageService.add({ severity: 'success', summary: 'Success', detail: 'User created successfully' });
        this.showCreateDialog = false;
        this.loadUsers();
      },
      error: (err) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: err.error?.message || 'Failed to create user'
        });
        this.cdr.detectChanges();
      }
    });
  }

  toggleUserActive(user: AdminUserResponse): void {
    this.adminService.toggleUserActive(user.id).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Updated',
          detail: `User ${user.username} ${user.isActive ? 'disabled' : 'enabled'}`
        });
        this.loadUsers();
      },
      error: (err) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: err.error?.message || 'Failed to update user'
        });
        this.cdr.detectChanges();
      }
    });
  }

  openResetDialog(user: AdminUserResponse): void {
    this.resetUserId = user.id;
    this.resetUsername = user.username;
    this.resetForm.reset();
    this.showResetDialog = true;
  }

  resetPassword(): void {
    if (this.resetForm.invalid || !this.resetUserId) return;

    this.adminService.resetPassword(this.resetUserId, this.resetForm.value).subscribe({
      next: () => {
        this.messageService.add({ severity: 'success', summary: 'Success', detail: 'Password reset successfully' });
        this.showResetDialog = false;
        this.resetUserId = null;
        this.loadUsers();
      },
      error: (err) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: err.error?.message || 'Failed to reset password'
        });
        this.cdr.detectChanges();
      }
    });
  }

  deleteUser(user: AdminUserResponse): void {
    this.confirmationService.confirm({
      key: 'userManagement',
      message: `Are you sure you want to delete user "${user.username}"? This will permanently remove all their data.`,
      header: 'Delete User',
      icon: 'pi pi-exclamation-triangle',
      acceptButtonStyleClass: 'p-button-danger',
      accept: () => {
        this.adminService.deleteUser(user.id).subscribe({
          next: () => {
            this.messageService.add({ severity: 'success', summary: 'Deleted', detail: `User ${user.username} deleted` });
            this.loadUsers();
          },
          error: (err) => {
            this.messageService.add({
              severity: 'error',
              summary: 'Error',
              detail: err.error?.message || 'Failed to delete user'
            });
            this.cdr.detectChanges();
          }
        });
      }
    });
  }
}
