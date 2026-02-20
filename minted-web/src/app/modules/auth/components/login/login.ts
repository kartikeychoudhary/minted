import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { MessageService } from 'primeng/api';
import { AuthService } from '../../../../core';

@Component({
  selector: 'app-login',
  standalone: false,
  templateUrl: './login.html',
  styleUrl: './login.scss',
  providers: [MessageService]
})
export class Login implements OnInit {
  loginForm?: FormGroup;
  loading = false;
  returnUrl: string = '/';
  signupEnabled = false;

  constructor(
    private formBuilder: FormBuilder,
    private router: Router,
    private route: ActivatedRoute,
    private authService: AuthService,
    private messageService: MessageService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    // Redirect to dashboard if already logged in
    if (this.authService.isAuthenticated) {
      this.router.navigate(['/']);
      return;
    }

    // Initialize form
    this.loginForm = this.formBuilder.group({
      username: ['', Validators.required],
      password: ['', Validators.required]
    });

    // Get return url from route parameters or default to '/'
    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/';

    // Check if signup is enabled
    this.authService.isSignupEnabled().subscribe({
      next: (enabled) => { this.signupEnabled = enabled; this.cdr.detectChanges(); },
      error: () => { this.signupEnabled = false; }
    });
  }

  onSubmit(): void {
    if (!this.loginForm || this.loginForm.invalid) {
      return;
    }

    this.loading = true;
    const { username, password } = this.loginForm.value;

    this.authService.login(username, password).subscribe({
      next: (response) => {
        if (response.success) {
          // Check if user needs to change password
          if (response.data.user.forcePasswordChange) {
            this.loading = false;
            this.router.navigate(['/change-password']);
          } else {
            this.loading = false;
            this.router.navigate([this.returnUrl]);
          }
        } else {
          this.loading = false;
        }
      },
      error: (error) => {
        this.loading = false;
        this.messageService.add({
          severity: 'error',
          summary: 'Login Failed',
          detail: error.message || 'Invalid username or password'
        });
      }
    });
  }

  get f() {
    return this.loginForm?.controls || {};
  }
}
