import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { OrderService, OrderItem } from '../../../../core/services/order.service';
import { NotificationService } from '../../../../core/services/notification.service';

export type { OrderItem };

@Component({
  selector: 'app-orders-page',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './orders-page.component.html',
  styleUrls: ['./orders-page.component.css']
})
export class OrdersPageComponent {
  private orderService = inject(OrderService);
  private notificationService = inject(NotificationService);

  selectedFilter: string = 'ALL';

  get orders(): OrderItem[] {
    return this.orderService.orders();
  }

  get filteredOrders(): OrderItem[] {
    if (this.selectedFilter === 'ALL') return this.orders;
    return this.orders.filter(o => o.fulfilmentStatus === this.selectedFilter);
  }

  get processingCount(): number {
    return this.orders.filter(o => o.fulfilmentStatus === 'PROCESSING').length;
  }

  get confirmedCount(): number {
    return this.orders.filter(o => o.fulfilmentStatus === 'CONFIRMED').length;
  }

  get shippedCount(): number {
    return this.orders.filter(o => o.fulfilmentStatus === 'SHIPPED').length;
  }

  get deliveredCount(): number {
    return this.orders.filter(o => o.fulfilmentStatus === 'DELIVERED').length;
  }

  get totalGrossVolume(): number {
    return this.orders.reduce((sum, o) => sum + (o.totalPrice || 0), 0);
  }

  confirmOrder(order: OrderItem): void {
    this.orderService.confirmOrder(order.id);
    this.notificationService.success(`Order ${order.orderNumber} confirmed by admin! Payment verified via Razorpay.`);
  }

  cycleStatus(order: OrderItem): void {
    this.orderService.cycleStatus(order.id);
    this.notificationService.info(`Order ${order.orderNumber} status progressed.`);
  }

  refreshOrders(): void {
    this.orderService.fetchOrders();
    this.notificationService.info('Orders list refreshed from database.');
  }
}
