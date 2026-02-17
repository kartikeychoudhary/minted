import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { AuthService } from '../../../../core/services/auth.service';
import { AnalyticsService } from '../../../../core/services/analytics.service';
import { DashboardService } from '../../../../core/services/dashboard.service';
import { AnalyticsSummary, CategoryWise, TrendData, DashboardCard } from '../../../../core/models/dashboard.model';

interface PeriodOption {
  label: string;
  value: string;
  startDate: string;
  endDate: string;
}

@Component({
  selector: 'app-home',
  standalone: false,
  templateUrl: './home.html',
  styleUrls: ['./home.scss']
})
export class Home implements OnInit, OnDestroy {
  currentUser: any;
  summary: AnalyticsSummary | null = null;
  categoryData: CategoryWise[] = [];
  trendData: TrendData[] = [];
  dashboardCards: DashboardCard[] = [];

  // Loading states
  loadingSummary = true;
  loadingCategories = true;
  loadingTrend = true;

  // Period selection
  periods: PeriodOption[] = [];
  selectedPeriod: PeriodOption | null = null;

  // Chart configurations
  barChartData: any = {};
  barChartOptions: any = {};
  doughnutChartData: any = {};
  doughnutChartOptions: any = {};
  lineChartData: any = {};
  lineChartOptions: any = {};

  private destroy$ = new Subject<void>();

  constructor(
    private authService: AuthService,
    private analyticsService: AnalyticsService,
    private dashboardService: DashboardService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) { }

