export interface RecurringTransaction {
    id: number;
    name: string;
    amount: number;
    type: 'INCOME' | 'EXPENSE';
    categoryId: number;
    categoryName: string;
    categoryIcon: string;
    categoryColor: string;
    accountId: number;
    accountName: string;
    frequency: 'MONTHLY';
    dayOfMonth: number;
    startDate: string;
    endDate: string | null;
    status: 'ACTIVE' | 'PAUSED';
    nextExecutionDate: string;
    createdAt: string;
    updatedAt: string;
}

export interface RecurringTransactionRequest {
    name: string;
    amount: number;
    type: string;
    categoryId: number;
    accountId: number;
    frequency: string;
    dayOfMonth: number;
    startDate: string;
    endDate: string | null;
}

export interface RecurringSummary {
    estimatedMonthlyExpenses: number;
    estimatedMonthlyIncome: number;
    scheduledNetFlux: number;
    activeCount: number;
    pausedCount: number;
}
