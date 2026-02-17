import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MessageService, ConfirmationService } from 'primeng/api';
import { AccountService } from '../../../../core/services/account.service';
import { AccountTypeService } from '../../../../core/services/account-type.service';
import { AccountResponse, AccountRequest } from '../../../../core/models/account.model';
import { AccountTypeResponse } from '../../../../core/models/account-type.model';

@Component({
  selector: 'app-accounts',
  standalone: false,
  templateUrl: './accounts.html',
  styleUrl: './accounts.scss',
})
export class Accounts implements OnInit {
  accounts: AccountResponse[] = [];
  accountTypes: AccountTypeResponse[] = [];
  displayDialog = false;
  accountForm?: FormGroup;
  isEditMode = false;
  selectedAccountId?: number;
  loading = false;

  constructor(
    private accountService: AccountService,
    private accountTypeService: AccountTypeService,
    private formBuilder: FormBuilder,
    private messageService: MessageService,
    private confirmationService: ConfirmationService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.initForm();
    setTimeout(() => {
      this.loadAccounts();
      this.loadAccountTypes();
    }, 0);
  }

  initForm(): void {
    this.accountForm = this.formBuilder.group({
      name: ['', [Validators.required, Validators.maxLength(100)]],
      accountTypeId: [null, Validators.required],
      balance: [0, [Validators.required]],
      currency: ['USD', [Validators.maxLength(3)]],
      color: ['#c48821'],
      icon: ['']
    });
  }

  loadAccounts(): void {
    this.loading = true;
    console.log('Loading accounts...');

    this.accountService.getAll().subscribe({
      next: (data) => {
        console.log('Accounts loaded successfully:', data);
        this.accounts = data;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error loading accounts:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: error?.message || 'Failed to load accounts'
        });
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  loadAccountTypes(): void {
    this.accountTypeService.getAll().subscribe({
      next: (data) => {
        console.log('Account types loaded for dropdown:', data);
        this.accountTypes = data;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error loading account types:', error);
      }
    });
  }

  showAddDialog(): void {
    this.isEditMode = false;
    this.selectedAccountId = undefined;
    this.accountForm?.reset({
      balance: 0,
      currency: 'USD',
      color: '#c48821',
      icon: ''
    });
    this.displayDialog = true;
  }

  showEditDialog(account: AccountResponse): void {
    this.isEditMode = true;
    this.selectedAccountId = account.id;
    this.accountForm?.patchValue({
      name: account.name,
      accountTypeId: account.accountTypeId,
      balance: account.balance,
      currency: account.currency,
      color: account.color,
      icon: account.icon
    });
    this.displayDialog = true;
  }

  saveAccount(): void {
    if (this.accountForm?.invalid) {
      return;
    }

    const request: AccountRequest = this.accountForm!.value;

    if (this.isEditMode && this.selectedAccountId) {
      this.accountService.update(this.selectedAccountId, request).subscribe({
        next: () => {
          this.messageService.add({
            severity: 'success',
            summary: 'Success',
            detail: 'Account updated successfully'
          });
          this.loadAccounts();
          this.displayDialog = false;
        },
        error: (error) => {
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: error?.message || 'Failed to update account'
          });
        }
      });
    } else {
      this.accountService.create(request).subscribe({
        next: () => {
          this.messageService.add({
            severity: 'success',
            summary: 'Success',
            detail: 'Account created successfully'
          });
          this.loadAccounts();
          this.displayDialog = false;
        },
        error: (error) => {
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: error?.message || 'Failed to create account'
          });
        }
      });
    }
  }

  confirmDelete(account: AccountResponse): void {
    this.confirmationService.confirm({
      message: `Are you sure you want to disconnect ${account.name}?`,
      header: 'Disconnect Confirmation',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.deleteAccount(account.id);
      }
    });
  }

  deleteAccount(id: number): void {
    this.accountService.delete(id).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Success',
          detail: 'Account disconnected successfully'
        });
        this.loadAccounts();
      },
      error: (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: error?.message || 'Failed to disconnect account'
        });
      }
    });
  }

  getAccountTypeColor(accountTypeName: string): string {
    const colorMap: { [key: string]: string } = {
      'Checking': 'bg-blue-100 text-blue-800',
      'Savings': 'bg-green-100 text-green-800',
      'Credit Card': 'bg-purple-100 text-purple-800',
      'Investments': 'bg-amber-100 text-amber-800',
      'Cash': 'bg-gray-100 text-gray-800',
      'Loan': 'bg-red-100 text-red-800'
    };
    return colorMap[accountTypeName] || 'bg-gray-100 text-gray-800';
  }

  formatCurrency(balance: number, currency: string): string {
    if (balance < 0) {
      return `-$${Math.abs(balance).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
    }
    return `$${balance.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
  }

  isNegativeBalance(balance: number): boolean {
    return balance < 0;
  }

  getIconClass(iconValue: string): string {
    if (!iconValue || typeof iconValue !== 'string') {
      return 'pi pi-wallet';
    }

    // Handle PrimeIcons
    if (iconValue.startsWith('pi pi-')) {
      return iconValue;
    }

    // Map common icon names to PrimeIcons
    const iconMap: { [key: string]: string } = {
      'account_balance': 'pi pi-building',
      'savings': 'pi pi-wallet',
      'credit_card': 'pi pi-credit-card',
      'trending_up': 'pi pi-chart-line',
      'wallet': 'pi pi-wallet',
      'bank': 'pi pi-building',
      'money': 'pi pi-money-bill',
      'dollar': 'pi pi-dollar'
    };

    return iconMap[iconValue] || 'pi pi-wallet';
  }
}
