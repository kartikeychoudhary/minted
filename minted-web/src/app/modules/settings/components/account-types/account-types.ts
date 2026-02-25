import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MessageService, ConfirmationService } from 'primeng/api';
import { AccountTypeService } from '../../../../core/services/account-type.service';
import { AccountTypeResponse, AccountTypeRequest } from '../../../../core/models/account-type.model';

@Component({
  selector: 'app-account-types',
  standalone: false,
  templateUrl: './account-types.html',
  styleUrl: './account-types.scss',
})
export class AccountTypes implements OnInit {
  accountTypes: AccountTypeResponse[] = [];
  displayDialog = false;
  accountTypeForm?: FormGroup;
  isEditMode = false;
  selectedAccountTypeId?: number;
  loading = false;
  selectedIcon: any = null;
  duplicateError = '';

  // Available icons with PrimeIcons
  iconOptions = [
    { label: 'Bank', value: 'bank', icon: 'pi pi-building' },
    { label: 'Wallet', value: 'wallet', icon: 'pi pi-wallet' },
    { label: 'Credit Card', value: 'credit-card', icon: 'pi pi-credit-card' },
    { label: 'Money', value: 'money', icon: 'pi pi-money-bill' },
    { label: 'Chart', value: 'chart', icon: 'pi pi-chart-line' },
    { label: 'Dollar', value: 'dollar', icon: 'pi pi-dollar' },
    { label: 'Briefcase', value: 'briefcase', icon: 'pi pi-briefcase' },
    { label: 'Home', value: 'home', icon: 'pi pi-home' }
  ];

  constructor(
    private accountTypeService: AccountTypeService,
    private formBuilder: FormBuilder,
    private messageService: MessageService,
    private confirmationService: ConfirmationService,
    private cdr: ChangeDetectorRef
  ) { }

  ngOnInit(): void {
    this.initForm();
    // Load data after initialization to avoid ExpressionChangedAfterItHasBeenCheckedError
    setTimeout(() => {
      this.loadAccountTypes();
    }, 0);
  }

  initForm(): void {
    this.accountTypeForm = this.formBuilder.group({
      name: ['', [Validators.required, Validators.maxLength(50)]],
      description: ['', [Validators.required, Validators.maxLength(255)]],
      icon: ['bank', Validators.required]
    });
  }

