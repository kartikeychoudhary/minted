import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { AnalyticsService } from '../../../../core/services/analytics.service';
import { TransactionService } from '../../../../core/services/transaction.service';
import { RecurringTransactionService } from '../../../../core/services/recurring.service';
import { AuthService } from '../../../../core/services/auth.service';
import { AnalyticsSummary, SpendingActivity, TotalBalance } from '../../../../core/models/dashboard.model';
import { TransactionResponse } from '../../../../core/models/transaction.model';
import { RecurringTransaction, RecurringSummary } from '../../../../core/models/recurring.model';
import { User } from '../../../../core/models/user.model';
import { CurrencyService } from '../../../../core/services/currency.service';

interface RecurringGroup {
    category: string;
    total: number;
    items: RecurringTransaction[];
    expanded: boolean;
}

interface SpendingPeriod {
    label: string;
    value: string;
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
    totalBalance: TotalBalance | null = null;
    summary: AnalyticsSummary | null = null;

    // Spending activity chart data
    spendingActivity: SpendingActivity[] = [];
    selectedPeriod: SpendingPeriod = { label: 'This Week', value: 'THIS_WEEK' };
    spendingPeriods: SpendingPeriod[] = [
        { label: 'This Week', value: 'THIS_WEEK' },
        { label: 'Last Week', value: 'LAST_WEEK' },
        { label: 'This Month', value: 'THIS_MONTH' }
    ];
    maxSpendingAmount = 0;

    // Recent transactions
    recentTransactions: TransactionResponse[] = [];

    // Recurring payments
    recurringTransactions: RecurringTransaction[] = [];
    recurringGroups: RecurringGroup[] = [];
    recurringSummary: RecurringSummary | null = null;

    // Loading states
    loading = {
        summary: false,
        spending: false,
        transactions: false,
        recurring: false
    };

    constructor(
        private analyticsService: AnalyticsService,
        private transactionService: TransactionService,
        private recurringService: RecurringTransactionService,
        private authService: AuthService,
        private cdr: ChangeDetectorRef,
        public currencyService: CurrencyService
    ) {}

    ngOnInit(): void {
        this.loadCurrentUser();
        this.loadAllData();
    }

    ngOnDestroy(): void {
        this.destroy$.next();
        this.destroy$.complete();
    }

    private loadCurrentUser(): void {
        this.currentUser = this.authService.currentUserValue || undefined;
    }

    private loadAllData(): void {
        this.loadTotalBalance();
        this.loadSummary();
        this.loadSpendingActivity();
        this.loadRecentTransactions();
        this.loadRecurringTransactions();
    }

    private loadTotalBalance(): void {
        this.loading.summary = true;
        this.analyticsService.getTotalBalance()
            .pipe(takeUntil(this.destroy$))
            .subscribe({
                next: (data) => {
                    this.totalBalance = data;
                    this.loading.summary = false;
                    this.cdr.detectChanges();
                },
                error: (err) => {
                    console.error('Error loading total balance:', err);
                    this.loading.summary = false;
                }
            });
    }

    private loadSummary(): void {
        const today = new Date();
        const firstDayOfMonth = new Date(today.getFullYear(), today.getMonth(), 1);
        const lastDayOfMonth = new Date(today.getFullYear(), today.getMonth() + 1, 0);

        const startDate = this.formatDate(firstDayOfMonth);
        const endDate = this.formatDate(lastDayOfMonth);

        this.analyticsService.getSummary(startDate, endDate)
            .pipe(takeUntil(this.destroy$))
            .subscribe({
                next: (data) => {
                    this.summary = data;
                    this.cdr.detectChanges();
                },
                error: (err) => {
                    console.error('Error loading summary:', err);
                }
            });
    }

    private loadSpendingActivity(): void {
        this.loading.spending = true;
        const { startDate, endDate } = this.getDateRangeForPeriod(this.selectedPeriod.value);

        this.analyticsService.getSpendingActivity(startDate, endDate)
            .pipe(takeUntil(this.destroy$))
            .subscribe({
                next: (data) => {
                    this.spendingActivity = data;
                    this.maxSpendingAmount = Math.max(...data.map(d => d.amount), 0);
                    this.loading.spending = false;
                    this.cdr.detectChanges();
                },
                error: (err) => {
                    console.error('Error loading spending activity:', err);
                    this.loading.spending = false;
                }
            });
    }

