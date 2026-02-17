import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MessageService } from 'primeng/api';
import { AuthService } from '../../../../core/services/auth.service';

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

  constructor(
    private formBuilder: FormBuilder,
    private authService: AuthService,
    private messageService: MessageService,
    private router: Router
  ) {}

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
    // Get user info from AuthService or localStorage
    const username = localStorage.getItem('username') || 'User';
    const email = localStorage.getItem('email') || '';
    const displayName = localStorage.getItem('displayName') || username;

    this.currentUser = {
      username,
      email,
      displayName
    };

    this.profileForm?.patchValue({
      username: username,
      email: email,
      displayName: displayName
    });
  }

  saveProfile(): void {
    if (this.profileForm?.invalid) {
      return;
    }

    const formValue = this.profileForm!.getRawValue();

    // TODO: Implement API call to update profile
    // For now, just update localStorage and show success message
    localStorage.setItem('displayName', formValue.displayName);
    localStorage.setItem('email', formValue.email);

    this.messageService.add({
      severity: 'success',
      summary: 'Success',
      detail: 'Profile updated successfully'
    });
  }

  updatePassword(): void {
    if (this.passwordForm?.invalid) {
      // Mark all fields as touched to show validation errors
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
    // TODO: Implement API call to save notification preferences
    // For now, just show success message
    this.messageService.add({
      severity: 'success',
      summary: 'Success',
      detail: 'Notification preferences saved'
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
