import { Injectable, signal, computed, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { catchError, of } from 'rxjs';

export interface OrderItem {
  id: string;
  orderNumber: string;
  customerName: string;
  customerEmail: string;
  itemsCount: number;
  totalPrice: number;
  paymentStatus: 'PAID' | 'REFUNDED';
  paymentMethod?: string;
  razorpayPaymentId?: string;
  fulfilmentStatus: 'PROCESSING' | 'CONFIRMED' | 'SHIPPED' | 'DELIVERED' | 'CANCELLED';
  carrier: string;
  trackingNumber: string;
  customerPhone?: string;
  deliveryOtp?: string;
  otpVerified?: boolean;
  createdAt: string;
}

@Injectable({
  providedIn: 'root'
})
export class OrderService {
  private http = inject(HttpClient);
  private apiUrl = '/api/v1/orders';

  readonly orders = signal<OrderItem[]>([
    {
      id: 'ord-101',
      orderNumber: 'ORD-84920',
      customerName: 'Alex Rivera',
      customerEmail: 'alex.rivera@example.com',
      itemsCount: 2,
      totalPrice: 2499.99,
      paymentStatus: 'PAID',
      paymentMethod: 'RAZORPAY',
      razorpayPaymentId: 'pay_Nzd8f90248a',
      fulfilmentStatus: 'PROCESSING',
      carrier: 'FedEx Priority',
      trackingNumber: 'FDX-9948201',
      createdAt: 'Today, 14:22'
    },
    {
      id: 'ord-102',
      orderNumber: 'ORD-84921',
      customerName: 'Marcus Vance',
      customerEmail: 'marcus.v@corporate.io',
      itemsCount: 1,
      totalPrice: 1099.00,
      paymentStatus: 'PAID',
      paymentMethod: 'RAZORPAY',
      razorpayPaymentId: 'pay_Nzd8f90248b',
      fulfilmentStatus: 'CONFIRMED',
      carrier: 'DHL Express',
      trackingNumber: 'DHL-3382109',
      createdAt: 'Today, 11:05'
    },
    {
      id: 'ord-103',
      orderNumber: 'ORD-84922',
      customerName: 'Sophia Chen',
      customerEmail: 'sophia.c@design.co',
      itemsCount: 3,
      totalPrice: 1899.00,
      paymentStatus: 'PAID',
      paymentMethod: 'RAZORPAY',
      razorpayPaymentId: 'pay_Nzd8f90248c',
      fulfilmentStatus: 'SHIPPED',
      carrier: 'UPS Worldwide',
      trackingNumber: '1Z9999999999999999',
      createdAt: 'Yesterday'
    },
    {
      id: 'ord-104',
      orderNumber: 'ORD-84923',
      customerName: 'David Miller',
      customerEmail: 'david.m@techlab.org',
      itemsCount: 1,
      totalPrice: 249.00,
      paymentStatus: 'PAID',
      paymentMethod: 'RAZORPAY',
      razorpayPaymentId: 'pay_Nzd8f90248d',
      fulfilmentStatus: 'DELIVERED',
      carrier: 'FedEx Home',
      trackingNumber: 'FDX-7738291',
      createdAt: '15 Nov 2026'
    },
    {
      id: 'ord-105',
      orderNumber: 'ORD-84924',
      customerName: 'Emma Watson',
      customerEmail: 'emma.w@domain.com',
      itemsCount: 1,
      totalPrice: 149.99,
      paymentStatus: 'REFUNDED',
      paymentMethod: 'RAZORPAY',
      razorpayPaymentId: 'pay_Nzd8f90248e',
      fulfilmentStatus: 'CANCELLED',
      carrier: 'Standard',
      trackingNumber: 'N/A',
      createdAt: '14 Nov 2026'
    }
  ]);

  readonly totalOrders = computed(() => this.orders().length);
  readonly processingOrders = computed(() => this.orders().filter(o => o.fulfilmentStatus === 'PROCESSING').length);
  readonly confirmedOrders = computed(() => this.orders().filter(o => o.fulfilmentStatus === 'CONFIRMED').length);
  readonly shippedOrders = computed(() => this.orders().filter(o => o.fulfilmentStatus === 'SHIPPED').length);
  readonly deliveredOrders = computed(() => this.orders().filter(o => o.fulfilmentStatus === 'DELIVERED').length);
  readonly cancelledOrders = computed(() => this.orders().filter(o => o.fulfilmentStatus === 'CANCELLED').length);

  constructor() {
    this.fetchOrders();
  }

  fetchOrders(): void {
    this.http.get<OrderItem[]>(this.apiUrl).pipe(
      catchError(() => {
        // Fallback to local shared storage
        try {
          const shared = localStorage.getItem('ecomm_shared_orders');
          if (shared) {
            const list = JSON.parse(shared);
            return of(list);
          }
        } catch {}
        return of(this.orders());
      })
    ).subscribe((liveOrders) => {
      if (liveOrders && liveOrders.length > 0) {
        this.orders.set(liveOrders);
      }
    });
  }

  confirmOrder(orderId: string): void {
    this.orders.update(orders =>
      orders.map(o => o.id === orderId ? { ...o, fulfilmentStatus: 'CONFIRMED' } : o)
    );

    this.http.put(`${this.apiUrl}/${orderId}/confirm`, {}).pipe(
      catchError(() => of(null))
    ).subscribe();
  }

  cycleStatus(orderId: string): void {
    const sequence: ('PROCESSING' | 'CONFIRMED' | 'SHIPPED' | 'DELIVERED')[] = [
      'PROCESSING',
      'CONFIRMED',
      'SHIPPED',
      'DELIVERED'
    ];
    let nextStatus: 'PROCESSING' | 'CONFIRMED' | 'SHIPPED' | 'DELIVERED' = 'CONFIRMED';

    this.orders.update(orders =>
      orders.map(order => {
        if (order.id !== orderId) return order;
        const currentIndex = sequence.indexOf(order.fulfilmentStatus as any);
        if (currentIndex !== -1 && currentIndex < sequence.length - 1) {
          nextStatus = sequence[currentIndex + 1];
          return { ...order, fulfilmentStatus: nextStatus };
        }
        return order;
      })
    );

    this.http.put(`${this.apiUrl}/${orderId}/status`, { status: nextStatus }).pipe(
      catchError(() => of(null))
    ).subscribe();
  }
}