  loadAccountTypes(): void {
    this.loading = true;
    console.log('Loading account types...');

    this.accountTypeService.getAll().subscribe({
      next: (data) => {
        console.log('Account types loaded successfully:', data);
        // Sort: active first, inactive (soft-deleted) last
        this.accountTypes = data.sort((a, b) => {
          if (a.isActive === b.isActive) return 0;
          return a.isActive ? -1 : 1;
        });
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error loading account types:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: error?.message || 'Failed to load account types'
        });
        this.loading = false;
        this.cdr.detectChanges();
      },
      complete: () => {
        console.log('Account types loading completed');
      }
    });
  }

  showAddDialog(): void {
    this.isEditMode = false;
    this.selectedAccountTypeId = undefined;
    this.selectedIcon = this.iconOptions[0]; // Set default icon
    this.accountTypeForm?.reset({ icon: 'bank' });
    this.duplicateError = '';
    this.displayDialog = true;
  }

  showEditDialog(accountType: AccountTypeResponse): void {
    this.isEditMode = true;
    this.selectedAccountTypeId = accountType.id;
    this.selectedIcon = this.iconOptions.find(opt => opt.value === accountType.icon) || null;
    this.accountTypeForm?.patchValue({
      name: accountType.name,
      description: accountType.description,
      icon: accountType.icon
    });
    this.duplicateError = '';
    this.displayDialog = true;
  }

  saveAccountType(): void {
    if (this.accountTypeForm?.invalid) {
      return;
    }

    this.duplicateError = '';
    const request: AccountTypeRequest = this.accountTypeForm!.value;

    if (this.isEditMode && this.selectedAccountTypeId) {
      this.accountTypeService.update(this.selectedAccountTypeId, request).subscribe({
        next: () => {
          this.messageService.add({
            severity: 'success',
            summary: 'Success',
            detail: 'Account type updated successfully'
          });
          this.loadAccountTypes();
          this.displayDialog = false;
        },
        error: (error) => {
          if (error?.status === 409) {
            this.duplicateError = error.error?.message || 'An account type with this name already exists.';
          } else {
            this.messageService.add({
              severity: 'error',
              summary: 'Error',
              detail: error?.error?.message || 'Failed to update account type'
            });
          }
        }
      });
    } else {
      this.accountTypeService.create(request).subscribe({
        next: () => {
          this.messageService.add({
            severity: 'success',
            summary: 'Success',
            detail: 'Account type created successfully'
          });
          this.loadAccountTypes();
          this.displayDialog = false;
        },
        error: (error) => {
          if (error?.status === 409) {
            this.duplicateError = error.error?.message || 'An account type with this name already exists.';
          } else {
            this.messageService.add({
              severity: 'error',
              summary: 'Error',
              detail: error?.error?.message || 'Failed to create account type'
            });
          }
        }
      });
    }
  }

  confirmDelete(accountType: AccountTypeResponse): void {
    this.confirmationService.confirm({
      key: 'accountTypes',
      message: `Are you sure you want to delete ${accountType.name}?`,
      header: 'Delete Confirmation',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.deleteAccountType(accountType.id);
      }
    });
  }

  deleteAccountType(id: number): void {
    this.accountTypeService.delete(id).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Success',
          detail: 'Account type deleted successfully'
        });
        this.loadAccountTypes();
      },
      error: (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: error?.error?.message || 'Failed to delete account type'
        });
      }
    });
  }

  restoreAccountType(accountType: AccountTypeResponse): void {
    this.accountTypeService.toggleActive(accountType.id).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Restored',
          detail: `Account type '${accountType.name}' restored successfully`
        });
        this.loadAccountTypes();
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to restore account type'
        });
      }
    });
  }

  getIconBackgroundClass(icon: string): string {
    const iconColorMap: { [key: string]: string } = {
      'bank': 'bg-amber-100 text-amber-600',
      'wallet': 'bg-blue-100 text-blue-600',
      'credit-card': 'bg-purple-100 text-purple-600',
      'chart': 'bg-green-100 text-green-600',
      'money': 'bg-orange-100 text-orange-600',
      'dollar': 'bg-teal-100 text-teal-600',
      'briefcase': 'bg-indigo-100 text-indigo-600',
      'home': 'bg-pink-100 text-pink-600'
    };
    return iconColorMap[icon] || 'bg-gray-100 text-gray-600';
  }

  getIconClass(iconValue: string): string {
    // Handle null, undefined, or non-string values
    if (!iconValue || typeof iconValue !== 'string') {
      return 'pi pi-circle';
    }

    // Handle both new icon format and legacy format from backend
    const iconOption = this.iconOptions.find(opt => opt.value === iconValue);
    if (iconOption) {
      return iconOption.icon;
    }

    // Fallback for any backend stored values
    // Check if it's already a valid PrimeIcon class
    if (iconValue.startsWith('pi pi-')) {
      return iconValue;
    }

    // Try to map common icon names
    const iconMap: { [key: string]: string } = {
      'account_balance': 'pi pi-building',
      'savings': 'pi pi-wallet',
      'credit_card': 'pi pi-credit-card',
      'trending_up': 'pi pi-chart-line',
      'monetization_on': 'pi pi-money-bill',
      'wallet': 'pi pi-wallet',
      'account_balance_wallet': 'pi pi-wallet',
      'payment': 'pi pi-dollar'
    };

    return iconMap[iconValue] || 'pi pi-circle';
  }

  getIconLabel(iconValue: string): string {
    if (!iconValue || typeof iconValue !== 'string') {
      return '';
    }
    const iconOption = this.iconOptions.find(opt => opt.value === iconValue);
    return iconOption ? iconOption.label : iconValue;
  }

  getIconClassFromValue(value: string): string {
    return this.getIconClass(value);
  }

  getIconLabelFromValue(value: string): string {
    return this.getIconLabel(value);
  }

  onIconChange(event: any): void {
    const selectedValue = event.value;
    this.selectedIcon = this.iconOptions.find(opt => opt.value === selectedValue) || null;
    console.log('Icon changed:', this.selectedIcon);
  }
}
