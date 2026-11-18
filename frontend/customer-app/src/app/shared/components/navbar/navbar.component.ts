import { Component, Output, EventEmitter, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';
import { CartService } from '../../../features/cart/services/cart.service';
import { NotificationService } from '../../../core/services/notification.service';
import { AuthModalComponent } from '../auth-modal/auth-modal.component';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, RouterLinkActive, AuthModalComponent],
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent {
  authService = inject(AuthService);
  cartService = inject(CartService);
  private notificationService = inject(NotificationService);

  @Output() searchChanged = new EventEmitter<string>();
  @Output() cartToggled = new EventEmitter<void>();

  searchQuery: string = '';

  onSearch(): void {
    this.searchChanged.emit(this.searchQuery);
  }

  toggleCart(): void {
    if (!this.authService.currentUser()) {
      this.notificationService.info('Please sign in to view your cart.');
      this.authService.openAuthModal('signin');
      return;
    }
    this.cartToggled.emit();
  }

  openSignIn(): void {
    this.authService.openAuthModal('signin');
  }

  openSignUp(): void {
    this.authService.openAuthModal('signup');
  }
}
