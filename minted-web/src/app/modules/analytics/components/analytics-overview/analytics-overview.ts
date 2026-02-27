import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { AnalyticsService } from '../../../../core/services/analytics.service';
import { TransactionService } from '../../../../core/services/transaction.service';
import { RecurringTransactionService } from '../../../../core/services/recurring.service';
import { AuthService } from '../../../../core/services/auth.service';
import { CategoryService } from '../../../../core/services/category.service';
import { AccountService } from '../../../../core/services/account.service';
import { AnalyticsSummary, BudgetSummary, SpendingActivity } from '../../../../core/models/dashboard.model';
import { TransactionResponse } from '../../../../core/models/transaction.model';
import { RecurringTransaction, RecurringSummary } from '../../../../core/models/recurring.model';
import { CategoryResponse } from '../../../../core/models/category.model';
import { AccountResponse } from '../../../../core/models/account.model';
import { User } from '../../../../core/models/user.model';
import { CurrencyService } from '../../../../core/services/currency.service';

interface RecurringGroup {
    category: string;
    total: number;
    items: RecurringTransaction[];
    expanded: boolean;
}

@Component({
    selector: 'app-analytics-overview',
    standalone: false,
    templateUrl: './analytics-overview.html',
    styleUrls: ['./analytics-overview.scss']
})
export class AnalyticsOverview implements OnInit, OnDestroy {
    private destroy$ = new Subject<void>();

    // User data
    currentUser?: User;

    // Summary cards data
    summary: AnalyticsSummary | null = null;

    // Spending activity chart data
    spendingActivity: SpendingActivity[] = [];
    spendingChartData: any = {};
    spendingChartOptions: any = {};
    selectedBarIndex: number | null = null;

    // All transactions for the current period (used for filtering)
    private allPeriodTransactions: TransactionResponse[] = [];

    // Recent transactions (displayed)
    recentTransactions: TransactionResponse[] = [];

    // Recurring payments
    recurringTransactions: RecurringTransaction[] = [];
    recurringGroups: RecurringGroup[] = [];
    recurringSummary: RecurringSummary | null = null;

    // Budget summaries
    budgetSummaries: BudgetSummary[] = [];

    // Filters
    monthOptions: { label: string; value: number }[] = [
        { label: 'January', value: 0 }, { label: 'February', value: 1 },
        { label: 'March', value: 2 }, { label: 'April', value: 3 },
        { label: 'May', value: 4 }, { label: 'June', value: 5 },
        { label: 'July', value: 6 }, { label: 'August', value: 7 },
        { label: 'September', value: 8 }, { label: 'October', value: 9 },
        { label: 'November', value: 10 }, { label: 'December', value: 11 }
    ];
    yearOptions: { label: string; value: number }[] = [];
    selectedMonth: number = new Date().getMonth();
    selectedYear: number = new Date().getFullYear();
    categoryOptions: CategoryResponse[] = [];
    accountOptions: AccountResponse[] = [];
    selectedCategoryId: number | null = null;
    selectedAccountId: number | null = null;

    // Loading states
    loading = {
        summary: false,
        spending: false,
        transactions: false,
        recurring: false,
        budgets: false
    };

    constructor(
        private analyticsService: AnalyticsService,
        private transactionService: TransactionService,
        private recurringService: RecurringTransactionService,
        private authService: AuthService,
        private categoryService: CategoryService,
        private accountService: AccountService,
        private cdr: ChangeDetectorRef,
        public currencyService: CurrencyService
    ) {}

    ngOnInit(): void {
        this.loadCurrentUser();
        this.initYearOptions();
        this.initSpendingChartOptions();
        this.loadFilterOptions();
        this.loadAllData();
    }

    ngOnDestroy(): void {
        this.destroy$.next();
        this.destroy$.complete();
    }

    private loadCurrentUser(): void {
        this.currentUser = this.authService.currentUserValue || undefined;
    }

    private initYearOptions(): void {
        const currentYear = new Date().getFullYear();
        for (let y = currentYear; y >= currentYear - 3; y--) {
            this.yearOptions.push({ label: y.toString(), value: y });
        }
    }

    private loadFilterOptions(): void {
        this.categoryService.getAll().pipe(takeUntil(this.destroy$)).subscribe({
            next: (data) => { this.categoryOptions = data; this.cdr.detectChanges(); }
        });
        this.accountService.getAll().pipe(takeUntil(this.destroy$)).subscribe({
            next: (data) => { this.accountOptions = data; this.cdr.detectChanges(); }
        });
    }

    private getDateRangeForFilters(): { startDate: string; endDate: string } {
        const start = new Date(this.selectedYear, this.selectedMonth, 1);
        const end = new Date(this.selectedYear, this.selectedMonth + 1, 0);
        return { startDate: this.formatDate(start), endDate: this.formatDate(end) };
    }

