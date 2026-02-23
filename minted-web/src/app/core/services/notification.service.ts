import { Injectable, OnDestroy } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { BehaviorSubject, Subject, Subscription, interval } from 'rxjs';
import { map, takeUntil } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { NotificationResponse, NotificationPage } from '../models/notification.model';

@Injectable({ providedIn: 'root' })
export class NotificationService implements OnDestroy {
  private apiUrl = `${environment.apiUrl}/notifications`;

  private unreadCountSubject = new BehaviorSubject<number>(0);
  unreadCount$ = this.unreadCountSubject.asObservable();

  private notificationsSubject = new BehaviorSubject<NotificationResponse[]>([]);
  notifications$ = this.notificationsSubject.asObservable();

  private totalElementsSubject = new BehaviorSubject<number>(0);
  totalElements$ = this.totalElementsSubject.asObservable();

  private destroy$ = new Subject<void>();
  private pollSubscription: Subscription | null = null;
  private currentPage = 0;
  private pageSize = 20;

  constructor(private http: HttpClient) {}

  /** Start polling unread count every 30 seconds. Call after login. */
  startPolling(): void {
    if (this.pollSubscription) return;

    this.fetchUnreadCount();
    this.pollSubscription = interval(30000).pipe(
      takeUntil(this.destroy$)
    ).subscribe(() => {
      this.fetchUnreadCount();
    });
  }

  /** Stop polling. Call on logout. */
  stopPolling(): void {
    if (this.pollSubscription) {
      this.pollSubscription.unsubscribe();
      this.pollSubscription = null;
    }
    this.unreadCountSubject.next(0);
    this.notificationsSubject.next([]);
    this.totalElementsSubject.next(0);
  }

  /** Fetch unread count from server. */
  fetchUnreadCount(): void {
    this.http.get<{ success: boolean; data: number }>(`${this.apiUrl}/unread-count`)
      .pipe(map(r => r.data))
      .subscribe({
        next: (count) => this.unreadCountSubject.next(count),
        error: () => {} // Silent fail for polling
      });
  }

  /** Load notifications (paginated). Called when drawer/page opens. */
  loadNotifications(page: number = 0): void {
    this.currentPage = page;
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', this.pageSize.toString());

    this.http.get<{ success: boolean; data: NotificationPage }>(this.apiUrl, { params })
      .pipe(map(r => r.data))
      .subscribe({
        next: (pageData) => {
          if (page === 0) {
            this.notificationsSubject.next(pageData.content);
          } else {
            const current = this.notificationsSubject.value;
            this.notificationsSubject.next([...current, ...pageData.content]);
          }
          this.totalElementsSubject.next(pageData.totalElements);
        },
        error: () => {}
      });
  }

  /** Load next page for "Load More". */
  loadMore(): void {
    this.loadNotifications(this.currentPage + 1);
  }

  get hasMore(): boolean {
    return this.notificationsSubject.value.length < this.totalElementsSubject.value;
  }

  /** Mark a single notification as read. */
  markAsRead(id: number): void {
    this.http.put<{ success: boolean; data: NotificationResponse }>(
      `${this.apiUrl}/${id}/read`, {}
    ).subscribe({
      next: () => {
        const list = this.notificationsSubject.value.map(n =>
          n.id === id ? { ...n, isRead: true } : n
        );
        this.notificationsSubject.next(list);
        this.decrementUnread();
      }
    });
  }

  /** Mark all notifications as read. */
  markAllAsRead(): void {
    this.http.put<{ success: boolean; message: string }>(
      `${this.apiUrl}/read-all`, {}
    ).subscribe({
      next: () => {
        const list = this.notificationsSubject.value.map(n => ({ ...n, isRead: true }));
        this.notificationsSubject.next(list);
        this.unreadCountSubject.next(0);
      }
    });
  }

  /** Dismiss (delete) a single notification. */
  dismiss(id: number): void {
    const notification = this.notificationsSubject.value.find(n => n.id === id);
    this.http.delete<void>(`${this.apiUrl}/${id}`).subscribe({
      next: () => {
        const list = this.notificationsSubject.value.filter(n => n.id !== id);
        this.notificationsSubject.next(list);
        this.totalElementsSubject.next(this.totalElementsSubject.value - 1);
        if (notification && !notification.isRead) {
          this.decrementUnread();
        }
      }
    });
  }

  /** Dismiss all read notifications. */
  dismissAllRead(): void {
    this.http.delete<{ success: boolean; message: string }>(`${this.apiUrl}/read`)
      .subscribe({
        next: () => {
          const list = this.notificationsSubject.value.filter(n => !n.isRead);
          this.notificationsSubject.next(list);
          this.totalElementsSubject.next(list.length);
        }
      });
  }

  private decrementUnread(): void {
    const current = this.unreadCountSubject.value;
    if (current > 0) {
      this.unreadCountSubject.next(current - 1);
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.stopPolling();
  }
}
