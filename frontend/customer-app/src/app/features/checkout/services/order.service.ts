import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';

export interface CreateOrderPayload {
  customerName: string;
  customerEmail: string;
  customerPhone?: string;
  itemsCount: number;
  totalPrice: number;
  razorpayPaymentId: string;
}

export interface PlacedOrder {
  id: string;
  orderNumber: string;
  customerName: string;
  customerEmail: string;
  customerPhone?: string;
  itemsCount: number;
  totalPrice: number;
  paymentStatus: string;
  paymentMethod: string;
  razorpayPaymentId: string;
  fulfilmentStatus: 'PROCESSING' | 'CONFIRMED' | 'SHIPPED' | 'DELIVERED' | 'CANCELLED' | string;
  carrier: string;
  trackingNumber: string;
  deliveryOtp?: string;
  otpVerified?: boolean;
  createdAt: string;
}

@Injectable({
  providedIn: 'root'
})
export class CustomerOrderService {
  private http = inject(HttpClient);
  private apiUrl = '/api/v1/orders';

  createOrder(payload: CreateOrderPayload): Observable<PlacedOrder> {
    return this.http.post<PlacedOrder>(this.apiUrl, payload).pipe(
      tap(order => this.saveToLocalStorage(order)),
      catchError(err => {
        console.warn('API Gateway order endpoint unreachable, creating local fallback order:', err);
        const fallbackOrder: PlacedOrder = {
          id: 'ord-' + Date.now().toString().slice(-6),
          orderNumber: 'ORD-' + Math.floor(10000 + Math.random() * 90000),
          customerName: payload.customerName,
          customerEmail: payload.customerEmail,
          customerPhone: payload.customerPhone || '+91 98765 43210',
          itemsCount: payload.itemsCount,
          totalPrice: payload.totalPrice,
          paymentStatus: 'PAID',
          paymentMethod: 'RAZORPAY',
          razorpayPaymentId: payload.razorpayPaymentId,
          fulfilmentStatus: 'PROCESSING',
          carrier: 'Razorpay Express Handoff',
          trackingNumber: 'RPAY-' + Math.floor(10000000 + Math.random() * 90000000),
          deliveryOtp: String(Math.floor(100000 + Math.random() * 900000)),
          otpVerified: false,
          createdAt: new Date().toISOString()
        };
        this.saveToLocalStorage(fallbackOrder);
        return of(fallbackOrder);
      })
    );
  }

  fetchOrders(email?: string): Observable<PlacedOrder[]> {
    const url = email ? `${this.apiUrl}?email=${encodeURIComponent(email)}` : this.apiUrl;
    return this.http.get<PlacedOrder[]>(url).pipe(
      catchError(() => {
        return of(this.loadFromLocalStorage(email));
      })
    );
  }

  verifyDeliveryOtp(orderId: string, otp: string): Observable<PlacedOrder> {
    return this.http.post<PlacedOrder>(`${this.apiUrl}/${orderId}/verify-otp`, { otp }).pipe(
      tap(updatedOrder => this.updateLocalStorageOrder(updatedOrder))
    );
  }

  resendDeliveryOtp(orderId: string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/${orderId}/resend-otp`, {});
  }

  private saveToLocalStorage(order: PlacedOrder): void {
    try {
      const existing = localStorage.getItem('ecomm_shared_orders');
      const list: PlacedOrder[] = existing ? JSON.parse(existing) : [];
      localStorage.setItem('ecomm_shared_orders', JSON.stringify([order, ...list]));
    } catch {}
  }

  private updateLocalStorageOrder(order: PlacedOrder): void {
    try {
      const existing = localStorage.getItem('ecomm_shared_orders');
      if (existing) {
        const list: PlacedOrder[] = JSON.parse(existing);
        const updated = list.map(o => o.id === order.id ? order : o);
        localStorage.setItem('ecomm_shared_orders', JSON.stringify(updated));
      }
    } catch {}
  }

  private loadFromLocalStorage(email?: string): PlacedOrder[] {
    try {
      const existing = localStorage.getItem('ecomm_shared_orders');
      if (existing) {
        let list: PlacedOrder[] = JSON.parse(existing);
        if (email) {
          list = list.filter(o => o.customerEmail?.toLowerCase() === email.toLowerCase());
        }
        return list;
      }
    } catch {}
    return [];
  }
}
