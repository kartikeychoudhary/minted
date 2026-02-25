import { Component, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { MessageService } from 'primeng/api';
import { Accounts } from '../components/accounts/accounts';

@Component({
  selector: 'app-settings',
  standalone: false,
  templateUrl: './settings.html',
  styleUrl: './settings.scss',
  providers: [MessageService]
})
export class Settings {

  @ViewChild('accountsComponent') accountsComponent!: Accounts;
  activeTab: string | number = '0';

  constructor(private router: Router) {}

  goBack(): void {
    this.router.navigate(['/']);
  }

  onTabChange(value: string | number | undefined): void {
    if (value == null) return;
    this.activeTab = value;
    if (String(value) === '1' && this.accountsComponent) {
      this.accountsComponent.refreshData();
    }
  }
}
