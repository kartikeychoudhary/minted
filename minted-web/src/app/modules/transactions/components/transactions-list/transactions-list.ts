import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { MessageService, ConfirmationService } from 'primeng/api';
import { TransactionService } from '../../../../core/services/transaction.service';
import { CategoryService } from '../../../../core/services/category.service';
import { AccountService } from '../../../../core/services/account.service';
import { FriendService } from '../../../../core/services/friend.service';
import { SplitService } from '../../../../core/services/split.service';
import {
  TransactionRequest,
  TransactionResponse,
  DateFilterOption,
  TransactionFilters
} from '../../../../core/models/transaction.model';
import { CategoryResponse, TransactionType } from '../../../../core/models/category.model';
import { AccountResponse } from '../../../../core/models/account.model';
import { FriendResponse } from '../../../../core/models/friend.model';
import {
  SplitTransactionRequest,
  SplitShareRequest,
  SplitType
} from '../../../../core/models/split.model';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ColDef, GridApi, GridReadyEvent, GridOptions, themeQuartz } from 'ag-grid-community';
import { CategoryCellRendererComponent } from '../cell-renderers/category-cell-renderer.component';
import { ActionsCellRendererComponent } from '../cell-renderers/actions-cell-renderer.component';
import { CurrencyService } from '../../../../core/services/currency.service';

