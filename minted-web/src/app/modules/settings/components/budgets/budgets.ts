import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MessageService, ConfirmationService } from 'primeng/api';
import { BudgetService } from '../../../../core/services/budget.service';
import { CategoryService } from '../../../../core/services/category.service';
import { BudgetResponse, BudgetRequest } from '../../../../core/models/budget.model';
import { CategoryResponse } from '../../../../core/models/category.model';
import { CurrencyService } from '../../../../core/services/currency.service';

@Component({
  selector: 'app-budgets',
  standalone: false,
  templateUrl: './budgets.html',
  styleUrl: './budgets.scss',
})
export class Budgets implements OnInit {
  budgets: BudgetResponse[] = [];
  categories: CategoryResponse[] = [];
  displayDialog = false;
  budgetForm?: FormGroup;
  isEditMode = false;
  selectedBudgetId?: number;
  loading = false;
  duplicateError = '';

  // Month options
  monthOptions = [
    { label: 'January', value: 1 },
    { label: 'February', value: 2 },
    { label: 'March', value: 3 },
    { label: 'April', value: 4 },
    { label: 'May', value: 5 },
    { label: 'June', value: 6 },
    { label: 'July', value: 7 },
    { label: 'August', value: 8 },
    { label: 'September', value: 9 },
    { label: 'October', value: 10 },
    { label: 'November', value: 11 },
    { label: 'December', value: 12 }
  ];

  // Year options (current year and next 2 years)
  yearOptions: { label: string; value: number }[] = [];

  constructor(
    private budgetService: BudgetService,
    private categoryService: CategoryService,
    private formBuilder: FormBuilder,
    private messageService: MessageService,
    private confirmationService: ConfirmationService,
    private cdr: ChangeDetectorRef,
    public currencyService: CurrencyService
  ) {
    // Generate year options
    const currentYear = new Date().getFullYear();
    for (let i = 0; i < 3; i++) {
      const year = currentYear + i;
      this.yearOptions.push({ label: year.toString(), value: year });
    }
  }

  ngOnInit(): void {
    this.initForm();
    setTimeout(() => {
      this.loadBudgets();
      this.loadCategories();
    }, 0);
  }

  initForm(): void {
    const currentDate = new Date();
    this.budgetForm = this.formBuilder.group({
      name: ['', [Validators.required, Validators.maxLength(100)]],
      amount: [0, [Validators.required, Validators.min(0.01)]],
      month: [currentDate.getMonth() + 1, Validators.required],
      year: [currentDate.getFullYear(), Validators.required],
      categoryId: [null]
    });
  }