  ngOnInit(): void {
    this.authService.currentUser$.pipe(takeUntil(this.destroy$)).subscribe(user => {
      this.currentUser = user;
    });

    this.initPeriods();
    this.initChartOptions();
    this.loadDashboardData();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  onPeriodChange(): void {
    this.loadDashboardData();
  }

  private initPeriods(): void {
    const now = new Date();
    const year = now.getFullYear();
    const month = now.getMonth();

    // This Month
    const thisMonthStart = new Date(year, month, 1);
    const thisMonthEnd = new Date(year, month + 1, 0);

    // Last Month
    const lastMonthStart = new Date(year, month - 1, 1);
    const lastMonthEnd = new Date(year, month, 0);

    // Last 3 Months
    const last3Start = new Date(year, month - 2, 1);

    // Last 6 Months
    const last6Start = new Date(year, month - 5, 1);

    // This Year
    const thisYearStart = new Date(year, 0, 1);

    this.periods = [
      { label: 'This Month', value: 'THIS_MONTH', startDate: this.formatDate(thisMonthStart), endDate: this.formatDate(thisMonthEnd) },
      { label: 'Last Month', value: 'LAST_MONTH', startDate: this.formatDate(lastMonthStart), endDate: this.formatDate(lastMonthEnd) },
      { label: 'Last 3 Months', value: 'LAST_3_MONTHS', startDate: this.formatDate(last3Start), endDate: this.formatDate(thisMonthEnd) },
      { label: 'Last 6 Months', value: 'LAST_6_MONTHS', startDate: this.formatDate(last6Start), endDate: this.formatDate(thisMonthEnd) },
      { label: 'This Year', value: 'THIS_YEAR', startDate: this.formatDate(thisYearStart), endDate: this.formatDate(thisMonthEnd) }
    ];

    this.selectedPeriod = this.periods[0]; // Default: This Month
  }

  private loadDashboardData(): void {
    if (!this.selectedPeriod) return;

    const { startDate, endDate } = this.selectedPeriod;

    // Load summary
    this.loadingSummary = true;
    this.analyticsService.getSummary(startDate, endDate)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data) => {
          this.summary = data;
          this.loadingSummary = false;
          this.cdr.detectChanges();
        },
        error: () => {
          this.summary = { totalIncome: 0, totalExpense: 0, netBalance: 0, transactionCount: 0 };
          this.loadingSummary = false;
          this.cdr.detectChanges();
        }
      });

    // Load category-wise
    this.loadingCategories = true;
    this.analyticsService.getCategoryWise(startDate, endDate, 'EXPENSE')
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data) => {
          this.categoryData = data;
          this.loadingCategories = false;
          this.buildDoughnutChart();
          this.buildBarChart();
          this.cdr.detectChanges();
        },
        error: () => {
          this.categoryData = [];
          this.loadingCategories = false;
          this.buildDoughnutChart();
          this.buildBarChart();
          this.cdr.detectChanges();
        }
      });

    // Load trend
    this.loadingTrend = true;
    this.analyticsService.getTrend(6)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data) => {
          this.trendData = data;
          this.loadingTrend = false;
          this.buildLineChart();
          this.cdr.detectChanges();
        },
        error: () => {
          this.trendData = [];
          this.loadingTrend = false;
          this.buildLineChart();
          this.cdr.detectChanges();
        }
      });
  }

  private buildBarChart(): void {
    const topCategories = this.categoryData.slice(0, 8);
    this.barChartData = {
      labels: topCategories.map(c => c.categoryName),
      datasets: [
        {
          label: 'Expenses',
          data: topCategories.map(c => c.totalAmount),
          backgroundColor: topCategories.map(c => c.color || '#c48821'),
          borderRadius: 6,
          barThickness: 28
        }
      ]
    };
  }

  private buildDoughnutChart(): void {
    const topCategories = this.categoryData.slice(0, 6);
    this.doughnutChartData = {
      labels: topCategories.map(c => c.categoryName),
      datasets: [
        {
          data: topCategories.map(c => c.totalAmount),
          backgroundColor: topCategories.map(c => c.color || '#94a3b8'),
          borderWidth: 2,
          borderColor: '#ffffff',
          hoverOffset: 8
        }
      ]
    };
  }

  private buildLineChart(): void {
    const monthLabels = this.trendData.map(t => {
      const [year, month] = t.month.split('-');
      const date = new Date(parseInt(year), parseInt(month) - 1);
      return date.toLocaleString('default', { month: 'short' });
    });

    this.lineChartData = {
      labels: monthLabels,
      datasets: [
        {
          label: 'Income',
          data: this.trendData.map(t => t.income),
          borderColor: '#22c55e',
          backgroundColor: 'rgba(34, 197, 94, 0.08)',
          fill: true,
          tension: 0.4,
          pointRadius: 4,
          pointHoverRadius: 7,
          pointBackgroundColor: '#22c55e',
          borderWidth: 2.5
        },
        {
          label: 'Expenses',
          data: this.trendData.map(t => t.expense),
          borderColor: '#c48821',
          backgroundColor: 'rgba(196, 136, 33, 0.08)',
          fill: true,
          tension: 0.4,
          pointRadius: 4,
          pointHoverRadius: 7,
          pointBackgroundColor: '#c48821',
          borderWidth: 2.5
        }
      ]
    };
  }

  private initChartOptions(): void {
    const baseFont = { family: "'Inter', sans-serif" };

    this.barChartOptions = {
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
            label: (ctx: any) => `₹${ctx.parsed.y?.toLocaleString('en-IN') || 0}`
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
            callback: (val: number) => `₹${(val / 1000).toFixed(0)}k`
          }
        }
      }
    };

    this.doughnutChartOptions = {
      responsive: true,
      maintainAspectRatio: false,
      cutout: '65%',
      plugins: {
        legend: {
          position: 'bottom',
          labels: {
            font: { ...baseFont, size: 11 },
            color: '#64748b',
            padding: 12,
            usePointStyle: true,
            pointStyleWidth: 10
          }
        },
        tooltip: {
          backgroundColor: '#1e293b',
          titleFont: { ...baseFont, size: 13 },
          bodyFont: { ...baseFont, size: 12 },
          padding: 12,
          cornerRadius: 8,
          callbacks: {
            label: (ctx: any) => ` ₹${ctx.parsed?.toLocaleString('en-IN') || 0}`
          }
        }
      }
    };

    this.lineChartOptions = {
      responsive: true,
      maintainAspectRatio: false,
      interaction: { intersect: false, mode: 'index' },
      plugins: {
        legend: {
          position: 'top',
          align: 'end',
          labels: {
            font: { ...baseFont, size: 12 },
            color: '#64748b',
            padding: 20,
            usePointStyle: true,
            pointStyleWidth: 10
          }
        },
        tooltip: {
          backgroundColor: '#1e293b',
          titleFont: { ...baseFont, size: 13 },
          bodyFont: { ...baseFont, size: 12 },
          padding: 12,
          cornerRadius: 8,
          callbacks: {
            label: (ctx: any) => ` ${ctx.dataset.label}: ₹${ctx.parsed.y?.toLocaleString('en-IN') || 0}`
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
            callback: (val: number) => `₹${(val / 1000).toFixed(0)}k`
          }
        }
      }
    };
  }

  formatCurrency(value: number): string {
    if (value === null || value === undefined) return '₹0';
    return '₹' + value.toLocaleString('en-IN', { maximumFractionDigits: 0 });
  }

  getGreeting(): string {
    const hour = new Date().getHours();
    if (hour < 12) return 'Good Morning';
    if (hour < 17) return 'Good Afternoon';
    return 'Good Evening';
  }

  private formatDate(date: Date): string {
    return date.toISOString().split('T')[0];
  }
}
