import { Component, OnInit, signal } from '@angular/core';
import { ThemeService } from './core/services/theme.service';
import { PrivacyService } from './core/services/privacy.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.html',
  standalone: false,
  styleUrl: './app.scss'
})
export class App implements OnInit {
  protected readonly title = signal('Minted');

  constructor(
    private themeService: ThemeService,
    private privacyService: PrivacyService
  ) {}

  ngOnInit(): void {
    this.themeService.init();
    this.privacyService.init();
  }
}
