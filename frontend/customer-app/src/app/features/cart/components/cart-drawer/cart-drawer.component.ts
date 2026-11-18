import { Component, Output, EventEmitter, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CartService } from '../../services/cart.service';
import { AuthService } from '../../../../core/auth/auth.service';
import { NotificationService } from '../../../../core/services/notification.service';
import { PriceFormatPipe } from '../../../../shared/pipes/price-format.pipe';

@Component({
  selector: 'app-cart-drawer',
  standalone: true,
  imports: [CommonModule, PriceFormatPipe],
  templateUrl: './cart-drawer.component.html',
  styleUrls: ['./cart-drawer.component.css']
})
export class CartDrawerComponent {
  cartService = inject(CartService);
  private authService = inject(AuthService);
  private notificationService = inject(NotificationService);

  @Output() checkoutStarted = new EventEmitter<void>();

  onCheckout(): void {
    if (!this.authService.currentUser()) {
      this.cartService.closeDrawer();
      this.notificationService.warning('Please sign in to proceed with checkout.');
      this.authService.openAuthModal('signin');
      return;
    }
    this.cartService.closeDrawer();
    this.checkoutStarted.emit();
  }
}
