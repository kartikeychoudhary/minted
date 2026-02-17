import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { MessageService, ConfirmationService } from 'primeng/api';
import { TransactionService } from '../../../../core/services/transaction.service';
import { CategoryService } from '../../../../core/services/category.service';
import { AccountService } from '../../../../core/services/account.service';
import {
  TransactionRequest,
  TransactionResponse,
  DateFilterOption,
  TransactionFilters
} from '../../../../core/models/transaction.model';
import { CategoryResponse, TransactionType } from '../../../../core/models/category.model';
import { AccountResponse } from '../../../../core/models/account.model';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

@Component({
  selector: 'app-transactions-list',
  standalone: false,
  templateUrl: './transactions-list.html',
  styleUrl: './transactions-list.scss',
})
export class TransactionsList implements OnInit {
  transactions: TransactionResponse[] = [];
  filteredTransactions: TransactionResponse[] = [];
  categories: CategoryResponse[] = [];
  accounts: AccountResponse[] = [];
  loading = false;
  showDialog = false;
  isEditMode = false;
  transactionForm?: FormGroup;
  selectedTransaction?: TransactionResponse;
  searchTerm = '';

  // Filter options
  selectedDateFilter: DateFilterOption = DateFilterOption.THIS_MONTH;
  selectedAccountId?: number;
  selectedCategoryId?: number;
  customStartDate?: Date;
  customEndDate?: Date;
  showCustomDatePickers = false;

  // Pagination
  first = 0;
  rows = 10;
  totalRecords = 0;

  // Transaction types for dropdown
  transactionTypes = [
    { label: 'Expense', value: TransactionType.EXPENSE },
    { label: 'Income', value: TransactionType.INCOME },
    { label: 'Transfer', value: TransactionType.TRANSFER }
  ];

  // Expose enums to template
  DateFilterOption = DateFilterOption;

  // Computed options for dropdowns
  accountOptions: { label: string; value: number | undefined }[] = [];
  categoryOptions: { label: string; value: number | undefined }[] = [];
  toAccountOptions: { label: string; value: number | null }[] = [];

  constructor(
    private transactionService: TransactionService,
    private categoryService: CategoryService,
    private accountService: AccountService,
    private messageService: MessageService,
    private confirmationService: ConfirmationService,
    private formBuilder: FormBuilder,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadData();
  }

  initForm(): void {
    this.transactionForm = this.formBuilder.group({
      amount: [null, [Validators.required, Validators.min(0.01)]],
      type: [TransactionType.EXPENSE, Validators.required],
      description: ['', [Validators.required, Validators.maxLength(500)]],
      notes: [''],
      transactionDate: [new Date(), Validators.required],
      accountId: [null, Validators.required],
      toAccountId: [null],
      categoryId: [null, Validators.required],
      isRecurring: [false],
      tags: ['', Validators.maxLength(500)]
    });
  }

  loadData(): void {
    this.loading = true;
    Promise.all([
      this.loadCategories(),
      this.loadAccounts(),
      this.loadTransactions()
    ]).finally(() => {
      this.loading = false;
      this.cdr.detectChanges();
    });
  }

  async loadCategories(): Promise<void> {
    this.categoryService.getAll().subscribe({
      next: (data) => {
        this.categories = data;
        this.categoryOptions = [
          { label: 'All Categories', value: undefined },
          ...data.map(c => ({ label: c.name, value: c.id }))
        ];
      },
      error: (error) => {
        console.error('Error loading categories:', error);
      }
    });
  }

  async loadAccounts(): Promise<void> {
    this.accountService.getAll().subscribe({
      next: (data) => {
        this.accounts = data;
        this.accountOptions = [
          { label: 'All Accounts', value: undefined },
          ...data.map(a => ({ label: a.name, value: a.id }))
        ];
        this.toAccountOptions = [
          { label: 'None', value: null },
          ...data.map(a => ({ label: a.name, value: a.id }))
        ];
      },
      error: (error) => {
        console.error('Error loading accounts:', error);
      }
    });
  }