interface SplitFriendEntry {
  friendId: number | null;
  friendName: string;
  avatarColor: string;
  shareAmount: number;
  sharePercentage: number | null;
  isPayer: boolean;
}

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
    // Pagination button styling via AG Grid theming API
    iconButtonHoverBackgroundColor: 'var(--minted-bg-hover)',
    iconButtonHoverColor: 'var(--minted-accent)',
    iconButtonBorderRadius: 4,
    iconSize: 16,
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

  // Bulk operations
  selectedTransactions: TransactionResponse[] = [];
  showBulkCategoryDialog = false;
  bulkCategoryId?: number;

  // Expose enums to template
  DateFilterOption = DateFilterOption;

  // Computed options for dropdowns
  accountOptions: { label: string; value: number | undefined }[] = [];
  categoryOptions: { label: string; value: number | undefined }[] = [];
  toAccountOptions: { label: string; value: number | null }[] = [];

  // Split dialog state
  showSplitDialog = false;
  splitForm?: FormGroup;
  friends: FriendResponse[] = [];
  splitFriendEntries: SplitFriendEntry[] = [];
  selectedSplitType: SplitType = 'EQUAL';
  availableFriendsForSplit: FriendResponse[] = [];
  avatarColors = ['#6366f1', '#ec4899', '#f59e0b', '#10b981', '#3b82f6', '#8b5cf6', '#ef4444', '#14b8a6'];

  constructor(
    private transactionService: TransactionService,
    private categoryService: CategoryService,
    private accountService: AccountService,
    private friendService: FriendService,
    private splitService: SplitService,
    private messageService: MessageService,
    private confirmationService: ConfirmationService,
    private formBuilder: FormBuilder,
    private cdr: ChangeDetectorRef,
    public currencyService: CurrencyService
  ) {
    this.setupGridColumns();
  }

  ngOnInit(): void {
    this.initForm();
    this.initSplitForm();
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
        sort: 'desc',
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
        width: 220,
        minWidth: 120,
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
        width: 160,
        minWidth: 160,
        maxWidth: 160,
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

  onSelectionChanged(): void {
    if (this.gridApi) {
      this.selectedTransactions = this.gridApi.getSelectedRows();
      this.cdr.detectChanges();
    }
  }

  bulkDelete(): void {
    if (this.selectedTransactions.length === 0) return;
    this.confirmationService.confirm({
      key: 'transactions',
      message: `Are you sure you want to delete ${this.selectedTransactions.length} selected transaction(s)?`,
      header: 'Confirm Bulk Delete',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        const ids = this.selectedTransactions.map(t => t.id);
        this.transactionService.bulkDelete(ids).subscribe({
          next: () => {
            this.messageService.add({
              severity: 'success',
              summary: 'Success',
              detail: `${ids.length} transaction(s) deleted successfully`
            });
            this.selectedTransactions = [];
            this.loadTransactions();
          },
          error: () => {
            this.messageService.add({
              severity: 'error',
              summary: 'Error',
              detail: 'Failed to delete transactions'
            });
          }
        });
      }
    });
  }

  openBulkCategoryDialog(): void {
    if (this.selectedTransactions.length === 0) return;
    this.bulkCategoryId = undefined;
    this.showBulkCategoryDialog = true;
  }

  saveBulkCategory(): void {
    if (!this.bulkCategoryId || this.selectedTransactions.length === 0) return;
    const ids = this.selectedTransactions.map(t => t.id);
    this.transactionService.bulkUpdateCategory(ids, this.bulkCategoryId).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Success',
          detail: `Category updated for ${ids.length} transaction(s)`
        });
        this.showBulkCategoryDialog = false;
        this.selectedTransactions = [];
        this.loadTransactions();
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to update categories'
        });
      }
    });
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
      tags: ['', Validators.maxLength(500)],
      excludeFromAnalysis: [false]
    });
  }

  loadData(): void {
    this.loading = true;
    Promise.all([
      this.loadCategories(),
      this.loadAccounts(),
      this.loadTransactions(),
      this.loadFriends()
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
      case DateFilterOption.LAST_6_MONTHS:
        startDate = new Date(today.getFullYear(), today.getMonth() - 6, 1);
        break;
      case DateFilterOption.LAST_YEAR:
        startDate = new Date(today.getFullYear() - 1, today.getMonth(), today.getDate());
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
      tags: transaction.tags,
      excludeFromAnalysis: transaction.excludeFromAnalysis || false
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
      tags: formValue.tags,
      excludeFromAnalysis: formValue.excludeFromAnalysis
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

  // ── Split Dialog Methods ──

  initSplitForm(): void {
    this.splitForm = this.formBuilder.group({
      sourceTransactionId: [null],
      description: ['', [Validators.required, Validators.maxLength(500)]],
      categoryName: ['', [Validators.required, Validators.maxLength(100)]],
      totalAmount: [null, [Validators.required, Validators.min(0.01)]],
      transactionDate: [new Date(), Validators.required]
    });
  }

  async loadFriends(): Promise<void> {
    this.friendService.getAll().subscribe({
      next: (data) => {
        this.friends = data;
        this.cdr.detectChanges();
      },
      error: () => {
        this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Failed to load friends' });
      }
    });
  }

  splitTransaction(transaction: TransactionResponse): void {
    this.splitForm?.patchValue({
      sourceTransactionId: transaction.id,
      description: transaction.description,
      categoryName: transaction.categoryName,
      totalAmount: transaction.amount,
      transactionDate: new Date(transaction.transactionDate)
    });
    this.selectedSplitType = 'EQUAL';
    this.splitFriendEntries = [
      { friendId: null, friendName: 'Me', avatarColor: '#22c55e', shareAmount: 0, sharePercentage: null, isPayer: true }
    ];
    this.updateAvailableFriends();
    this.recalculateShares();
    this.showSplitDialog = true;
  }

  addFriendToSplit(friend: FriendResponse): void {
    if (this.splitFriendEntries.some(e => e.friendId === friend.id)) return;
    this.splitFriendEntries.push({
      friendId: friend.id,
      friendName: friend.name,
      avatarColor: friend.avatarColor,
      shareAmount: 0,
      sharePercentage: null,
      isPayer: false
    });
    this.updateAvailableFriends();
    this.recalculateShares();
  }

  removeFriendFromSplit(index: number): void {
    if (this.splitFriendEntries[index].friendId === null) return;
    this.splitFriendEntries.splice(index, 1);
    this.updateAvailableFriends();
    this.recalculateShares();
  }

  updateAvailableFriends(): void {
    const usedIds = new Set(this.splitFriendEntries.filter(e => e.friendId !== null).map(e => e.friendId));
    this.availableFriendsForSplit = this.friends.filter(f => !usedIds.has(f.id));
  }

  onSplitTypeChange(type: SplitType): void {
    this.selectedSplitType = type;
    this.recalculateShares();
  }

  recalculateShares(): void {
    const totalAmount = this.splitForm?.get('totalAmount')?.value || 0;
    const count = this.splitFriendEntries.length;
    if (count === 0) return;

    if (this.selectedSplitType === 'EQUAL') {
      const equalAmount = Math.floor((totalAmount / count) * 100) / 100;
      const remainder = Math.round((totalAmount - equalAmount * count) * 100) / 100;
      this.splitFriendEntries.forEach((entry, i) => {
        entry.shareAmount = i === 0 ? equalAmount + remainder : equalAmount;
        entry.sharePercentage = null;
      });
    } else if (this.selectedSplitType === 'SHARE') {
      // Distribute by equal percentage, user can then edit
      const equalPct = Math.floor((100 / count) * 100) / 100;
      const pctRemainder = Math.round((100 - equalPct * count) * 100) / 100;
      this.splitFriendEntries.forEach((entry, i) => {
        const pct = i === 0 ? equalPct + pctRemainder : equalPct;
        entry.sharePercentage = pct;
        entry.shareAmount = Math.round((totalAmount * pct / 100) * 100) / 100;
      });
    }
    // UNEQUAL: leave amounts as-is for manual entry (no auto-recalculation)
    this.cdr.detectChanges();
  }

  onTotalAmountChange(): void {
    this.recalculateShares();
  }

  onSharePercentageChange(index: number): void {
    const totalAmount = this.splitForm?.get('totalAmount')?.value || 0;
    const entry = this.splitFriendEntries[index];
    const pct = entry.sharePercentage || 0;
    entry.shareAmount = Math.round((totalAmount * pct / 100) * 100) / 100;
    this.cdr.detectChanges();
  }

  getSplitTotal(): number {
    return this.splitFriendEntries.reduce((sum, e) => sum + (e.shareAmount || 0), 0);
  }

  saveSplit(): void {
    if (this.splitForm?.invalid) {
      Object.keys(this.splitForm.controls).forEach(key => {
        this.splitForm?.get(key)?.markAsTouched();
      });
      return;
    }

    if (this.splitFriendEntries.length < 2) {
      this.messageService.add({ severity: 'warn', summary: 'Warning', detail: 'Add at least one friend to split with' });
      return;
    }

    const totalAmount = this.splitForm!.value.totalAmount || 0;
    const splitTotal = this.getSplitTotal();
    if (Math.abs(splitTotal - totalAmount) > 0.01) {
      this.messageService.add({ severity: 'warn', summary: 'Warning', detail: 'Split amounts must add up to the total amount' });
      return;
    }

    const formValue = this.splitForm!.value;
    const shares: SplitShareRequest[] = this.splitFriendEntries.map(entry => ({
      friendId: entry.friendId,
      shareAmount: entry.shareAmount,
      sharePercentage: entry.sharePercentage || undefined,
      isPayer: entry.isPayer
    }));

    const request: SplitTransactionRequest = {
      sourceTransactionId: formValue.sourceTransactionId,
      description: formValue.description,
      categoryName: formValue.categoryName,
      totalAmount: formValue.totalAmount,
      splitType: this.selectedSplitType,
      transactionDate: this.formatDate(formValue.transactionDate),
      shares
    };

    this.splitService.create(request).subscribe({
      next: () => {
        this.messageService.add({ severity: 'success', summary: 'Success', detail: 'Split created' });
        this.showSplitDialog = false;
        this.loadTransactions();
      },
      error: (err) => {
        this.messageService.add({ severity: 'error', summary: 'Error', detail: err?.error?.message || 'Failed to create split' });
      }
    });
  }

  onSplitDialogHide(): void {
    this.splitForm?.reset({ transactionDate: new Date() });
    this.splitFriendEntries = [];
    this.selectedSplitType = 'EQUAL';
    this.availableFriendsForSplit = [...this.friends];
  }

  getInitials(name: string): string {
    if (!name) return '?';
    const parts = name.trim().split(' ');
    if (parts.length >= 2) {
      return (parts[0][0] + parts[1][0]).toUpperCase();
    }
    return name.substring(0, 2).toUpperCase();
  }
}