    private loadRecentTransactions(): void {
        this.loading.transactions = true;
        const endDate = this.formatDate(new Date());
        const startDate = this.formatDate(new Date(Date.now() - 30 * 24 * 60 * 60 * 1000)); // Last 30 days

        this.transactionService.getByDateRange(startDate, endDate)
            .pipe(takeUntil(this.destroy$))
            .subscribe({
                next: (data) => {
                    this.recentTransactions = data.slice(0, 5); // Show only 5 most recent
                    this.loading.transactions = false;
                    this.cdr.detectChanges();
                },
                error: (err) => {
                    console.error('Error loading recent transactions:', err);
                    this.loading.transactions = false;
                }
            });
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
                error: (err) => {
                    console.error('Error loading recurring transactions:', err);
                    this.loading.recurring = false;
                }
            });

        this.recurringService.getSummary()
            .pipe(takeUntil(this.destroy$))
            .subscribe({
                next: (data) => {
                    this.recurringSummary = data;
                    this.cdr.detectChanges();
                },
                error: (err) => {
                    console.error('Error loading recurring summary:', err);
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
            expanded: category === 'Subscriptions' // Expand subscriptions by default
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
        } else {
            return 'Other';
        }
    }

    onPeriodChange(): void {
        this.loadSpendingActivity();
    }

    toggleRecurringGroup(group: RecurringGroup): void {
        group.expanded = !group.expanded;
    }

    getBarHeight(amount: number): number {
        if (this.maxSpendingAmount === 0) return 0;
        return (amount / this.maxSpendingAmount) * 100;
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

    getChangeIcon(percent: number): string {
        return percent >= 0 ? 'trending_up' : 'trending_down';
    }

    getChangeClass(percent: number): string {
        return percent >= 0 ? 'text-green-500' : 'text-red-500';
    }

    formatCurrency(amount: number): string {
        return this.currencyService.format(amount);
    }

    formatDate(date: Date): string {
        return date.toISOString().split('T')[0];
    }

    formatTransactionDate(dateStr: string): string {
        const date = new Date(dateStr);
        const options: Intl.DateTimeFormatOptions = { month: 'short', day: 'numeric', year: 'numeric' };
        return date.toLocaleDateString('en-US', options);
    }

    private getDateRangeForPeriod(period: string): { startDate: string; endDate: string } {
        const today = new Date();
        let startDate: Date;
        let endDate: Date = today;

        switch (period) {
            case 'THIS_WEEK':
                const dayOfWeek = today.getDay();
                const diff = dayOfWeek === 0 ? 6 : dayOfWeek - 1; // Monday as first day
                startDate = new Date(today);
                startDate.setDate(today.getDate() - diff);
                break;
            case 'LAST_WEEK':
                const lastWeekEnd = new Date(today);
                const lastWeekDayOfWeek = today.getDay();
                const lastWeekDiff = lastWeekDayOfWeek === 0 ? 6 : lastWeekDayOfWeek - 1;
                lastWeekEnd.setDate(today.getDate() - lastWeekDiff - 1); // Last Sunday
                startDate = new Date(lastWeekEnd);
                startDate.setDate(lastWeekEnd.getDate() - 6);
                endDate = lastWeekEnd;
                break;
            case 'THIS_MONTH':
                startDate = new Date(today.getFullYear(), today.getMonth(), 1);
                endDate = new Date(today.getFullYear(), today.getMonth() + 1, 0);
                break;
            default:
                startDate = new Date(today);
                startDate.setDate(today.getDate() - 7);
        }

        return {
            startDate: this.formatDate(startDate),
            endDate: this.formatDate(endDate)
        };
    }

    pauseRecurring(item: RecurringTransaction): void {
        this.recurringService.toggleStatus(item.id)
            .pipe(takeUntil(this.destroy$))
            .subscribe({
                next: () => {
                    this.loadRecurringTransactions();
                },
                error: (err) => {
                    console.error('Error toggling recurring transaction:', err);
                }
            });
    }

    editRecurring(item: RecurringTransaction): void {
        // TODO: Open edit dialog
        console.log('Edit recurring transaction:', item);
    }

    addRecurring(): void {
        // TODO: Open add dialog
        console.log('Add recurring transaction');
    }

    getGreeting(): string {
        const hour = new Date().getHours();
        if (hour < 12) return 'morning';
        if (hour < 18) return 'afternoon';
        return 'evening';
    }
}

