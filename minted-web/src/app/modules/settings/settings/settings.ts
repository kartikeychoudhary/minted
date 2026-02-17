import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { MessageService } from 'primeng/api';

@Component({
  selector: 'app-settings',
  standalone: false,
  templateUrl: './settings.html',
  styleUrl: './settings.scss',
  providers: [MessageService]
})
export class Settings {

  constructor(private router: Router) {}

  goBack(): void {
    this.router.navigate(['/']);
  }
}
