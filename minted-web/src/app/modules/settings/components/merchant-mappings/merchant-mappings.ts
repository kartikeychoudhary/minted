import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { MessageService, ConfirmationService } from 'primeng/api';
import { ColDef, GridOptions, GridApi, GridReadyEvent, CellValueChangedEvent, themeQuartz } from 'ag-grid-community';
import { LlmConfigService } from '../../../../core/services/llm-config.service';
import { CategoryService } from '../../../../core/services/category.service';
import { MerchantMapping } from '../../../../core/models/llm-config.model';
import { CategoryResponse } from '../../../../core/models/category.model';

@Component({
  selector: 'app-merchant-mappings',
  standalone: false,
  templateUrl: './merchant-mappings.html',
  styleUrl: './merchant-mappings.scss'
})
export class MerchantMappingsComponent implements OnInit {
  mappings: MerchantMapping[] = [];
  categories: CategoryResponse[] = [];
  loading = true;
  private gridApi!: GridApi;

  mintedTheme = themeQuartz.withParams({
    backgroundColor: 'var(--minted-bg-card)',
    foregroundColor: 'var(--minted-text-primary)',
    borderColor: 'var(--minted-border)',
    headerBackgroundColor: 'var(--minted-bg-card)',
    headerFontSize: 12,
    headerFontWeight: 600,
    headerTextColor: 'var(--minted-text-muted)',
    rowHoverColor: 'var(--minted-surface)',
    fontFamily: '"Inter", sans-serif',
    rowHeight: 48,
    headerHeight: 44,
  });

  colDefs: ColDef[] = [
    {
      field: 'snippets',
      headerName: 'Keywords (comma-separated)',
      flex: 2,
      editable: true,
      cellEditor: 'agTextCellEditor'
    },
    {
      field: 'categoryName',
      headerName: 'Category',
      flex: 1,
      editable: true,
      cellEditor: 'agSelectCellEditor',
      cellEditorParams: () => ({ values: this.categories.map(c => c.name) }),
      cellRenderer: (params: any) => {
        const cat = this.categories.find(c => c.name === params.value);
        const dot = cat
          ? `<span style="display:inline-block;width:10px;height:10px;border-radius:50%;background:${cat.color};margin-right:6px"></span>`
          : '';
        return `${dot}${params.value || '\u2014'}`;
      }
    },
    {
      headerName: '',
      width: 60,
      cellRenderer: () =>
        `<button class="text-red-500 hover:bg-red-50 p-1.5 rounded" data-action="delete" title="Delete"><i class="pi pi-trash"></i></button>`
    }
  ];

  gridOptions: GridOptions = {
    domLayout: 'autoHeight',
    singleClickEdit: true,
    stopEditingWhenCellsLoseFocus: true,
    onCellClicked: (params) => {
      const event = params.event as MouseEvent;
      const target = event.target as HTMLElement;
      if (target.closest('button[data-action="delete"]')) {
        this.deleteMapping(params.data);
      }
    },
    overlayNoRowsTemplate: '<span style="padding:16px;color:var(--minted-text-muted)">No merchant mappings yet. Add one to improve AI categorisation accuracy.</span>'
  };

  constructor(
    private llmConfigService: LlmConfigService,
    private categoryService: CategoryService,
    private messageService: MessageService,
    private confirmationService: ConfirmationService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadCategories();
  }

  loadCategories(): void {
    this.categoryService.getAll().subscribe({
      next: (cats) => {
        this.categories = cats;
        this.loadMappings();
      }
    });
  }

  loadMappings(): void {
    this.loading = true;
    this.llmConfigService.getMappings().subscribe({
      next: (data) => {
        this.mappings = data;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  onGridReady(params: GridReadyEvent): void {
    this.gridApi = params.api;
  }

  onCellValueChanged(event: CellValueChangedEvent): void {
    const data = event.data as any;
    if (!data.snippets || !data.categoryName) return;

    const cat = this.categories.find(c => c.name === data.categoryName);
    if (!cat) return;

    const request = { snippets: data.snippets, categoryId: cat.id };

    if (data.id) {
      // Update existing
      this.llmConfigService.updateMapping(data.id, request).subscribe({
        next: (updated) => {
          Object.assign(data, updated);
          this.gridApi.refreshCells({ rowNodes: [event.node!] });
          this.messageService.add({ severity: 'success', summary: 'Saved', detail: 'Mapping updated.' });
        },
        error: (err) => {
          this.messageService.add({ severity: 'error', summary: 'Error', detail: err.error?.message || 'Failed to update mapping.' });
        }
      });
    } else {
      // Create new
      this.llmConfigService.createMapping(request).subscribe({
        next: (created) => {
          Object.assign(data, created);
          this.gridApi.refreshCells({ rowNodes: [event.node!] });
          this.messageService.add({ severity: 'success', summary: 'Created', detail: 'Mapping created.' });
        },
        error: (err) => {
          this.messageService.add({ severity: 'error', summary: 'Error', detail: err.error?.message || 'Failed to create mapping.' });
        }
      });
    }
  }

  addMapping(): void {
    const newRow = { id: null, snippets: '', categoryName: '', snippetList: [], categoryId: 0, categoryIcon: '', categoryColor: '' };
    this.mappings = [...this.mappings, newRow as any];
    this.cdr.detectChanges();

    // Start editing the new row
    setTimeout(() => {
      if (this.gridApi) {
        const lastRowIndex = this.mappings.length - 1;
        this.gridApi.startEditingCell({ rowIndex: lastRowIndex, colKey: 'snippets' });
      }
    }, 100);
  }

  deleteMapping(data: any): void {
    if (!data.id) {
      // Remove unsaved row
      this.mappings = this.mappings.filter(m => m !== data);
      this.cdr.detectChanges();
      return;
    }

    this.confirmationService.confirm({
      message: 'Delete this merchant mapping?',
      accept: () => {
        this.llmConfigService.deleteMapping(data.id).subscribe({
          next: () => {
            this.mappings = this.mappings.filter(m => (m as any).id !== data.id);
            this.messageService.add({ severity: 'success', summary: 'Deleted', detail: 'Mapping deleted.' });
            this.cdr.detectChanges();
          },
          error: (err) => {
            this.messageService.add({ severity: 'error', summary: 'Error', detail: err.error?.message || 'Failed to delete mapping.' });
          }
        });
      }
    });
  }
}
