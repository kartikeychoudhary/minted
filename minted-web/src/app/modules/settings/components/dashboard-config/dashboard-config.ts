import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { MessageService } from 'primeng/api';
import { DashboardConfigService } from '../../../../core/services/dashboard-config.service';
import { CategoryService } from '../../../../core/services/category.service';
import { CategoryResponse } from '../../../../core/models/category.model';

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

  constructor(
    private dashboardConfigService: DashboardConfigService,
    private categoryService: CategoryService,
    private messageService: MessageService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
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
}
