import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';
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
import { ColDef, GridApi, GridReadyEvent, GridOptions, themeQuartz } from 'ag-grid-community';
import { CategoryCellRendererComponent } from '../cell-renderers/category-cell-renderer.component';
import { ActionsCellRendererComponent } from '../cell-renderers/actions-cell-renderer.component';
import { CurrencyService } from '../../../../core/services/currency.service';

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

  // AG Grid v35 Theming API
  mintedTheme = themeQuartz.withParams({
    backgroundColor: 'var(--minted-bg-card)',
    foregroundColor: 'var(--minted-text-primary)',
    borderColor: 'var(--minted-border)',
    browserColorScheme: 'inherit',
    headerBackgroundColor: 'var(--minted-bg-card)',
    headerFontSize: 12,
    headerFontWeight: 600,
    headerTextColor: 'var(--minted-text-muted)',
    oddRowBackgroundColor: 'var(--minted-bg-card)',
    rowHoverColor: 'var(--minted-bg-hover)',
    selectedRowBackgroundColor: 'var(--minted-accent-subtle)',
    accentColor: 'var(--minted-accent)',
    fontFamily: '"Inter", -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif',
    fontSize: 14,
    rowHeight: 60,
    headerHeight: 48,
    spacing: 6,
    wrapperBorderRadius: 8,
    cellHorizontalPadding: 16,
    headerColumnBorder: false,
    headerColumnResizeHandleColor: 'transparent',
    columnBorder: false,
    rowBorder: { color: 'var(--minted-border-light)', width: 1, style: 'solid' },
    checkboxCheckedBackgroundColor: 'var(--minted-accent)',
    checkboxCheckedBorderColor: 'var(--minted-accent)',
    checkboxUncheckedBackgroundColor: 'transparent',
    checkboxUncheckedBorderColor: 'var(--minted-border)',
  });

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
    overlayNoRowsTemplate: '<span class="ag-overlay-no-rows-center">No transactions found. Add a transaction to get started.</span>',
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
    private cdr: ChangeDetectorRef,
    public currencyService: CurrencyService,
    private router: Router
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
        cellClass: 'cell-v-center',
        valueFormatter: (params) => {
          const date = new Date(params.value);
          return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
        }
      },
      {
        headerName: 'Category',
        field: 'categoryName',
        width: 220,
        cellClass: 'cell-v-center',
        cellRenderer: CategoryCellRendererComponent,
        sortable: false
      },
      {
        headerName: 'Description',
        field: 'description',
        flex: 1,
        minWidth: 250,
        cellClass: 'cell-v-center'
      },
      {
        headerName: 'Account',
        field: 'accountName',
        width: 180,
        cellClass: 'cell-v-center'
      },
      {
        headerName: 'Amount',
        field: 'amount',
        width: 150,
        cellClass: (params) => {
          const baseClass = 'cell-v-center';
          const colorClass = params.data.type === 'INCOME'
            ? 'text-success font-bold'
            : 'font-bold';
          return `${baseClass} ${colorClass}`;
        },
        valueFormatter: (params) => {
          const prefix = params.data.type === 'INCOME' ? '+' : '-';
          return `${prefix}${this.currencyService.format(Math.abs(params.value))}`;
        }
      },
      {
        headerName: '',
        field: 'actions',
        width: 120,
        sortable: false,
        cellRenderer: ActionsCellRendererComponent,
        cellRendererParams: {
          callbacks: {
            onEdit: (data: any) => this.openEditDialog(data),
            onDelete: (data: any) => this.deleteTransaction(data),
            onSplit: (data: any) => this.splitTransaction(data)
          }
        },
        cellClass: 'ag-cell-actions'
      }
    ];
  }

  onGridReady(params: GridReadyEvent): void {
    this.gridApi = params.api;
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

    // Apply account filter
    if (this.selectedAccountId) {
      filtered = filtered.filter(t => t.accountId === this.selectedAccountId);
    }

    // Apply category filter
    if (this.selectedCategoryId) {
      filtered = filtered.filter(t => t.categoryId === this.selectedCategoryId);
    }

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
    this.cdr.detectChanges();
  }

  onDateFilterChange(filter: DateFilterOption): void {
    this.selectedDateFilter = filter;
    this.showCustomDatePickers = filter === DateFilterOption.CUSTOM;

    // Trigger change detection to show/hide custom date pickers immediately
    this.cdr.detectChanges();

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
    this.applyFilters();
  }

  onCategoryFilterChange(): void {
    this.applyFilters();
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
      key: 'transactions',
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

  splitTransaction(transaction: TransactionResponse): void {
    this.router.navigate(['/splits'], {
      queryParams: {
        sourceTransactionId: transaction.id,
        description: transaction.description,
        categoryName: transaction.categoryName,
        totalAmount: transaction.amount,
        transactionDate: transaction.transactionDate
      }
    });
  }
}
