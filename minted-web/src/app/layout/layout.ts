import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { NavigationStart, NavigationEnd, NavigationCancel, NavigationError, Router } from '@angular/router';
import { Subject } from 'rxjs';
import { filter, takeUntil } from 'rxjs/operators';
import { ThemeService } from '../core/services/theme.service';
import { PrivacyService } from '../core/services/privacy.service';
import { NotificationService } from '../core/services/notification.service';
import { AuthService } from '../core/services/auth.service';
import { NotificationType } from '../core/models/notification.model';

@Component({
  selector: 'app-layout',
  standalone: false,
  templateUrl: './layout.html',
  styleUrl: './layout.scss'
})
export class Layout implements OnInit, OnDestroy {
  isSidebarOpen = true;
  isNotificationDrawerVisible = false;
  isMobileSidebarVisible = false;
  isRouteLoading = false;
  private mobileQuery!: MediaQueryList;
  private mobileQueryListener!: (e: MediaQueryListEvent) => void;
  private destroy$ = new Subject<void>();

  constructor(
    public themeService: ThemeService,
    public privacyService: PrivacyService,
    public notificationService: NotificationService,
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.authService.currentUser$.pipe(takeUntil(this.destroy$)).subscribe(user => {
      if (user) {
        this.notificationService.startPolling();
      } else {
        this.notificationService.stopPolling();
      }
    });

    // Route loading indicator + auto-close mobile sidebar on navigation
    this.router.events.pipe(takeUntil(this.destroy$)).subscribe(event => {
      if (event instanceof NavigationStart) {
        this.isRouteLoading = true;
        this.cdr.detectChanges();
      } else if (event instanceof NavigationEnd || event instanceof NavigationCancel || event instanceof NavigationError) {
        this.isRouteLoading = false;
        this.isMobileSidebarVisible = false;
        this.cdr.detectChanges();
      }
    });
  }

  get isMobile(): boolean {
    return window.innerWidth < 768;
  }

  toggleMobileSidebar(): void {
    this.isMobileSidebarVisible = !this.isMobileSidebarVisible;
    this.cdr.detectChanges();
  }

  onSidebarToggle(isOpen: boolean): void {
    this.isSidebarOpen = isOpen;
  }

  toggleNotificationDrawer(): void {
    this.isNotificationDrawerVisible = !this.isNotificationDrawerVisible;
    if (this.isNotificationDrawerVisible) {
      this.notificationService.loadNotifications(0);
    }
    this.cdr.detectChanges();
  }

  onNotificationClick(n: any): void {
    if (!n.isRead) {
      this.notificationService.markAsRead(n.id);
    }
  }

  viewAllNotifications(): void {
    this.isNotificationDrawerVisible = false;
    this.router.navigate(['/notifications']);
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
