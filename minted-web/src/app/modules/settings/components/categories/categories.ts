import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MessageService, ConfirmationService } from 'primeng/api';
import { CategoryService } from '../../../../core/services/category.service';
import { CategoryResponse, CategoryRequest, TransactionType } from '../../../../core/models/category.model';

@Component({
  selector: 'app-categories',
  standalone: false,
  templateUrl: './categories.html',
  styleUrl: './categories.scss',
})
export class Categories implements OnInit {
  categories: CategoryResponse[] = [];
  displayDialog = false;
  categoryForm?: FormGroup;
  isEditMode = false;
  selectedCategoryId?: number;
  loading = false;
  selectedIcon: any = null;
  duplicateError = '';

  // Transaction type options
  transactionTypes = [
    { label: 'Income', value: TransactionType.INCOME },
    { label: 'Expense', value: TransactionType.EXPENSE },
    { label: 'Transfer', value: TransactionType.TRANSFER }
  ];

  // Icon options with colors
  iconOptions = [
    { label: 'Restaurant', value: 'restaurant', icon: 'pi pi-gift', color: 'red' },
    { label: 'Shopping Bag', value: 'shopping_bag', icon: 'pi pi-shopping-bag', color: 'blue' },
    { label: 'Car', value: 'directions_car', icon: 'pi pi-car', color: 'green' },
    { label: 'Movie', value: 'movie', icon: 'pi pi-video', color: 'purple' },
    { label: 'Home', value: 'home', icon: 'pi pi-home', color: 'amber' },
    { label: 'Health', value: 'local_hospital', icon: 'pi pi-heart', color: 'pink' },
    { label: 'Education', value: 'school', icon: 'pi pi-book', color: 'indigo' },
    { label: 'Travel', value: 'flight', icon: 'pi pi-send', color: 'teal' },
    { label: 'Utilities', value: 'bolt', icon: 'pi pi-bolt', color: 'orange' },
    { label: 'Salary', value: 'attach_money', icon: 'pi pi-dollar', color: 'green' },
    { label: 'Gift', value: 'card_giftcard', icon: 'pi pi-gift', color: 'pink' },
    { label: 'Investment', value: 'trending_up', icon: 'pi pi-chart-line', color: 'blue' }
  ];

  // Color options
  colorOptions = [
    { label: 'Red', value: 'red', class: 'bg-red-100 text-red-600' },
    { label: 'Blue', value: 'blue', class: 'bg-blue-100 text-blue-600' },
    { label: 'Green', value: 'green', class: 'bg-green-100 text-green-600' },
    { label: 'Purple', value: 'purple', class: 'bg-purple-100 text-purple-600' },
    { label: 'Amber', value: 'amber', class: 'bg-amber-100 text-amber-600' },
    { label: 'Pink', value: 'pink', class: 'bg-pink-100 text-pink-600' },
    { label: 'Indigo', value: 'indigo', class: 'bg-indigo-100 text-indigo-600' },
    { label: 'Teal', value: 'teal', class: 'bg-teal-100 text-teal-600' },
    { label: 'Orange', value: 'orange', class: 'bg-orange-100 text-orange-600' }
  ];

  constructor(
    private categoryService: CategoryService,
    private formBuilder: FormBuilder,
    private messageService: MessageService,
    private confirmationService: ConfirmationService,
    private cdr: ChangeDetectorRef
  ) { }

  ngOnInit(): void {
    this.initForm();
    setTimeout(() => {
      this.loadCategories();
    }, 0);
  }

  initForm(): void {
    this.categoryForm = this.formBuilder.group({
      name: ['', [Validators.required, Validators.maxLength(100)]],
      type: [TransactionType.EXPENSE, Validators.required],
      icon: ['restaurant'],
      color: ['red']
    });
  }