  loadTransactions(): void {
    const dateRange = this.getDateRange();

    if (this.selectedAccountId || this.selectedCategoryId) {
      const filters: TransactionFilters = {
        startDate: dateRange.startDate,
        endDate: dateRange.endDate,
        accountId: this.selectedAccountId,
        categoryId: this.selectedCategoryId
      };

      this.transactionService.getByFilters(filters).subscribe({
        next: (data) => {
          this.transactions = data;
          this.applySearchFilter();
        },
        error: (error) => {
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: 'Failed to load transactions'
          });
        }
      });
    } else {
      this.transactionService.getByDateRange(dateRange.startDate, dateRange.endDate).subscribe({
        next: (data) => {
          this.transactions = data;
          this.applySearchFilter();
        },
        error: (error) => {
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: 'Failed to load transactions'
          });
        }
      });
    }
  }

  getDateRange(): { startDate: string; endDate: string } {
    const today = new Date();
    let startDate: Date;
    let endDate: Date = today;

    switch (this.selectedDateFilter) {
      case DateFilterOption.THIS_MONTH:
        startDate = new Date(today.getFullYear(), today.getMonth(), 1);
        break;
      case DateFilterOption.LAST_MONTH:
        startDate = new Date(today.getFullYear(), today.getMonth() - 1, 1);
        endDate = new Date(today.getFullYear(), today.getMonth(), 0);
        break;
      case DateFilterOption.LAST_3_MONTHS:
        startDate = new Date(today.getFullYear(), today.getMonth() - 3, 1);
        break;
      case DateFilterOption.CUSTOM:
        startDate = this.customStartDate || new Date(today.getFullYear(), today.getMonth(), 1);
        endDate = this.customEndDate || today;
        break;
      default:
        startDate = new Date(today.getFullYear(), today.getMonth(), 1);
    }

    return {
      startDate: startDate.toISOString().split('T')[0],
      endDate: endDate.toISOString().split('T')[0]
    };
  }

  applySearchFilter(): void {
    if (!this.searchTerm) {
      this.filteredTransactions = [...this.transactions];
    } else {
      const term = this.searchTerm.toLowerCase();
      this.filteredTransactions = this.transactions.filter(t =>
        t.description.toLowerCase().includes(term) ||
        t.categoryName.toLowerCase().includes(term) ||
        t.accountName.toLowerCase().includes(term)
      );
    }
    this.totalRecords = this.filteredTransactions.length;
  }

  onDateFilterChange(filter: DateFilterOption): void {
    this.selectedDateFilter = filter;
    this.showCustomDatePickers = filter === DateFilterOption.CUSTOM;

    // Only load transactions if not custom range (wait for user to select dates)
    if (filter !== DateFilterOption.CUSTOM) {
      this.loadTransactions();
    }
  }

  onCustomDateChange(): void {
    if (this.customStartDate && this.customEndDate) {
      this.loadTransactions();
    }
  }

  onAccountFilterChange(): void {
    this.loadTransactions();
  }

  onCategoryFilterChange(): void {
    this.loadTransactions();
  }

  onSearchChange(): void {
    this.applySearchFilter();
  }

  openNewDialog(): void {
    this.isEditMode = false;
    this.transactionForm?.reset({
      type: TransactionType.EXPENSE,
      transactionDate: new Date(),
      isRecurring: false
    });
    this.showDialog = true;
  }

  openEditDialog(transaction: TransactionResponse): void {
    this.isEditMode = true;
    this.selectedTransaction = transaction;
    this.transactionForm?.patchValue({
      amount: transaction.amount,
      type: transaction.type,
      description: transaction.description,
      notes: transaction.notes,
      transactionDate: new Date(transaction.transactionDate),
      accountId: transaction.accountId,
      toAccountId: transaction.toAccountId,
      categoryId: transaction.categoryId,
      isRecurring: transaction.isRecurring,
      tags: transaction.tags
    });
    this.showDialog = true;
  }

  saveTransaction(): void {
    if (this.transactionForm?.invalid) {
      Object.keys(this.transactionForm.controls).forEach(key => {
        this.transactionForm?.get(key)?.markAsTouched();
      });
      return;
    }

    const formValue = this.transactionForm!.value;
    const request: TransactionRequest = {
      amount: formValue.amount,
      type: formValue.type,
      description: formValue.description,
      notes: formValue.notes,
      transactionDate: this.formatDate(formValue.transactionDate),
      accountId: formValue.accountId,
      toAccountId: formValue.toAccountId,
      categoryId: formValue.categoryId,
      isRecurring: formValue.isRecurring,
      tags: formValue.tags
    };

    if (this.isEditMode && this.selectedTransaction) {
      this.transactionService.update(this.selectedTransaction.id, request).subscribe({
        next: () => {
          this.messageService.add({
            severity: 'success',
            summary: 'Success',
            detail: 'Transaction updated successfully'
          });
          this.showDialog = false;
          this.loadTransactions();
        },
        error: (error) => {
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: error?.error?.message || 'Failed to update transaction'
          });
        }
      });
    } else {
      this.transactionService.create(request).subscribe({
        next: () => {
          this.messageService.add({
            severity: 'success',
            summary: 'Success',
            detail: 'Transaction created successfully'
          });
          this.showDialog = false;
          this.loadTransactions();
        },
        error: (error) => {
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: error?.error?.message || 'Failed to create transaction'
          });
        }
      });
    }
  }

  deleteTransaction(transaction: TransactionResponse): void {
    this.confirmationService.confirm({
      message: `Are you sure you want to delete the transaction "${transaction.description}"?`,
      header: 'Confirm Delete',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.transactionService.delete(transaction.id).subscribe({
          next: () => {
            this.messageService.add({
              severity: 'success',
              summary: 'Success',
              detail: 'Transaction deleted successfully'
            });
            this.loadTransactions();
          },
          error: (error) => {
            this.messageService.add({
              severity: 'error',
              summary: 'Error',
              detail: 'Failed to delete transaction'
            });
          }
        });
      }
    });
  }

  exportTransactions(): void {
    this.transactionService.exportTransactions(this.filteredTransactions);
    this.messageService.add({
      severity: 'success',
      summary: 'Success',
      detail: 'Transactions exported successfully'
    });
  }

  getCategoryColor(color: string): string {
    const colorMap: Record<string, string> = {
      'red': 'bg-red-100 text-red-600',
      'blue': 'bg-blue-100 text-blue-600',
      'green': 'bg-green-100 text-green-600',
      'yellow': 'bg-yellow-100 text-yellow-600',
      'purple': 'bg-purple-100 text-purple-600',
      'orange': 'bg-orange-100 text-orange-600',
      'pink': 'bg-pink-100 text-pink-600',
      'cyan': 'bg-cyan-100 text-cyan-600',
      'gray': 'bg-gray-100 text-gray-600'
    };
    return colorMap[color] || 'bg-gray-100 text-gray-600';
  }

  getAmountClass(amount: number, type: TransactionType): string {
    if (type === TransactionType.INCOME) {
      return 'text-green-600 font-bold';
    }
    return 'text-slate-900';
  }

  formatAmount(amount: number, type: TransactionType): string {
    const prefix = type === TransactionType.INCOME ? '+' : '-';
    return `${prefix}$${Math.abs(amount).toFixed(2)}`;
  }

  formatDate(date: Date | string): string {
    if (typeof date === 'string') {
      return date;
    }
    return date.toISOString().split('T')[0];
  }

  onPageChange(event: any): void {
    this.first = event.first;
    this.rows = event.rows;
  }
}
