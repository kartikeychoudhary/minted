import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { AdminService } from '../../../../core/services/admin.service';
import { JobScheduleConfig, DefaultCategory, DefaultAccountType } from '../../../../core/models/admin.model';
import { MessageService, ConfirmationService } from 'primeng/api';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ColDef, GridOptions, themeQuartz } from 'ag-grid-community';

@Component({
  selector: 'app-server-settings',
  standalone: false,
  templateUrl: './server-settings.html',
  styleUrl: './server-settings.scss'
})
export class ServerSettings implements OnInit {
  schedules: JobScheduleConfig[] = [];
  categories: DefaultCategory[] = [];
  accountTypes: DefaultAccountType[] = [];

  loadingSchedules = false;
  loadingCategories = false;
  loadingAccountTypes = false;

  // Modals for new defaults
  showCategoryDialog = false;
  showAccountTypeDialog = false;

  categoryForm: FormGroup;
  accountTypeForm: FormGroup;

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
    rowHeight: 48,
    headerHeight: 44,
  });

  categoryColDefs: ColDef[] = [
    { field: 'name', headerName: 'Name', flex: 1 },
    { field: 'type', headerName: 'Type', width: 120 },
    { field: 'icon', headerName: 'Icon', width: 100, cellRenderer: (params: any) => `<i class="pi ${params.value}"></i>` },
    {
      headerName: 'Actions', width: 100, cellRenderer: () => `<button class="text-red-500 hover:bg-red-50 p-1.5 rounded" data-action="delete"><i class="pi pi-trash"></i></button>`
    }
  ];

  accountTypeColDefs: ColDef[] = [
    { field: 'name', headerName: 'Name', flex: 1 },
    {
      headerName: 'Actions', width: 100, cellRenderer: () => `<button class="text-red-500 hover:bg-red-50 p-1.5 rounded" data-action="delete"><i class="pi pi-trash"></i></button>`
    }
  ];

  categoryGridOptions: GridOptions = {
    domLayout: 'autoHeight',
    onCellClicked: (params) => {
      const event = params.event as MouseEvent;
      const target = event.target as HTMLElement;
      if (target.closest('button[data-action="delete"]')) {
        this.deleteCategory(params.data as DefaultCategory);
      }
    }
  };

  accountTypeGridOptions: GridOptions = {
    domLayout: 'autoHeight',
    onCellClicked: (params) => {
      const event = params.event as MouseEvent;
      const target = event.target as HTMLElement;
      if (target.closest('button[data-action="delete"]')) {
        this.deleteAccountType(params.data as DefaultAccountType);
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
    this.categoryForm = this.fb.group({
      name: ['', Validators.required],
      type: ['EXPENSE', Validators.required],
      icon: ['pi-tag']
    });

    this.accountTypeForm = this.fb.group({
      name: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.loadSchedules();
    this.loadCategories();
    this.loadAccountTypes();
  }

  loadSchedules() {
    this.loadingSchedules = true;
    this.adminService.getSchedules().subscribe({
      next: (data) => { this.schedules = data; this.loadingSchedules = false; this.cdr.detectChanges(); },
      error: () => { this.loadingSchedules = false; this.cdr.detectChanges(); }
    });
  }

  loadCategories() {
    this.loadingCategories = true;
    this.adminService.getDefaultCategories().subscribe({
      next: (data) => { this.categories = data; this.loadingCategories = false; this.cdr.detectChanges(); },
      error: () => { this.loadingCategories = false; this.cdr.detectChanges(); }
    });
  }

  loadAccountTypes() {
    this.loadingAccountTypes = true;
    this.adminService.getDefaultAccountTypes().subscribe({
      next: (data) => { this.accountTypes = data; this.loadingAccountTypes = false; this.cdr.detectChanges(); },
      error: () => { this.loadingAccountTypes = false; this.cdr.detectChanges(); }
    });
  }

  toggleSchedule(schedule: JobScheduleConfig) {
    this.adminService.updateSchedule(schedule.id, schedule.cronExpression, !schedule.enabled).subscribe({
      next: (updated) => {
        schedule.enabled = updated.enabled;
        this.messageService.add({ severity: 'success', summary: 'Success', detail: 'Schedule enabled status updated' });
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.messageService.add({ severity: 'error', summary: 'Error', detail: err.error?.message || 'Failed to update schedule' });
        this.cdr.detectChanges();
      }
    });
  }

  saveCategory() {
    if (this.categoryForm.invalid) return;
    this.adminService.createDefaultCategory(this.categoryForm.value).subscribe({
      next: () => {
        this.messageService.add({ severity: 'success', summary: 'Success', detail: 'Category created' });
        this.showCategoryDialog = false;
        this.categoryForm.reset({ type: 'EXPENSE', icon: 'pi-tag' });
        this.loadCategories();
      },
      error: (err) => {
        this.messageService.add({ severity: 'error', summary: 'Error', detail: err.error?.message || 'Failed to create category' });
        this.cdr.detectChanges();
      }
    });
  }

  saveAccountType() {
    if (this.accountTypeForm.invalid) return;
    this.adminService.createDefaultAccountType(this.accountTypeForm.value).subscribe({
      next: () => {
        this.messageService.add({ severity: 'success', summary: 'Success', detail: 'Account Type created' });
        this.showAccountTypeDialog = false;
        this.accountTypeForm.reset();
        this.loadAccountTypes();
      },
      error: (err) => {
        this.messageService.add({ severity: 'error', summary: 'Error', detail: err.error?.message || 'Failed to create account type' });
        this.cdr.detectChanges();
      }
    });
  }

  deleteCategory(category: DefaultCategory) {
    if (!category.id) return;
    this.confirmationService.confirm({
      message: `Delete default category "${category.name}"?`,
      accept: () => {
        this.adminService.deleteDefaultCategory(category.id!).subscribe({
          next: () => {
            this.messageService.add({ severity: 'success', summary: 'Deleted', detail: 'Category deleted' });
            this.loadCategories();
          }
        });
      }
    });
  }

  deleteAccountType(accountType: DefaultAccountType) {
    if (!accountType.id) return;
    this.confirmationService.confirm({
      message: `Delete default account type "${accountType.name}"?`,
      accept: () => {
        this.adminService.deleteDefaultAccountType(accountType.id!).subscribe({
          next: () => {
            this.messageService.add({ severity: 'success', summary: 'Deleted', detail: 'Account type deleted' });
            this.loadAccountTypes();
          }
        });
      }
    });
  }
}
