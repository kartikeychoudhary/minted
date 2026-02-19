import { Component } from '@angular/core';
import { ICellRendererAngularComp } from 'ag-grid-angular';
import { ICellRendererParams } from 'ag-grid-community';

@Component({
  selector: 'app-category-cell-renderer',
  standalone: false,
  template: `
    <div class="flex items-center gap-3">
      <div
        class="flex-shrink-0 h-9 w-9 rounded-full flex items-center justify-center"
        [ngClass]="getCategoryColorClass()">
        <span class="material-icons-outlined text-base">{{ params.data.categoryIcon || 'receipt_long' }}</span>
      </div>
      <div>
      <div class="text-sm font-medium" style="color: var(--minted-text-primary);">{{ params.data.categoryName }}</div>
      </div>
    </div>
  `,
  styles: [`
    :host {
      display: flex;
      align-items: center;
      width: 100%;
      height: 100%;
    }
  `]
})
export class CategoryCellRendererComponent implements ICellRendererAngularComp {
  params!: ICellRendererParams;

  agInit(params: ICellRendererParams): void {
    this.params = params;
  }

  refresh(params: ICellRendererParams): boolean {
    this.params = params;
    return true;
  }

  getCategoryColorClass(): string {
    const color = this.params.data.categoryColor || 'gray';
    const colorMap: Record<string, string> = {
      'red': 'bg-red-100 text-red-600',
      'blue': 'bg-blue-100 text-blue-600',
      'green': 'bg-green-100 text-green-600',
      'yellow': 'bg-yellow-100 text-yellow-600',
      'purple': 'bg-purple-100 text-purple-600',
      'orange': 'bg-orange-100 text-orange-600',
      'pink': 'bg-pink-100 text-pink-600',
      'cyan': 'bg-cyan-100 text-cyan-600',
      'gray': 'bg-gray-100 text-gray-600'
    };
    return colorMap[color] || 'bg-gray-100 text-gray-600';
  }
}
