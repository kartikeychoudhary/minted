import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { ThemeService } from '../../../core/services/theme.service';

interface NavigationItem {
  label: string;
  icon: string;
  route: string;
  section?: string;
}

@Component({
  selector: 'app-sidebar',
  standalone: false,
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.scss'
})
export class Sidebar {
  @Input() mobileMode = false;
  @Output() sidebarToggle = new EventEmitter<boolean>();
  @Output() navigationClicked = new EventEmitter<void>();

  isOpen = true;
  currentUser: any = null;
  showUserMenu = false;

  navigationItems: NavigationItem[] = [];

  private baseNavigationItems: NavigationItem[] = [
    { label: 'Dashboard', icon: 'pi pi-th-large', route: '/' },
    { label: 'Transactions', icon: 'pi pi-list', route: '/transactions' },
    { label: 'Recurring', icon: 'pi pi-sync', route: '/recurring' },
    { label: 'Import', icon: 'pi pi-upload', route: '/import' },
    { label: 'Statements', icon: 'pi pi-file', route: '/statements' },
    { label: 'Splits', icon: 'pi pi-sitemap', route: '/splits' },
    { label: 'Analytics', icon: 'pi pi-chart-pie', route: '/analytics' },
    { label: 'Notifications', icon: 'pi pi-bell', route: '/notifications' },
    { label: 'Settings', icon: 'pi pi-cog', route: '/settings', section: 'Management' },
  ];

  private adminNavigationItems: NavigationItem[] = [
    { label: 'Users', icon: 'pi pi-users', route: '/admin/users', section: 'Admin' },
    { label: 'Server Jobs', icon: 'pi pi-clock', route: '/admin/jobs' },
    { label: 'Server Settings', icon: 'pi pi-server', route: '/admin/settings' }
  ];

  constructor(
    public router: Router,
    private authService: AuthService,
    public themeService: ThemeService
  ) { }

  ngOnInit(): void {
    this.authService.currentUser$.subscribe(user => {
      if (user) {
        this.currentUser = {
          displayName: user.displayName || user.username,
          email: user.email,
          avatar: null,
          role: user.role
        };
        this.buildNavigation(user.role);
      }
    });
  }

  buildNavigation(role?: string): void {
    this.navigationItems = [...this.baseNavigationItems];
    if (role === 'ADMIN') {
      this.navigationItems = [...this.navigationItems, ...this.adminNavigationItems];
    }
  }

  toggleSidebar(): void {
    this.isOpen = !this.isOpen;
    this.sidebarToggle.emit(this.isOpen);
  }

  toggleUserMenu(): void {
    this.showUserMenu = !this.showUserMenu;
  }

  isActive(route: string): boolean {
    if (route === '/') {
      return this.router.url === '/';
    }
    return this.router.url.startsWith(route);
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
    this.onNavClick();
  }

  onNavClick(): void {
    if (this.mobileMode) {
      this.navigationClicked.emit();
    }
  }

  navigateToProfile(): void {
    this.router.navigate(['/settings']);
    this.showUserMenu = false;
    this.onNavClick();
  }

  getInitials(name: string): string {
    const parts = name.split(' ');
    if (parts.length >= 2) {
      return (parts[0][0] + parts[1][0]).toUpperCase();
    }
    return name.substring(0, 2).toUpperCase();
  }

  get userAvatar(): string | null {
    return localStorage.getItem('avatarBase64');
  }
}
