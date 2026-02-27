export interface DashboardCard {
  id: number;
  title: string;
  chartType: string;
  xAxisMeasure: string;
  yAxisMeasure: string;
  filters: string | null;
  positionOrder: number;
  width: 'HALF' | 'FULL';
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface ChartDataset {
  label: string;
  data: number[];
  backgroundColor: string[];
}

export interface ChartDataResponse {
  labels: string[];
  datasets: ChartDataset[];
}

export interface AnalyticsSummary {
  totalIncome: number;
  totalExpense: number;
  netBalance: number;
  transactionCount: number;
}

export interface CategoryWise {
  categoryId: number;
  categoryName: string;
  icon: string;
  color: string;
  totalAmount: number;
  transactionCount: number;
  percentage: number;
}

export interface TrendData {
  month: string;
  income: number;
  expense: number;
  net: number;
}

export interface BudgetSummary {
  budgetId: number;
  budgetName: string;
  categoryName: string;
  budgetedAmount: number;
  spentAmount: number;
  remainingAmount: number;
  utilizationPercent: number;
}

export interface SpendingActivity {
  date: string;
  dayLabel: string;
  amount: number;
}

export interface TotalBalance {
  totalBalance: number;
  previousMonthBalance: number;
  incomeChangePercent: number;
  expenseChangePercent: number;
}
