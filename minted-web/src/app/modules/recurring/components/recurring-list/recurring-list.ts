import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { MessageService, ConfirmationService } from 'primeng/api';
import { RecurringTransactionService } from '../../../../core/services/recurring.service';
import { AccountService } from '../../../../core/services/account.service';
import { CategoryService } from '../../../../core/services/category.service';
import { RecurringTransaction, RecurringTransactionRequest, RecurringSummary } from '../../../../core/models/recurring.model';
import { AccountResponse } from '../../../../core/models/account.model';
import { CategoryResponse } from '../../../../core/models/category.model';
import { CurrencyService } from '../../../../core/services/currency.service';

@Component({
    selector: 'app-recurring-list',
    standalone: false,
    templateUrl: './recurring-list.html',
    styleUrls: ['./recurring-list.scss'],
    providers: [MessageService, ConfirmationService]
})
export class RecurringList implements OnInit, OnDestroy {
    transactions: RecurringTransaction[] = [];
    filteredTransactions: RecurringTransaction[] = [];
    summary: RecurringSummary | null = null;
    accounts: AccountResponse[] = [];
    categories: CategoryResponse[] = [];

    // Form
    formData: RecurringTransactionRequest = this.resetForm();
    selectedType: 'EXPENSE' | 'INCOME' = 'EXPENSE';
    editing = false;
    editingId: number | null = null;

    // UI state
    loading = true;
    submitting = false;
    searchQuery = '';

    private destroy$ = new Subject<void>();

    constructor(
        private recurringService: RecurringTransactionService,
        private accountService: AccountService,
        private categoryService: CategoryService,
        private messageService: MessageService,
        private confirmationService: ConfirmationService,
        private cdr: ChangeDetectorRef,
        public currencyService: CurrencyService
    ) { }

    ngOnInit(): void {
        this.loadDropdownData();
        this.loadData();
    }

    ngOnDestroy(): void {
        this.destroy$.next();
        this.destroy$.complete();
    }

    loadDropdownData(): void {
        this.accountService.getAll().pipe(takeUntil(this.destroy$)).subscribe({
            next: (data) => this.accounts = data
        });
        this.categoryService.getAll().pipe(takeUntil(this.destroy$)).subscribe({
            next: (data) => this.categories = data
        });
    }

    loadData(): void {
        this.loading = true;
        this.recurringService.getAll().pipe(takeUntil(this.destroy$)).subscribe({
            next: (data) => {
                this.transactions = data;
                this.applyFilter();
                this.loading = false;
                this.cdr.detectChanges();
            },
            error: () => {
                this.transactions = [];
                this.filteredTransactions = [];
                this.loading = false;
                this.cdr.detectChanges();
            }
        });

        this.recurringService.getSummary().pipe(takeUntil(this.destroy$)).subscribe({
            next: (data) => {
                this.summary = data;
                this.cdr.detectChanges();
            }
        });
    }

    onSearch(): void {
        this.applyFilter();
    }

    applyFilter(): void {
        if (!this.searchQuery.trim()) {
            this.filteredTransactions = [...this.transactions];
        } else {
            const q = this.searchQuery.toLowerCase();
            this.filteredTransactions = this.transactions.filter(t =>
                t.name.toLowerCase().includes(q) ||
                t.categoryName.toLowerCase().includes(q)
            );
        }
    }

    selectType(type: 'EXPENSE' | 'INCOME'): void {
        this.selectedType = type;
        this.formData = { ...this.formData, type };
    }

    onSubmit(): void {
        if (!this.formData.name || !this.formData.amount || !this.formData.categoryId || !this.formData.accountId || !this.formData.startDate) {
            this.messageService.add({ severity: 'warn', summary: 'Validation', detail: 'Please fill all required fields' });
            return;
        }

        this.submitting = true;
        const request = { ...this.formData, type: this.selectedType };

        const operation = this.editing && this.editingId
            ? this.recurringService.update(this.editingId, request)
            : this.recurringService.create(request);

        operation.pipe(takeUntil(this.destroy$)).subscribe({
            next: () => {
                this.messageService.add({
                    severity: 'success',
                    summary: 'Success',
                    detail: this.editing ? 'Schedule updated' : 'Schedule created'
                });
                this.resetFormState();
                this.loadData();
                this.submitting = false;
                this.cdr.detectChanges();
            },
            error: (err) => {
                this.messageService.add({ severity: 'error', summary: 'Error', detail: err?.error?.message || 'Operation failed' });
                this.submitting = false;
                this.cdr.detectChanges();
            }
        });
    }

    editTransaction(tx: RecurringTransaction): void {
        this.editing = true;
        this.editingId = tx.id;
        this.selectedType = tx.type;
        this.formData = {
            name: tx.name,
            amount: tx.amount,
            type: tx.type,
            categoryId: tx.categoryId,
            accountId: tx.accountId,
            frequency: tx.frequency,
            dayOfMonth: tx.dayOfMonth,
            startDate: tx.startDate,
            endDate: tx.endDate
        };
    }

    cancelEdit(): void {
        this.resetFormState();
    }

    toggleStatus(tx: RecurringTransaction): void {
        this.recurringService.toggleStatus(tx.id).pipe(takeUntil(this.destroy$)).subscribe({
            next: () => {
                this.messageService.add({
                    severity: 'info',
                    summary: 'Status Changed',
                    detail: `${tx.name} ${tx.status === 'ACTIVE' ? 'paused' : 'resumed'}`
                });
                this.loadData();
            }
        });
    }

    confirmDelete(tx: RecurringTransaction): void {
        this.confirmationService.confirm({
            key: 'recurring',
            message: `Are you sure you want to delete "${tx.name}"?`,
            header: 'Delete Recurring Transaction',
            icon: 'pi pi-exclamation-triangle',
            acceptButtonStyleClass: 'p-button-danger',
            accept: () => {
                this.recurringService.delete(tx.id).pipe(takeUntil(this.destroy$)).subscribe({
                    next: () => {
                        this.messageService.add({ severity: 'success', summary: 'Deleted', detail: `${tx.name} deleted` });
                        this.loadData();
                    }
                });
            }
        });
    }

    formatCurrency(value: number): string {
        return this.currencyService.format(value);
    }

    getDaysUntil(dateStr: string): string {
        if (!dateStr) return '';
        const target = new Date(dateStr);
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        target.setHours(0, 0, 0, 0);
        const diffDays = Math.ceil((target.getTime() - today.getTime()) / (1000 * 60 * 60 * 24));
        if (diffDays === 0) return 'Today';
        if (diffDays === 1) return 'Tomorrow';
        if (diffDays < 0) return `${Math.abs(diffDays)} days ago`;
        return `In ${diffDays} days`;
    }

    formatDate(dateStr: string): string {
        if (!dateStr) return '';
        return new Date(dateStr).toLocaleDateString('en-US', { month: 'short', day: '2-digit', year: 'numeric' });
    }

    private resetForm(): RecurringTransactionRequest {
        return {
            name: '',
            amount: 0,
            type: 'EXPENSE',
            categoryId: 0,
            accountId: 0,
            frequency: 'MONTHLY',
            dayOfMonth: 1,
            startDate: '',
            endDate: null
        };
    }

    private resetFormState(): void {
        this.editing = false;
        this.editingId = null;
        this.selectedType = 'EXPENSE';
        this.formData = this.resetForm();
    }
}
