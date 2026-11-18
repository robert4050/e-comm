import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-admin-auth-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-auth-modal.component.html',
  styleUrls: ['./admin-auth-modal.component.css']
})
export class AdminAuthModalComponent {
  authService = inject(AuthService);

  email: string = 'admin@gmail.com';
  password: string = 'admin1234';
  errorMessage: string = '';
  isLoading: boolean = false;

  onGoogleLogin(): void {
    this.errorMessage = '';
    this.authService.loginWithGoogle();
  }

  onSignIn(): void {
    this.errorMessage = '';
    this.isLoading = true;
    this.authService.loginWithPassword(this.email, this.password).subscribe(res => {
      this.isLoading = false;
      if (!res.success && res.error) {
        this.errorMessage = res.error;
      }
    });
  }

  close(): void {
    this.errorMessage = '';
    this.authService.closeAuthModal();
  }
}
