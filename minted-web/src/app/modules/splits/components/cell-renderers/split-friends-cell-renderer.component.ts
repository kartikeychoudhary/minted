import { Component } from '@angular/core';
import { ICellRendererAngularComp } from 'ag-grid-angular';
import { ICellRendererParams } from 'ag-grid-community';

@Component({
  selector: 'app-split-friends-cell-renderer',
  standalone: false,
  template: `
    <div class="flex items-center h-full">
      <div class="flex -space-x-2 overflow-hidden">
        <div *ngFor="let share of friendShares"
          class="inline-flex items-center justify-center w-8 h-8 rounded-full text-xs font-bold text-white"
          style="box-shadow: 0 0 0 2px var(--minted-bg-card);"
          [style.background-color]="share.friendAvatarColor || '#6366f1'"
          [pTooltip]="share.friendName"
          tooltipPosition="top">
          {{ getInitials(share.friendName) }}
        </div>
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
export class SplitFriendsCellRendererComponent implements ICellRendererAngularComp {
  friendShares: any[] = [];

  agInit(params: ICellRendererParams): void {
    this.extractFriends(params);
  }

  refresh(params: ICellRendererParams): boolean {
    this.extractFriends(params);
    return true;
  }

  private extractFriends(params: ICellRendererParams): void {
    const shares = params.data?.shares || [];
    this.friendShares = shares.filter((s: any) => s.friendId !== null);
  }

  getInitials(name: string): string {
    if (!name) return '?';
    const parts = name.trim().split(' ');
    if (parts.length >= 2) {
      return (parts[0][0] + parts[1][0]).toUpperCase();
    }
    return name.substring(0, 2).toUpperCase();
  }
}
