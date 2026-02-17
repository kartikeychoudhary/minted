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
import { ColDef, GridApi, GridReadyEvent, GridOptions } from 'ag-grid-community';

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

  // AG Grid
  private gridApi!: GridApi;
  columnDefs: ColDef[] = [];
  defaultColDef: ColDef = {
    sortable: true,
    filter: false,
    resizable: true,
  };
  gridOptions: GridOptions = {
    pagination: true,
    paginationPageSize: 10,
    paginationPageSizeSelector: [10, 25, 50],
    domLayout: 'normal',
    rowSelection: 'multiple',
    suppressRowClickSelection: true,
    animateRows: true,
    rowHeight: 60,
    headerHeight: 48,
  };
  rowData: any[] = [];

  // Filter options
  selectedDateFilter: DateFilterOption = DateFilterOption.THIS_MONTH;
  selectedAccountId?: number;
  selectedCategoryId?: number;
  customStartDate?: Date;
  customEndDate?: Date;
  showCustomDatePickers = false;

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
  ) {
    this.setupGridColumns();
  }

  ngOnInit(): void {
    this.initForm();
    this.loadData();
  }

  setupGridColumns(): void {
    this.columnDefs = [
      {
        headerCheckboxSelection: true,
        checkboxSelection: true,
        width: 50,
        maxWidth: 50,
        sortable: false,
        headerClass: 'ag-header-checkbox',
        cellClass: 'ag-cell-checkbox'
      },
      {
        headerName: 'Date',
        field: 'transactionDate',
        width: 130,
        valueFormatter: (params) => {
          const date = new Date(params.value);
          return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
        },
        cellClass: 'text-sm text-slate-600'
      },
      {
        headerName: 'Category',
        field: 'categoryName',
        width: 200,
        cellRenderer: (params: any) => {
          const categoryIcon = params.data.categoryIcon || 'receipt_long';
          const categoryColor = this.getCategoryIconColor(params.data.categoryColor);
          return `
            <div class="flex items-center">
              <div class="flex-shrink-0 h-8 w-8 rounded-full ${categoryColor} flex items-center justify-center">
                <span class="material-icons-outlined text-sm">${categoryIcon}</span>
              </div>
              <div class="ml-3">
                <div class="text-sm font-medium text-slate-900">${params.value}</div>
              </div>
            </div>
          `;
        },
        sortable: false
      },
      {
        headerName: 'Description',
        field: 'description',
        flex: 1,
        minWidth: 250,
        cellClass: 'text-sm text-slate-900 font-medium'
      },
      {
        headerName: 'Account',
        field: 'accountName',
        width: 180,
        cellClass: 'text-sm text-slate-500'
      },
      {
        headerName: 'Amount',
        field: 'amount',
        width: 150,
        cellClass: (params) => {
          const baseClass = 'text-right text-sm font-medium';
          return params.data.type === 'INCOME'
            ? `${baseClass} text-success font-bold`
            : `${baseClass} text-slate-900`;
        },
        valueFormatter: (params) => {
          const prefix = params.data.type === 'INCOME' ? '+' : '-';
          return `${prefix}$${Math.abs(params.value).toFixed(2)}`;
        },
        headerClass: 'ag-header-right'
      },
      {
        headerName: '',
        field: 'actions',
        width: 80,
        sortable: false,
        cellRenderer: (params: any) => {
          return `
            <button class="edit-btn text-slate-400 hover:text-primary opacity-0 group-hover:opacity-100 transition-opacity" data-action="edit">
              <span class="material-icons-outlined text-lg">edit</span>
            </button>
          `;
        },
        cellClass: 'ag-cell-actions'
      }
    ];
  }

  onGridReady(params: GridReadyEvent): void {
    this.gridApi = params.api;
  }

  onCellClicked(event: any): void {
    if (event.event.target.dataset.action === 'edit') {
      this.openEditDialog(event.data);
    }
  }

  getCategoryIconColor(color: string): string {
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
          this.applyFilters();
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
          this.applyFilters();
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

  applyFilters(): void {
    let filtered = [...this.transactions];

    // Apply search filter
    if (this.searchTerm) {
      const term = this.searchTerm.toLowerCase();
      filtered = filtered.filter(t =>
        t.description.toLowerCase().includes(term) ||
        t.categoryName.toLowerCase().includes(term) ||
        t.accountName.toLowerCase().includes(term)
      );
    }

    this.filteredTransactions = filtered;
    this.rowData = filtered;
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
    this.applyFilters();
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

  formatDate(date: Date | string): string {
    if (typeof date === 'string') {
      return date;
    }
    return date.toISOString().split('T')[0];
  }
}
