import { Component, EventEmitter, Output } from '@angular/core';
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
  @Output() sidebarToggle = new EventEmitter<boolean>();

  isOpen = true;
  currentUser: any = null;
  showUserMenu = false;

  navigationItems: NavigationItem[] = [];

  private baseNavigationItems: NavigationItem[] = [
    { label: 'Dashboard', icon: 'dashboard', route: '/' },
    { label: 'Transactions', icon: 'receipt_long', route: '/transactions' },
    { label: 'Recurring', icon: 'sync_alt', route: '/recurring' },
    { label: 'Import', icon: 'upload_file', route: '/import' },
    { label: 'Statements', icon: 'description', route: '/statements' },
    { label: 'Analytics', icon: 'pie_chart', route: '/analytics' },
    { label: 'Notifications', icon: 'notifications', route: '/notifications' },
    { label: 'Settings', icon: 'settings', route: '/settings', section: 'Management' },
  ];

  private adminNavigationItems: NavigationItem[] = [
    { label: 'Users', icon: 'group', route: '/admin/users', section: 'Admin' },
    { label: 'Server Jobs', icon: 'schedule', route: '/admin/jobs' },
    { label: 'Server Settings', icon: 'dns', route: '/admin/settings' }
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
  }

  navigateToProfile(): void {
    this.router.navigate(['/settings']);
    this.showUserMenu = false;
  }

  getInitials(name: string): string {
    const parts = name.split(' ');
    if (parts.length >= 2) {
      return (parts[0][0] + parts[1][0]).toUpperCase();
    }
    return name.substring(0, 2).toUpperCase();
  }
}
