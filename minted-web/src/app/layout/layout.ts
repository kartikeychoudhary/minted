import { Component } from '@angular/core';
import { ThemeService } from '../core/services/theme.service';

@Component({
  selector: 'app-layout',
  standalone: false,
  templateUrl: './layout.html',
  styleUrl: './layout.scss'
})
export class Layout {
  isSidebarOpen = true;

  constructor(public themeService: ThemeService) {}

  onSidebarToggle(isOpen: boolean): void {
    this.isSidebarOpen = isOpen;
  }
}
