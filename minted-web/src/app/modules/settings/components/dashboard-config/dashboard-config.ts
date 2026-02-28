import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { MessageService } from 'primeng/api';
import { DashboardConfigService } from '../../../../core/services/dashboard-config.service';
import { CategoryService } from '../../../../core/services/category.service';
import { CategoryResponse } from '../../../../core/models/category.model';

interface ColorPreset {
  name: string;
  colors: string[];
}

@Component({
  selector: 'app-dashboard-config',
  standalone: false,
  templateUrl: './dashboard-config.html',
})
export class DashboardConfigComponent implements OnInit {
  categories: CategoryResponse[] = [];
  excludedCategoryIds: number[] = [];
  loading = false;
  saving = false;

  // Chart color palette
  chartColors: string[] = [];
  colorPresets: ColorPreset[] = [
    { name: 'Minted', colors: ['#c48821', '#22c55e', '#3b82f6', '#a855f7', '#ec4899', '#14b8a6', '#f97316', '#6366f1'] },
    { name: 'Pastel', colors: ['#fbbf24', '#86efac', '#93c5fd', '#c4b5fd', '#f9a8d4', '#99f6e4', '#fdba74', '#a5b4fc'] },
    { name: 'Vibrant', colors: ['#ef4444', '#f59e0b', '#10b981', '#3b82f6', '#8b5cf6', '#ec4899', '#06b6d4', '#84cc16'] },
    { name: 'Ocean', colors: ['#0ea5e9', '#06b6d4', '#14b8a6', '#0d9488', '#0284c7', '#0369a1', '#155e75', '#164e63'] },
    { name: 'Sunset', colors: ['#f97316', '#ef4444', '#e11d48', '#db2777', '#f59e0b', '#d97706', '#b91c1c', '#9f1239'] },
    { name: 'Forest', colors: ['#16a34a', '#15803d', '#166534', '#4ade80', '#84cc16', '#65a30d', '#a3e635', '#22d3ee'] },
    { name: 'Berry', colors: ['#8b5cf6', '#a855f7', '#d946ef', '#ec4899', '#f43f5e', '#6366f1', '#c084fc', '#f472b6'] },
    { name: 'Earth', colors: ['#92400e', '#b45309', '#a16207', '#854d0e', '#78716c', '#57534e', '#d97706', '#ca8a04'] },
    { name: 'Neon', colors: ['#22d3ee', '#a3e635', '#facc15', '#fb923c', '#f87171', '#e879f9', '#818cf8', '#34d399'] }
  ];

  constructor(
    private dashboardConfigService: DashboardConfigService,
    private categoryService: CategoryService,
    private messageService: MessageService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.chartColors = this.dashboardConfigService.getChartColors();
    this.loadData();
  }

  loadData(): void {
    this.loading = true;
    this.categoryService.getAll().subscribe({
      next: (categories) => {
        this.categories = categories;
        this.loadConfig();
      },
      error: () => {
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  loadConfig(): void {
    this.dashboardConfigService.getConfig().subscribe({
      next: (config) => {
        this.excludedCategoryIds = config.excludedCategoryIds || [];
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  saveConfig(): void {
    this.saving = true;
    this.dashboardConfigService.saveChartColors(this.chartColors);
    this.dashboardConfigService.saveConfig({ excludedCategoryIds: this.excludedCategoryIds }).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Success',
          detail: 'Dashboard configuration saved successfully'
        });
        this.saving = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to save dashboard configuration'
        });
        this.saving = false;
        this.cdr.detectChanges();
      }
    });
  }

  addColor(): void {
    this.chartColors.push('#94a3b8');
  }

  removeColor(index: number): void {
    this.chartColors.splice(index, 1);
  }

  applyPreset(colors: string[]): void {
    this.chartColors = [...colors];
  }
}