    onFilterChange(): void {
        this.selectedBarIndex = null;
        this.loadAllData();
    }

    private loadAllData(): void {
        this.loadSummary();
        this.loadSpendingActivity();
        this.loadRecentTransactions();
        this.loadRecurringTransactions();
        this.loadBudgetSummary();
    }

    private loadSummary(): void {
        this.loading.summary = true;
        const { startDate, endDate } = this.getDateRangeForFilters();

        this.analyticsService.getSummary(startDate, endDate)
            .pipe(takeUntil(this.destroy$))
            .subscribe({
                next: (data) => {
                    this.summary = data;
                    this.loading.summary = false;
                    this.cdr.detectChanges();
                },
                error: () => {
                    this.loading.summary = false;
                    this.cdr.detectChanges();
                }
            });
    }

    private loadSpendingActivity(): void {
        this.loading.spending = true;
        const { startDate, endDate } = this.getDateRangeForFilters();

        this.analyticsService.getSpendingActivity(startDate, endDate)
            .pipe(takeUntil(this.destroy$))
            .subscribe({
                next: (data) => {
                    this.spendingActivity = data;
                    this.buildSpendingChart();
                    this.loading.spending = false;
                    this.cdr.detectChanges();
                },
                error: () => {
                    this.loading.spending = false;
                    this.cdr.detectChanges();
                }
            });
    }

    private loadRecentTransactions(): void {
        this.loading.transactions = true;
        const { startDate, endDate } = this.getDateRangeForFilters();

        this.transactionService.getByDateRange(startDate, endDate)
            .pipe(takeUntil(this.destroy$))
            .subscribe({
                next: (data) => {
                    this.allPeriodTransactions = data;
                    this.applyTransactionFilters();
                    this.loading.transactions = false;
                    this.cdr.detectChanges();
                },
                error: () => {
                    this.loading.transactions = false;
                    this.cdr.detectChanges();
                }
            });
    }

    private applyTransactionFilters(): void {
        let filtered = [...this.allPeriodTransactions];

        if (this.selectedCategoryId) {
            filtered = filtered.filter(t => t.categoryId === this.selectedCategoryId);
        }
        if (this.selectedAccountId) {
            filtered = filtered.filter(t => t.accountId === this.selectedAccountId);
        }

        // If a bar is clicked, filter to that specific day
        if (this.selectedBarIndex !== null && this.spendingActivity[this.selectedBarIndex]) {
            const selectedDate = this.spendingActivity[this.selectedBarIndex].date;
            filtered = filtered.filter(t => t.transactionDate === selectedDate);
        }

        this.recentTransactions = filtered;
    }

    clearBarSelection(): void {
        this.selectedBarIndex = null;
        this.buildSpendingChart();
        this.applyTransactionFilters();
        this.cdr.detectChanges();
    }

    onBarClick(event: any): void {
        if (!event || event.element === undefined) return;

        const index = event.element.index;
        if (this.selectedBarIndex === index) {
            // Deselect
            this.selectedBarIndex = null;
        } else {
            this.selectedBarIndex = index;
        }
        this.buildSpendingChart();
        this.applyTransactionFilters();
        this.cdr.detectChanges();
    }

    private buildSpendingChart(): void {
        const labels = this.spendingActivity.map(s => s.dayLabel);
        const data = this.spendingActivity.map(s => s.amount);
        const accentColor = getComputedStyle(document.documentElement).getPropertyValue('--minted-accent').trim() || '#c48821';

        const bgColors = data.map((_, i) => {
            if (this.selectedBarIndex !== null) {
                return i === this.selectedBarIndex ? accentColor : (accentColor + '40');
            }
            return accentColor;
        });

        this.spendingChartData = {
            labels,
            datasets: [{
                label: 'Spending',
                data,
                backgroundColor: bgColors,
                borderRadius: 6,
                barThickness: 28,
                maxBarThickness: 40
            }]
        };
    }

