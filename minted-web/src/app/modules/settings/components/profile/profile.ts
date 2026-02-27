import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';
import { MessageService } from 'primeng/api';
import { AuthService } from '../../../../core/services/auth.service';
import { ThemeService, AccentPreset } from '../../../../core/services/theme.service';
import { CurrencyService, CurrencyOption } from '../../../../core/services/currency.service';
import { ProfileService } from '../../../../core/services/profile.service';

@Component({
  selector: 'app-profile',
  standalone: false,
  templateUrl: './profile.html',
  styleUrl: './profile.scss',
})
export class Profile implements OnInit {
  profileForm?: FormGroup;
  passwordForm?: FormGroup;
  currentUser: any = null;

  // Notification preferences
  transactionAlertsEnabled = true;
  budgetOverrunEnabled = true;
  weeklySummaryEnabled = false;
  twoFactorEnabled = false;

  // Theme (assigned in constructor after DI)
  isDarkMode$!: Observable<boolean>;
  accentColor$!: Observable<string>;
  accentPresets!: AccentPreset[];

  // Currency
  currencies!: CurrencyOption[];
  selectedCurrency!: string;

  constructor(
    private formBuilder: FormBuilder,
    private authService: AuthService,
    private messageService: MessageService,
    private router: Router,
    public themeService: ThemeService,
    public currencyService: CurrencyService,
    private profileService: ProfileService
  ) {
    this.isDarkMode$ = themeService.isDarkMode$;
    this.accentColor$ = themeService.accentColor$;
    this.accentPresets = themeService.accentPresets;
    this.currencies = currencyService.currencies;
    this.selectedCurrency = currencyService.currentCurrency;
  }

  ngOnInit(): void {
    this.initForms();
    this.loadUserInfo();
  }

  initForms(): void {
    // Profile form
    this.profileForm = this.formBuilder.group({
      displayName: ['', [Validators.required, Validators.maxLength(100)]],
      email: ['', [Validators.required, Validators.email, Validators.maxLength(100)]],
      username: [{ value: '', disabled: true }]
    });

    // Password form
    this.passwordForm = this.formBuilder.group({
      currentPassword: ['', Validators.required],
      newPassword: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', Validators.required]
    }, {
      validators: this.passwordMatchValidator
    });
  }

  passwordMatchValidator(form: FormGroup) {
    const newPassword = form.get('newPassword');
    const confirmPassword = form.get('confirmPassword');

    if (newPassword && confirmPassword && newPassword.value !== confirmPassword.value) {
      confirmPassword.setErrors({ passwordMismatch: true });
      return { passwordMismatch: true };
    }
    return null;
  }

  loadUserInfo(): void {
    const user = this.authService.currentUserValue;
    const username = user?.username || 'User';
    const email = user?.email || '';
    const displayName = user?.displayName || username;
    const avatarBase64 = localStorage.getItem('avatarBase64') || null;

    this.currentUser = {
      username,
      email,
      displayName,
      avatarBase64
    };

    this.profileForm?.patchValue({
      username: username,
      email: email,
      displayName: displayName
    });
  }

  getInitials(name: string): string {
    return name
      .split(' ')
      .map(p => p.charAt(0).toUpperCase())
      .slice(0, 2)
      .join('');
  }

  saveProfile(): void {
    if (this.profileForm?.invalid) {
      return;
    }

    const formValue = this.profileForm!.getRawValue();

    this.profileService.updateProfile({
      displayName: formValue.displayName,
      email: formValue.email
    }).subscribe({
      next: (response) => {
        if (response.success && response.data) {
          localStorage.setItem('displayName', response.data.displayName || formValue.displayName);
          localStorage.setItem('email', response.data.email || formValue.email);
        }
        this.messageService.add({
          severity: 'success',
          summary: 'Success',
          detail: 'Profile updated successfully'
        });
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to update profile'
        });
      }
    });
  }

  onAvatarSelected(file: File): void {
    this.profileService.uploadAvatar(file).subscribe({
      next: (response) => {
        if (response.success && response.data) {
          const avatarBase64 = (response.data as any).avatarBase64 || null;
          if (this.currentUser) { this.currentUser.avatarBase64 = avatarBase64; }
          if (avatarBase64) { localStorage.setItem('avatarBase64', avatarBase64); }
        }
        this.messageService.add({ severity: 'success', summary: 'Avatar Updated', detail: 'Profile picture saved.' });
      },
      error: () => {
        this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Failed to upload avatar.' });
      }
    });
  }

  onAvatarRemoved(): void {
    this.profileService.deleteAvatar().subscribe({
      next: () => {
        if (this.currentUser) { this.currentUser.avatarBase64 = null; }
        localStorage.removeItem('avatarBase64');
        this.messageService.add({ severity: 'info', summary: 'Avatar Removed', detail: 'Profile picture removed.' });
      },
      error: () => {
        this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Failed to remove avatar.' });
      }
    });
  }

  updatePassword(): void {
    if (this.passwordForm?.invalid) {
      Object.keys(this.passwordForm.controls).forEach(key => {
        this.passwordForm?.get(key)?.markAsTouched();
      });
      return;
    }

    const { currentPassword, newPassword, confirmPassword } = this.passwordForm!.value;

    this.authService.changePassword({
      currentPassword,
      newPassword,
      confirmPassword
    }).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Success',
          detail: 'Password updated successfully'
        });
        this.passwordForm?.reset();
      },
      error: (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: error?.message || 'Failed to update password'
        });
      }
    });
  }

  goToChangePassword(): void {
    this.router.navigate(['/change-password']);
  }

  saveNotificationPreferences(): void {
    this.messageService.add({
      severity: 'success',
      summary: 'Success',
      detail: 'Notification preferences saved'
    });
  }

  onDarkModeChange(event: any): void {
    this.themeService.toggleDarkMode();
  }

  onAccentSelect(color: string): void {
    this.themeService.setAccentColor(color);
  }

  onCurrencyChange(code: string): void {
    this.currencyService.setCurrency(code);
    this.profileService.updateProfile({ currency: code }).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Currency Updated',
          detail: `Default currency changed to ${code}`
        });
      },
      error: () => {
        this.messageService.add({
          severity: 'warn',
          summary: 'Saved Locally',
          detail: `Currency set to ${code} (will sync when online)`
        });
      }
    });
  }

  onTransactionAlertsChange(event: any): void {
    this.transactionAlertsEnabled = event.checked;
  }

  onBudgetOverrunChange(event: any): void {
    this.budgetOverrunEnabled = event.checked;
  }

  onWeeklySummaryChange(event: any): void {
    this.weeklySummaryEnabled = event.checked;
  }

  onTwoFactorChange(event: any): void {
    this.twoFactorEnabled = event.checked;
    if (this.twoFactorEnabled) {
      this.messageService.add({
        severity: 'info',
        summary: 'Coming Soon',
        detail: 'Two-Factor Authentication setup will be available in a future update'
      });
    }
  }
}