  loadCategories(): void {
    this.loading = true;
    console.log('Loading categories...');

    this.categoryService.getAll().subscribe({
      next: (data) => {
        console.log('Categories loaded successfully:', data);
        this.categories = data;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error loading categories:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: error?.message || 'Failed to load categories'
        });
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  showAddDialog(): void {
    this.isEditMode = false;
    this.selectedCategoryId = undefined;
    this.selectedIcon = this.iconOptions[0];
    this.categoryForm?.reset({
      type: TransactionType.EXPENSE,
      icon: 'restaurant',
      color: 'red'
    });
    this.duplicateError = '';
    this.displayDialog = true;
  }

  showEditDialog(category: CategoryResponse): void {
    this.isEditMode = true;
    this.selectedCategoryId = category.id;
    this.selectedIcon = this.iconOptions.find(opt => opt.value === category.icon) || null;
    this.categoryForm?.patchValue({
      name: category.name,
      type: category.type,
      icon: category.icon,
      color: category.color
    });
    this.duplicateError = '';
    this.displayDialog = true;
  }

  saveCategory(): void {
    if (this.categoryForm?.invalid) {
      return;
    }

    this.duplicateError = '';
    const request: CategoryRequest = this.categoryForm!.value;

    if (this.isEditMode && this.selectedCategoryId) {
      this.categoryService.update(this.selectedCategoryId, request).subscribe({
        next: () => {
          this.messageService.add({
            severity: 'success',
            summary: 'Success',
            detail: 'Category updated successfully'
          });
          this.loadCategories();
          this.displayDialog = false;
        },
        error: (error) => {
          if (error?.status === 409) {
            this.duplicateError = error.error?.message || 'A category with this name already exists.';
          } else {
            this.messageService.add({
              severity: 'error',
              summary: 'Error',
              detail: error?.error?.message || error?.message || 'Failed to update category'
            });
          }
        }
      });
    } else {
      this.categoryService.create(request).subscribe({
        next: () => {
          this.messageService.add({
            severity: 'success',
            summary: 'Success',
            detail: 'Category created successfully'
          });
          this.loadCategories();
          this.displayDialog = false;
        },
        error: (error) => {
          if (error?.status === 409) {
            this.duplicateError = error.error?.message || 'A category with this name already exists.';
          } else {
            this.messageService.add({
              severity: 'error',
              summary: 'Error',
              detail: error?.error?.message || error?.message || 'Failed to create category'
            });
          }
        }
      });
    }
  }

  confirmDelete(category: CategoryResponse): void {
    this.confirmationService.confirm({
      message: `Are you sure you want to delete ${category.name}?`,
      header: 'Delete Confirmation',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.deleteCategory(category.id);
      }
    });
  }

  deleteCategory(id: number): void {
    this.categoryService.delete(id).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Success',
          detail: 'Category deleted successfully'
        });
        this.loadCategories();
      },
      error: (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: error?.message || 'Failed to delete category'
        });
      }
    });
  }

  getIconBackgroundClass(color: string): string {
    const colorMap: { [key: string]: string } = {
      'red': 'bg-red-100 text-red-600',
      'blue': 'bg-blue-100 text-blue-600',
      'green': 'bg-green-100 text-green-600',
      'purple': 'bg-purple-100 text-purple-600',
      'amber': 'bg-amber-100 text-amber-600',
      'pink': 'bg-pink-100 text-pink-600',
      'indigo': 'bg-indigo-100 text-indigo-600',
      'teal': 'bg-teal-100 text-teal-600',
      'orange': 'bg-orange-100 text-orange-600'
    };
    return colorMap[color] || 'bg-gray-100 text-gray-600';
  }

  getIconClass(iconValue: string): string {
    if (!iconValue || typeof iconValue !== 'string') {
      return 'pi pi-tag';
    }

    const iconOption = this.iconOptions.find(opt => opt.value === iconValue);
    if (iconOption) {
      return iconOption.icon;
    }

    if (iconValue.startsWith('pi pi-')) {
      return iconValue;
    }

    // Map Material Icons to PrimeIcons
    const iconMap: { [key: string]: string } = {
      'restaurant': 'pi pi-gift',
      'shopping_bag': 'pi pi-shopping-bag',
      'directions_car': 'pi pi-car',
      'movie': 'pi pi-video',
      'home': 'pi pi-home',
      'local_hospital': 'pi pi-heart',
      'school': 'pi pi-book',
      'flight': 'pi pi-send',
      'bolt': 'pi pi-bolt',
      'attach_money': 'pi pi-dollar',
      'card_giftcard': 'pi pi-gift',
      'trending_up': 'pi pi-chart-line'
    };

    return iconMap[iconValue] || 'pi pi-tag';
  }

  getTransactionTypeLabel(type: TransactionType): string {
    const typeMap: { [key: string]: string } = {
      [TransactionType.INCOME]: 'Income',
      [TransactionType.EXPENSE]: 'Expense',
      [TransactionType.TRANSFER]: 'Transfer'
    };
    return typeMap[type] || type;
  }

  onIconChange(event: any): void {
    const selectedValue = event.value;
    this.selectedIcon = this.iconOptions.find(opt => opt.value === selectedValue) || null;

    // Auto-update color based on icon's default color
    if (this.selectedIcon && this.selectedIcon.color) {
      this.categoryForm?.patchValue({
        color: this.selectedIcon.color
      });
    }

    console.log('Icon changed:', this.selectedIcon);
  }
}