    private initSpendingChartOptions(): void {
        const baseFont = { family: "'Inter', sans-serif" };
        this.spendingChartOptions = {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { display: false },
                tooltip: {
                    backgroundColor: '#1e293b',
                    titleFont: { ...baseFont, size: 13 },
                    bodyFont: { ...baseFont, size: 12 },
                    padding: 12,
                    cornerRadius: 8,
                    callbacks: {
                        label: (ctx: any) => this.currencyService.format(ctx.parsed.y || 0)
                    }
                }
            },
            scales: {
                x: {
                    grid: { display: false },
                    ticks: { font: { ...baseFont, size: 11 }, color: '#94a3b8' }
                },
                y: {
                    grid: { color: '#f1f5f9' },
                    ticks: {
                        font: { ...baseFont, size: 11 },
                        color: '#94a3b8',
                        callback: (val: number) => this.currencyService.format(val)
                    }
                }
            }
        };
    }

    private loadRecurringTransactions(): void {
        this.loading.recurring = true;

        this.recurringService.getAll()
            .pipe(takeUntil(this.destroy$))
            .subscribe({
                next: (data) => {
                    this.recurringTransactions = data;
                    this.groupRecurringTransactions();
                    this.loading.recurring = false;
                    this.cdr.detectChanges();
                },
                error: () => {
                    this.loading.recurring = false;
                    this.cdr.detectChanges();
                }
            });

        this.recurringService.getSummary()
            .pipe(takeUntil(this.destroy$))
            .subscribe({
                next: (data) => {
                    this.recurringSummary = data;
                    this.cdr.detectChanges();
                }
            });
    }

    private groupRecurringTransactions(): void {
        const groups = new Map<string, RecurringTransaction[]>();

        this.recurringTransactions.forEach(rt => {
            const category = this.categorizeRecurring(rt.categoryName);
            if (!groups.has(category)) {
                groups.set(category, []);
            }
            groups.get(category)!.push(rt);
        });

        this.recurringGroups = Array.from(groups.entries()).map(([category, items]) => ({
            category,
            total: items.reduce((sum, item) => sum + item.amount, 0),
            items,
            expanded: category === 'Subscriptions'
        }));
    }

    private categorizeRecurring(categoryName: string): string {
        const lower = categoryName.toLowerCase();
        if (lower.includes('subscription') || lower.includes('streaming') || lower.includes('software')) {
            return 'Subscriptions';
        } else if (lower.includes('utility') || lower.includes('electricity') || lower.includes('water') || lower.includes('gas')) {
            return 'Utilities';
        } else if (lower.includes('loan') || lower.includes('mortgage') || lower.includes('emi')) {
            return 'Loans';
        }
        return 'Other';
    }

    toggleRecurringGroup(group: RecurringGroup): void {
        group.expanded = !group.expanded;
    }

    getTransactionIcon(categoryIcon: string): string {
        return categoryIcon || 'receipt_long';
    }

    getTransactionColor(type: string): string {
        return type === 'INCOME' ? 'emerald' : 'red';
    }

    getStatusBadgeClass(status: string): string {
        return status === 'ACTIVE' ? 'bg-green-100 text-green-600' : 'bg-amber-100 text-amber-600';
    }

    formatCurrency(amount: number): string {
        return this.currencyService.format(amount);
    }

    formatDate(date: Date): string {
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        return `${year}-${month}-${day}`;
    }

    formatTransactionDate(dateStr: string): string {
        const date = new Date(dateStr);
        const options: Intl.DateTimeFormatOptions = { month: 'short', day: 'numeric', year: 'numeric' };
        return date.toLocaleDateString('en-US', options);
    }

    pauseRecurring(item: RecurringTransaction): void {
        this.recurringService.toggleStatus(item.id)
            .pipe(takeUntil(this.destroy$))
            .subscribe({
                next: () => { this.loadRecurringTransactions(); }
            });
    }

    editRecurring(item: RecurringTransaction): void {
        console.log('Edit recurring transaction:', item);
    }

    addRecurring(): void {
        console.log('Add recurring transaction');
    }

    private loadBudgetSummary(): void {
        this.loading.budgets = true;
        this.analyticsService.getBudgetSummary()
            .pipe(takeUntil(this.destroy$))
            .subscribe({
                next: (data) => {
                    this.budgetSummaries = data;
                    this.loading.budgets = false;
                    this.cdr.detectChanges();
                },
                error: () => {
                    this.loading.budgets = false;
                    this.cdr.detectChanges();
                }
            });
    }

    getBudgetStatusColor(percent: number): string {
        if (percent > 90) return 'var(--minted-danger)';
        if (percent > 70) return 'var(--minted-warning)';
        return 'var(--minted-success)';
    }

    getBudgetStatusBg(percent: number): string {
        if (percent > 90) return 'var(--minted-danger-subtle)';
        if (percent > 70) return 'var(--minted-warning-subtle)';
        return 'var(--minted-success-subtle)';
    }

    getBudgetStatusLabel(percent: number): string {
        if (percent > 90) return 'Over';
        if (percent > 70) return 'Warning';
        return 'On Track';
    }

    getSelectedDateLabel(): string | null {
        if (this.selectedBarIndex !== null && this.spendingActivity[this.selectedBarIndex]) {
            return this.formatTransactionDate(this.spendingActivity[this.selectedBarIndex].date);
        }
        return null;
    }
}
