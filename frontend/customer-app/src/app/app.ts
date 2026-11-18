import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import { NavbarComponent } from './shared/components/navbar/navbar.component';
import { FooterComponent } from './shared/components/footer/footer.component';
import { CartDrawerComponent } from './features/cart/components/cart-drawer/cart-drawer.component';
import { SagaStepperComponent } from './features/checkout/components/saga-stepper/saga-stepper.component';
import { CartService } from './features/cart/services/cart.service';
import { NotificationService } from './core/services/notification.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet,
    NavbarComponent,
    FooterComponent,
    CartDrawerComponent,
    SagaStepperComponent
  ],
  templateUrl: './app.html',
  styleUrls: ['./app.css']
})
export class App {
  cartService = inject(CartService);
  notificationService = inject(NotificationService);

  isCheckoutOpen: boolean = false;

  onCartToggle(): void {
    this.cartService.toggleDrawer();
  }

  startCheckout(): void {
    this.isCheckoutOpen = true;
  }

  closeCheckout(): void {
    this.isCheckoutOpen = false;
  }
}
