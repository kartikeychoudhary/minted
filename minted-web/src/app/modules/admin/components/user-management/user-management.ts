import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { AdminService } from '../../../../core/services/admin.service';
import { AdminUserResponse } from '../../../../core/models/user.model';
import { MessageService, ConfirmationService } from 'primeng/api';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ColDef, GridOptions, themeQuartz } from 'ag-grid-community';

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
      width: 100,
      cellRenderer: (params: any) => {
        const isAdmin = params.value === 'ADMIN';
        const bg = isAdmin ? 'var(--minted-accent)' : 'var(--minted-bg-surface)';
        const color = isAdmin ? '#fff' : 'var(--minted-text-secondary)';
        return `<span style="display:inline-block;padding:2px 8px;border-radius:4px;font-size:0.65rem;font-weight:700;letter-spacing:0.05em;text-transform:uppercase;background:${bg};color:${color}">${params.value}</span>`;
      }
    },
    {
      field: 'isActive',
      headerName: 'Status',
      width: 110,
      cellRenderer: (params: any) => {
        const active = params.value;
        const bg = active ? 'var(--minted-success-subtle)' : 'var(--minted-danger-subtle)';
        const color = active ? 'var(--minted-success)' : 'var(--minted-danger)';
        const text = active ? 'Active' : 'Disabled';
        return `<span style="display:inline-block;padding:2px 8px;border-radius:4px;font-size:0.65rem;font-weight:700;letter-spacing:0.05em;text-transform:uppercase;background:${bg};color:${color}">${text}</span>`;
      }
    },
    {
      field: 'forcePasswordChange',
      headerName: 'Password',
      width: 130,
      cellRenderer: (params: any) => {
        if (params.value) {
          return `<span style="display:inline-block;padding:2px 8px;border-radius:4px;font-size:0.65rem;font-weight:700;letter-spacing:0.05em;background:var(--minted-warning-subtle, #fef3c7);color:var(--minted-warning, #d97706)">MUST CHANGE</span>`;
        }
        return '';
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
      width: 220,
      sortable: false,
      filter: false,
      cellRenderer: (params: any) => {
        const user = params.data as AdminUserResponse;
        if (!user) return '';
        const toggleLabel = user.isActive ? 'Disable' : 'Enable';
        const toggleColor = user.isActive ? 'var(--minted-danger)' : 'var(--minted-success)';
        return `
          <div style="display:flex;align-items:center;gap:4px;height:100%">
            <button data-action="toggle" title="${toggleLabel} user" style="padding:4px 8px;border-radius:6px;font-size:0.7rem;font-weight:600;border:1px solid ${toggleColor};color:${toggleColor};background:transparent;cursor:pointer">${toggleLabel}</button>
            <button data-action="reset" title="Reset password" style="width:30px;height:30px;border-radius:50%;display:inline-flex;align-items:center;justify-content:center;border:1px solid var(--minted-text-muted);color:var(--minted-text-secondary);background:transparent;cursor:pointer"><span class="material-icons" style="font-size:16px">key</span></button>
            <button data-action="delete" title="Delete user" style="width:30px;height:30px;border-radius:50%;display:inline-flex;align-items:center;justify-content:center;border:1px solid var(--minted-danger);color:var(--minted-danger);background:transparent;cursor:pointer"><span class="material-icons" style="font-size:16px">delete</span></button>
          </div>
        `;
      }
    }
  ];

  gridOptions: GridOptions = {
    domLayout: 'autoHeight',
    onCellClicked: (params) => {
      const event = params.event as MouseEvent;
      const target = event.target as HTMLElement;
      const user = params.data as AdminUserResponse;
      if (!user) return;

      if (target.closest('button[data-action="toggle"]')) {
        this.toggleUserActive(user);
      } else if (target.closest('button[data-action="reset"]')) {
        this.openResetDialog(user);
      } else if (target.closest('button[data-action="delete"]')) {
        this.deleteUser(user);
      }
    }
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
