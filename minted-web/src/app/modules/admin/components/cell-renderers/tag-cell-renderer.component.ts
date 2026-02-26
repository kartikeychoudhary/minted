import { Component } from '@angular/core';
import { ICellRendererAngularComp } from 'ag-grid-angular';
import { ICellRendererParams } from 'ag-grid-community';

export interface TagConfig {
  value: string;
  severity?: 'success' | 'info' | 'warn' | 'danger' | 'secondary' | 'contrast';
  rounded?: boolean;
  icon?: string;
}

@Component({
  selector: 'app-tag-cell-renderer',
  standalone: false,
  templateUrl: './tag-cell-renderer.component.html',
  styleUrls: ['./tag-cell-renderer.component.scss']
})
export class TagCellRendererComponent implements ICellRendererAngularComp {
  params!: ICellRendererParams;
  tagConfig: TagConfig | null = null;

  agInit(params: ICellRendererParams): void {
    this.params = params;
    this.updateConfig();
  }

  refresh(params: ICellRendererParams): boolean {
    this.params = params;
    this.updateConfig();
    return true;
  }

  private updateConfig(): void {
    const getConfig = (this.params as any).getTagConfig;
    if (getConfig) {
      this.tagConfig = getConfig(this.params);
    }
  }
}
