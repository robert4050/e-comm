import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { OrderService } from '../../../../core/services/order.service';

@Component({
  selector: 'app-dashboard-page',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard-page.component.html',
  styleUrls: ['./dashboard-page.component.css']
})
export class DashboardPageComponent {
  private orderService = inject(OrderService);

  readonly totalOrders = this.orderService.totalOrders;
  readonly processingOrders = this.orderService.processingOrders;
  readonly confirmedOrders = this.orderService.confirmedOrders;
  readonly shippedOrders = this.orderService.shippedOrders;
  readonly deliveredOrders = this.orderService.deliveredOrders;
  readonly cancelledOrders = this.orderService.cancelledOrders;
}
