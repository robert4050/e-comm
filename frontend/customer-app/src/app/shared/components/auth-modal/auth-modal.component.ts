import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-auth-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './auth-modal.component.html',
  styleUrls: ['./auth-modal.component.css']
})
export class AuthModalComponent {
  authService = inject(AuthService);

  // Form fields
  email: string = '';
  password: string = '';
  name: string = '';
  confirmPassword: string = '';
  errorMessage: string = '';

  onGoogleLogin(): void {
    this.errorMessage = '';
    this.authService.loginWithGoogle();
  }

  onSignIn(): void {
    this.errorMessage = '';
    const res = this.authService.loginWithPassword(this.email, this.password);
    if (!res.success && res.error) {
      this.errorMessage = res.error;
    }
  }

  onSignUp(): void {
    this.errorMessage = '';
    const res = this.authService.signUp(this.name, this.email, this.password, this.confirmPassword);
    if (!res.success && res.error) {
      this.errorMessage = res.error;
    }
  }

  setTab(tab: 'signin' | 'signup'): void {
    this.errorMessage = '';
    this.authService.authModalTab.set(tab);
  }

  close(): void {
    this.errorMessage = '';
    this.authService.closeAuthModal();
  }
}
