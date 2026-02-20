import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router } from '@angular/router';
import { MessageService } from 'primeng/api';
import { AuthService } from '../../../../core/services/auth.service';

@Component({
  selector: 'app-signup',
  standalone: false,
  templateUrl: './signup.html',
  styleUrl: './signup.scss'
})
export class Signup implements OnInit {
  signupForm?: FormGroup;
  loading = false;
  signupEnabled = true;
  checkingSignup = true;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private messageService: MessageService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    if (this.authService.isAuthenticated) {
      this.router.navigate(['/']);
      return;
    }

    this.signupForm = this.fb.group({
      displayName: ['', [Validators.maxLength(100)]],
      email: ['', [Validators.email, Validators.maxLength(100)]],
      username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(50)]],
      password: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', [Validators.required]]
    }, { validators: this.passwordMatchValidator });

    this.authService.isSignupEnabled().subscribe({
      next: (enabled) => {
        this.signupEnabled = enabled;
        this.checkingSignup = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.signupEnabled = false;
        this.checkingSignup = false;
        this.cdr.detectChanges();
      }
    });
  }

  passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
    const password = control.get('password');
    const confirmPassword = control.get('confirmPassword');
    if (password && confirmPassword && password.value !== confirmPassword.value) {
      confirmPassword.setErrors({ passwordMismatch: true });
      return { passwordMismatch: true };
    }
    return null;
  }

  onSubmit(): void {
    if (!this.signupForm || this.signupForm.invalid || !this.signupEnabled) return;

    this.loading = true;
    const formValue = this.signupForm.value;

    this.authService.signup({
      username: formValue.username,
      password: formValue.password,
      confirmPassword: formValue.confirmPassword,
      displayName: formValue.displayName || undefined,
      email: formValue.email || undefined
    }).subscribe({
      next: (response) => {
        this.loading = false;
        if (response.success) {
          this.router.navigate(['/']);
        }
        this.cdr.detectChanges();
      },
      error: (error) => {
        this.loading = false;
        this.messageService.add({
          severity: 'error',
          summary: 'Registration Failed',
          detail: error.error?.message || 'Failed to create account'
        });
        this.cdr.detectChanges();
      }
    });
  }

  get f() {
    return this.signupForm?.controls || {};
  }
}
