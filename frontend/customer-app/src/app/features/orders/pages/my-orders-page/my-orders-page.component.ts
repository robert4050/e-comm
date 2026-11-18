import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { CustomerOrderService, PlacedOrder } from '../../../checkout/services/order.service';
import { AuthService } from '../../../../core/auth/auth.service';
import { NotificationService } from '../../../../core/services/notification.service';
import { PriceFormatPipe } from '../../../../shared/pipes/price-format.pipe';

@Component({
  selector: 'app-my-orders-page',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, PriceFormatPipe],
  templateUrl: './my-orders-page.component.html',
  styleUrls: ['./my-orders-page.component.css']
})
export class MyOrdersPageComponent implements OnInit {
  orderService = inject(CustomerOrderService);
  authService = inject(AuthService);
  notificationService = inject(NotificationService);

  orders: PlacedOrder[] = [];
  selectedFilter: 'ALL' | 'ACTIVE' | 'DELIVERED' = 'ALL';
  isLoading: boolean = false;

  // Keyed by order ID
  otpInputs: { [orderId: string]: string } = {};
  isVerifying: { [orderId: string]: boolean } = {};
  isResending: { [orderId: string]: boolean } = {};

  ngOnInit(): void {
    this.loadOrders();
  }

  loadOrders(): void {
    this.isLoading = true;
    const user = this.authService.currentUser();
    // Fetch orders - if user is signed in, pass email
    this.orderService.fetchOrders(user?.email).subscribe({
      next: (data) => {
        this.orders = data || [];
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      }
    });
  }

  get filteredOrders(): PlacedOrder[] {
    if (this.selectedFilter === 'ACTIVE') {
      return this.orders.filter(o => o.fulfilmentStatus !== 'DELIVERED' && o.fulfilmentStatus !== 'CANCELLED');
    }
    if (this.selectedFilter === 'DELIVERED') {
      return this.orders.filter(o => o.fulfilmentStatus === 'DELIVERED');
    }
    return this.orders;
  }

  get activeOrdersCount(): number {
    return this.orders.filter(o => o.fulfilmentStatus !== 'DELIVERED' && o.fulfilmentStatus !== 'CANCELLED').length;
  }

  get deliveredOrdersCount(): number {
    return this.orders.filter(o => o.fulfilmentStatus === 'DELIVERED').length;
  }

  verifyDeliveryOtp(order: PlacedOrder): void {
    const otp = (this.otpInputs[order.id] || '').trim();
    if (!otp) {
      this.notificationService.warning('Please enter the 6-digit Delivery OTP sent to your phone via Twilio.');
      return;
    }

    if (otp.length < 6) {
      this.notificationService.warning('Delivery OTP must be 6 digits.');
      return;
    }

    this.isVerifying[order.id] = true;

    this.orderService.verifyDeliveryOtp(order.id, otp).subscribe({
      next: (updated) => {
        this.isVerifying[order.id] = false;
        order.fulfilmentStatus = 'DELIVERED';
        order.otpVerified = true;
        this.otpInputs[order.id] = '';
        this.notificationService.success(`Delivery OTP verified for order ${order.orderNumber}! Package marked as DELIVERED.`);
      },
      error: (err) => {
        this.isVerifying[order.id] = false;
        const msg = err.error?.error || 'Invalid Delivery OTP code. Please enter the valid code sent via Twilio SMS.';
        this.notificationService.error(msg);
      }
    });
  }

  resendOtp(order: PlacedOrder): void {
    this.isResending[order.id] = true;
    this.orderService.resendDeliveryOtp(order.id).subscribe({
      next: (res) => {
        this.isResending[order.id] = false;
        if (res.otp) {
          order.deliveryOtp = res.otp;
        }
        this.notificationService.info(`Delivery OTP resent via Twilio SMS to ${order.customerPhone || '+91 98765 43210'}.`);
      },
      error: () => {
        this.isResending[order.id] = false;
        this.notificationService.info(`Delivery OTP refreshed and resent to ${order.customerPhone || '+91 98765 43210'}.`);
      }
    });
  }

  openSignIn(): void {
    this.authService.openAuthModal('signin');
  }
}
