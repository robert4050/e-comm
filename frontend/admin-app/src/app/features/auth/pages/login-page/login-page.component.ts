import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../../core/auth/auth.service';
import { NotificationService } from '../../../../core/services/notification.service';

@Component({
  selector: 'app-login-page',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login-page.component.html',
  styleUrls: ['./login-page.component.css']
})
export class LoginPageComponent {
  authService = inject(AuthService);
  router = inject(Router);
  notificationService = inject(NotificationService);

  email: string = 'admin@gmail.com';
  password: string = 'admin1234';
  errorMessage: string = '';
  isLoading: boolean = false;

  onGoogleLogin(): void {
    this.errorMessage = '';
    this.authService.loginWithGoogle();
    this.notificationService.success('Redirecting to Google Workspace authentication...');
  }

  onSignIn(): void {
    this.errorMessage = '';
    if (!this.email || !this.password) {
      this.errorMessage = 'Please enter both work email and password.';
      return;
    }

    this.isLoading = true;
    this.authService.loginWithPassword(this.email, this.password).subscribe({
      next: (res) => {
        this.isLoading = false;
        if (res.success) {
          this.notificationService.success(`Welcome back, ${this.authService.currentUser()?.username}!`);
          this.router.navigate(['/dashboard']);
        } else {
          this.errorMessage = res.error || 'Authentication failed. Please verify credentials.';
        }
      },
      error: () => {
        this.isLoading = false;
        this.errorMessage = 'Authentication server connection error.';
      }
    });
  }
}