  loadBudgets(): void {
    this.loading = true;
    console.log('Loading budgets...');

    this.budgetService.getAll().subscribe({
      next: (data) => {
        console.log('Budgets loaded successfully:', data);
        this.budgets = data;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error loading budgets:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: error?.message || 'Failed to load budgets'
        });
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  loadCategories(): void {
    this.categoryService.getAll().subscribe({
      next: (data) => {
        console.log('Categories loaded for dropdown:', data);
        this.categories = data;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error loading categories:', error);
      }
    });
  }

  showAddDialog(): void {
    this.isEditMode = false;
    this.selectedBudgetId = undefined;
    const currentDate = new Date();
    this.budgetForm?.reset({
      amount: 0,
      month: currentDate.getMonth() + 1,
      year: currentDate.getFullYear(),
      categoryId: null
    });
    this.duplicateError = '';
    this.displayDialog = true;
  }

  showEditDialog(budget: BudgetResponse): void {
    this.isEditMode = true;
    this.selectedBudgetId = budget.id;
    this.budgetForm?.patchValue({
      name: budget.name,
      amount: budget.amount,
      month: budget.month,
      year: budget.year,
      categoryId: budget.categoryId
    });
    this.duplicateError = '';
    this.displayDialog = true;
  }

  saveBudget(): void {
    if (this.budgetForm?.invalid) {
      return;
    }

    this.duplicateError = '';
    const request: BudgetRequest = this.budgetForm!.value;

    if (this.isEditMode && this.selectedBudgetId) {
      this.budgetService.update(this.selectedBudgetId, request).subscribe({
        next: () => {
          this.messageService.add({
            severity: 'success',
            summary: 'Success',
            detail: 'Budget updated successfully'
          });
          this.loadBudgets();
          this.displayDialog = false;
        },
        error: (error) => {
          if (error?.status === 409) {
            this.duplicateError = error.error?.message || 'A budget for this category and period already exists.';
          } else {
            this.messageService.add({
              severity: 'error',
              summary: 'Error',
              detail: error?.error?.message || error?.message || 'Failed to update budget'
            });
          }
        }
      });
    } else {
      this.budgetService.create(request).subscribe({
        next: () => {
          this.messageService.add({
            severity: 'success',
            summary: 'Success',
            detail: 'Budget created successfully'
          });
          this.loadBudgets();
          this.displayDialog = false;
        },
        error: (error) => {
          if (error?.status === 409) {
            this.duplicateError = error.error?.message || 'A budget for this category and period already exists.';
          } else {
            this.messageService.add({
              severity: 'error',
              summary: 'Error',
              detail: error?.error?.message || error?.message || 'Failed to create budget'
            });
          }
        }
      });
    }
  }

  confirmDelete(budget: BudgetResponse): void {
    this.confirmationService.confirm({
      key: 'budgets',
      message: `Are you sure you want to delete budget for ${budget.name}?`,
      header: 'Delete Confirmation',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.deleteBudget(budget.id);
      }
    });
  }

  deleteBudget(id: number): void {
    this.budgetService.delete(id).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Success',
          detail: 'Budget deleted successfully'
        });
        this.loadBudgets();
      },
      error: (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: error?.message || 'Failed to delete budget'
        });
      }
    });
  }

  getCategoryColor(categoryName: string | null): string {
    if (!categoryName) return 'bg-gray-100 text-gray-600';

    // Simple hash function to get consistent color based on category name
    let hash = 0;
    for (let i = 0; i < categoryName.length; i++) {
      hash = categoryName.charCodeAt(i) + ((hash << 5) - hash);
    }

    const colors = [
      'bg-red-100 text-red-600',
      'bg-blue-100 text-blue-600',
      'bg-green-100 text-green-600',
      'bg-purple-100 text-purple-600',
      'bg-amber-100 text-amber-600',
      'bg-pink-100 text-pink-600',
      'bg-indigo-100 text-indigo-600',
      'bg-teal-100 text-teal-600',
      'bg-orange-100 text-orange-600'
    ];

    return colors[Math.abs(hash) % colors.length];
  }

  getMonthName(month: number): string {
    const monthOption = this.monthOptions.find(m => m.value === month);
    return monthOption ? monthOption.label : month.toString();
  }

  formatCurrency(amount: number): string {
    return `$${amount.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
  }

  getBudgetDescription(budget: BudgetResponse): string {
    return `${this.formatCurrency(budget.amount)} per month`;
  }

  getCategoryIcon(categoryName: string | null): string {
    if (!categoryName) return 'pi pi-tag';

    // Map common category names to icons
    const iconMap: { [key: string]: string } = {
      'dining': 'pi pi-gift',
      'food': 'pi pi-gift',
      'restaurant': 'pi pi-gift',
      'transportation': 'pi pi-car',
      'car': 'pi pi-car',
      'groceries': 'pi pi-shopping-bag',
      'shopping': 'pi pi-shopping-bag',
      'entertainment': 'pi pi-video',
      'movie': 'pi pi-video',
      'home': 'pi pi-home',
      'housing': 'pi pi-home',
      'health': 'pi pi-heart',
      'utilities': 'pi pi-bolt',
      'education': 'pi pi-book',
      'travel': 'pi pi-send'
    };

    const lowerName = categoryName.toLowerCase();
    for (const key in iconMap) {
      if (lowerName.includes(key)) {
        return iconMap[key];
      }
    }

    return 'pi pi-tag';
  }
}
