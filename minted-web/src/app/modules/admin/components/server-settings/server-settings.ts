import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { AdminService } from '../../../../core/services/admin.service';
import { LlmConfigService } from '../../../../core/services/llm-config.service';
import { JobScheduleConfig, DefaultCategory, DefaultAccountType } from '../../../../core/models/admin.model';
import { LlmModel, LlmModelRequest } from '../../../../core/models/llm-config.model';
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

  // Feature toggles
  parserEnabled = false;
  adminKeyShared = false;
  loadingToggles = false;

  // LLM Models
  llmModels: LlmModel[] = [];
  loadingModels = false;
  showModelDialog = false;
  editingModel: LlmModel | null = null;
  modelForm: FormGroup;

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


  llmModelColDefs: ColDef[] = [
    { field: 'name', headerName: 'Name', flex: 1 },
    { field: 'modelKey', headerName: 'Model Key', flex: 1 },
    { field: 'provider', headerName: 'Provider', width: 100 },
    {
      field: 'isActive', headerName: 'Status', width: 100,
      cellRenderer: (params: any) => params.value
        ? '<span style="color:var(--minted-success);font-weight:600">Active</span>'
        : '<span style="color:var(--minted-text-muted);opacity:0.6">Disabled</span>'
    },
    {
      field: 'isDefault', headerName: 'Default', width: 90,
      cellRenderer: (params: any) => params.value ? '<i class="pi pi-star-fill" style="color:var(--minted-accent)"></i>' : ''
    },
    {
      headerName: 'Actions', width: 140,
      cellRenderer: (params: any) => {
        const toggleLabel = params.data?.isActive ? 'Disable' : 'Enable';
        return `<button class="text-blue-500 hover:bg-blue-50 p-1 rounded mr-1" data-action="edit" title="Edit"><i class="pi pi-pencil"></i></button>` +
          `<button class="text-orange-500 hover:bg-orange-50 p-1 rounded mr-1" data-action="toggle" title="${toggleLabel}"><i class="pi pi-${params.data?.isActive ? 'eye-slash' : 'eye'}"></i></button>` +
          `<button class="text-red-500 hover:bg-red-50 p-1 rounded" data-action="delete" title="Delete"><i class="pi pi-trash"></i></button>`;
      }
    }
  ];

  llmModelGridOptions: GridOptions = {
    domLayout: 'autoHeight',
    onCellClicked: (params) => {
      const event = params.event as MouseEvent;
      const target = event.target as HTMLElement;
      if (target.closest('button[data-action="edit"]')) {
        this.editModel(params.data);
      } else if (target.closest('button[data-action="toggle"]')) {
        this.toggleModel(params.data);
      } else if (target.closest('button[data-action="delete"]')) {
        this.deleteLlmModel(params.data);
      }
    }
  };

  constructor(
    private adminService: AdminService,
    private llmConfigService: LlmConfigService,
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

    this.modelForm = this.fb.group({
      name: ['', Validators.required],
      provider: ['GEMINI'],
      modelKey: ['', Validators.required],
      description: [''],
      isDefault: [false]
    });
  }

  ngOnInit(): void {
    this.loadSchedules();
    this.loadCategories();
    this.loadAccountTypes();
    this.loadFeatureToggles();
    this.loadLlmModels();
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

  // --- Feature Toggles ---

  loadFeatureToggles() {
    this.loadingToggles = true;
    this.adminService.getSetting('CREDIT_CARD_PARSER_ENABLED').subscribe({
      next: (s) => { this.parserEnabled = s.settingValue === 'true'; this.cdr.detectChanges(); },
      error: () => { this.cdr.detectChanges(); }
    });
    this.adminService.getSetting('ADMIN_LLM_KEY_SHARED').subscribe({
      next: (s) => { this.adminKeyShared = s.settingValue === 'true'; this.loadingToggles = false; this.cdr.detectChanges(); },
      error: () => { this.loadingToggles = false; this.cdr.detectChanges(); }
    });
  }

  toggleParserEnabled() {
    // ngModel already updated parserEnabled before onChange fires — use it directly
    const newVal = this.parserEnabled;
    this.adminService.updateSetting('CREDIT_CARD_PARSER_ENABLED', String(newVal)).subscribe({
      next: () => {
        this.messageService.add({ severity: 'success', summary: 'Updated', detail: `Credit Card Parser ${newVal ? 'enabled' : 'disabled'}` });
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.parserEnabled = !newVal; // revert on failure
        this.messageService.add({ severity: 'error', summary: 'Error', detail: err.error?.message || 'Failed to update setting' });
        this.cdr.detectChanges();
      }
    });
  }

  toggleAdminKeyShared() {
    // ngModel already updated adminKeyShared before onChange fires — use it directly
    const newVal = this.adminKeyShared;
    this.adminService.updateSetting('ADMIN_LLM_KEY_SHARED', String(newVal)).subscribe({
      next: () => {
        this.messageService.add({ severity: 'success', summary: 'Updated', detail: `Admin LLM key sharing ${newVal ? 'enabled' : 'disabled'}` });
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.adminKeyShared = !newVal; // revert on failure
        this.messageService.add({ severity: 'error', summary: 'Error', detail: err.error?.message || 'Failed to update setting' });
        this.cdr.detectChanges();
      }
    });
  }

  // --- LLM Models ---

  loadLlmModels() {
    this.loadingModels = true;
    this.llmConfigService.getAllModels().subscribe({
      next: (data) => { this.llmModels = data; this.loadingModels = false; this.cdr.detectChanges(); },
      error: () => { this.loadingModels = false; this.cdr.detectChanges(); }
    });
  }

  openAddModel() {
    this.editingModel = null;
    this.modelForm.reset({ provider: 'GEMINI', isDefault: false });
    this.showModelDialog = true;
  }

  editModel(model: LlmModel) {
    this.editingModel = model;
    this.modelForm.patchValue({
      name: model.name,
      provider: model.provider,
      modelKey: model.modelKey,
      description: model.description,
      isDefault: model.isDefault
    });
    this.showModelDialog = true;
  }

  saveModel() {
    if (this.modelForm.invalid) return;
    const req: LlmModelRequest = this.modelForm.value;

    if (this.editingModel) {
      this.llmConfigService.updateModel(this.editingModel.id, req).subscribe({
        next: () => {
          this.messageService.add({ severity: 'success', summary: 'Updated', detail: 'Model updated' });
          this.showModelDialog = false;
          this.loadLlmModels();
        },
        error: (err) => {
          this.messageService.add({ severity: 'error', summary: 'Error', detail: err.error?.message || 'Failed to update model' });
          this.cdr.detectChanges();
        }
      });
    } else {
      this.llmConfigService.createModel(req).subscribe({
        next: () => {
          this.messageService.add({ severity: 'success', summary: 'Created', detail: 'Model created' });
          this.showModelDialog = false;
          this.loadLlmModels();
        },
        error: (err) => {
          this.messageService.add({ severity: 'error', summary: 'Error', detail: err.error?.message || 'Failed to create model' });
          this.cdr.detectChanges();
        }
      });
    }
  }

  toggleModel(model: LlmModel) {
    const req: LlmModelRequest = {
      name: model.name,
      modelKey: model.modelKey,
      isActive: !model.isActive
    };
    this.llmConfigService.updateModel(model.id, req).subscribe({
      next: () => {
        this.messageService.add({ severity: 'success', summary: 'Updated', detail: `Model ${model.isActive ? 'disabled' : 'enabled'}` });
        this.loadLlmModels();
      },
      error: (err) => {
        this.messageService.add({ severity: 'error', summary: 'Error', detail: err.error?.message || 'Failed to toggle model' });
        this.cdr.detectChanges();
      }
    });
  }

  deleteLlmModel(model: LlmModel) {
    this.confirmationService.confirm({
      message: `Delete model "${model.name}"?`,
      accept: () => {
        this.llmConfigService.deleteModel(model.id).subscribe({
          next: () => {
            this.messageService.add({ severity: 'success', summary: 'Deleted', detail: 'Model deleted' });
            this.loadLlmModels();
          },
          error: (err) => {
            this.messageService.add({ severity: 'error', summary: 'Error', detail: err.error?.message || 'Failed to delete model' });
            this.cdr.detectChanges();
          }
        });
      }
    });
  }
}
