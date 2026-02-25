import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { NotificationService } from '../../../../core/services/notification.service';
import { NotificationResponse, NotificationType } from '../../../../core/models/notification.model';
import { ConfirmationService, MessageService } from 'primeng/api';

interface NotificationGroup {
  label: string;
  notifications: NotificationResponse[];
}

@Component({
  selector: 'app-notifications-list',
  standalone: false,
  templateUrl: './notifications-list.html',
  styleUrl: './notifications-list.scss',
})
export class NotificationsList implements OnInit, OnDestroy {
  groups: NotificationGroup[] = [];
  loading = true;
  private destroy$ = new Subject<void>();

  constructor(
    public notificationService: NotificationService,
    private confirmationService: ConfirmationService,
    private messageService: MessageService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.notificationService.loadNotifications(0);
    this.notificationService.notifications$.pipe(takeUntil(this.destroy$)).subscribe(notifications => {
      this.groups = this.groupByDate(notifications);
      this.loading = false;
      this.cdr.detectChanges();
    });
  }

  private groupByDate(notifications: NotificationResponse[]): NotificationGroup[] {
    const now = new Date();
    const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
    const yesterday = new Date(today.getTime() - 86400000);
    const startOfWeek = new Date(today.getTime() - today.getDay() * 86400000);

    const groups: { [key: string]: NotificationResponse[] } = {
      'Today': [],
      'Yesterday': [],
      'Earlier this Week': [],
      'Older': []
    };

    for (const n of notifications) {
      const date = new Date(n.createdAt);
      const nDate = new Date(date.getFullYear(), date.getMonth(), date.getDate());

      if (nDate.getTime() === today.getTime()) {
        groups['Today'].push(n);
      } else if (nDate.getTime() === yesterday.getTime()) {
        groups['Yesterday'].push(n);
      } else if (nDate.getTime() >= startOfWeek.getTime()) {
        groups['Earlier this Week'].push(n);
      } else {
        groups['Older'].push(n);
      }
    }

    return Object.entries(groups)
      .filter(([_, items]) => items.length > 0)
      .map(([label, items]) => ({ label, notifications: items }));
  }

  markAllAsRead(): void {
    this.notificationService.markAllAsRead();
    this.messageService.add({ severity: 'success', summary: 'Done', detail: 'All notifications marked as read' });
  }

  clearAll(): void {
    this.confirmationService.confirm({
      key: 'notificationsPage',
      message: 'This will delete all read notifications. Continue?',
      header: 'Clear Notifications',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.notificationService.dismissAllRead();
        this.messageService.add({ severity: 'success', summary: 'Cleared', detail: 'Read notifications removed' });
      }
    });
  }

  onNotificationClick(n: NotificationResponse): void {
    if (!n.isRead) {
      this.notificationService.markAsRead(n.id);
    }
  }

  dismiss(event: Event, id: number): void {
    event.stopPropagation();
    this.notificationService.dismiss(id);
  }

  loadMore(): void {
    this.notificationService.loadMore();
  }

  getNotificationIcon(type: NotificationType): string {
    switch (type) {
      case 'SUCCESS': return 'pi pi-check-circle';
      case 'WARNING': return 'pi pi-exclamation-triangle';
      case 'ERROR':   return 'pi pi-times-circle';
      case 'SYSTEM':  return 'pi pi-megaphone';
      case 'INFO':
      default:        return 'pi pi-info-circle';
    }
  }

  getNotificationColor(type: NotificationType): string {
    switch (type) {
      case 'SUCCESS': return 'var(--minted-success)';
      case 'WARNING': return 'var(--minted-warning)';
      case 'ERROR':   return 'var(--minted-danger)';
      case 'SYSTEM':  return 'var(--minted-accent)';
      case 'INFO':
      default:        return 'var(--minted-info)';
    }
  }

  getNotificationBg(type: NotificationType): string {
    switch (type) {
      case 'SUCCESS': return 'var(--minted-success-subtle)';
      case 'WARNING': return 'var(--minted-warning-subtle)';
      case 'ERROR':   return 'var(--minted-danger-subtle)';
      case 'SYSTEM':  return 'var(--minted-accent-subtle)';
      case 'INFO':
      default:        return 'var(--minted-info-subtle)';
    }
  }

  getRelativeTime(dateStr: string): string {
    const date = new Date(dateStr);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMs / 3600000);
    const diffDays = Math.floor(diffMs / 86400000);

    if (diffMins < 1) return 'Just now';
    if (diffMins < 60) return `${diffMins}m ago`;
    if (diffHours < 24) return `${diffHours}h ago`;
    if (diffDays < 7) return `${diffDays}d ago`;
    return date.toLocaleDateString();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
