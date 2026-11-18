import { Injectable, signal, computed } from '@angular/core';
import { CartItem } from '../models/cart.model';
import { Product } from '../../catalog/models/product.model';

@Injectable({
  providedIn: 'root'
})
export class CartService {
  private itemsSignal = signal<CartItem[]>(this.loadCartFromStorage());
  private isDrawerOpenSignal = signal<boolean>(false);

  readonly items = this.itemsSignal.asReadonly();
  readonly isDrawerOpen = this.isDrawerOpenSignal.asReadonly();

  readonly totalCount = computed(() =>
    this.itemsSignal().reduce((sum, item) => sum + item.quantity, 0)
  );

  readonly totalPrice = computed(() =>
    this.itemsSignal().reduce((sum, item) => sum + (item.product.price * item.quantity), 0)
  );

  toggleDrawer(): void {
    this.isDrawerOpenSignal.update(open => !open);
  }

  closeDrawer(): void {
    this.isDrawerOpenSignal.set(false);
  }

  openDrawer(): void {
    this.isDrawerOpenSignal.set(true);
  }

  addToCart(product: Product, quantity: number = 1): void {
    const current = this.itemsSignal();
    const existingIndex = current.findIndex(i => i.product.id === product.id);

    if (existingIndex > -1) {
      const updated = [...current];
      updated[existingIndex] = {
        ...updated[existingIndex],
        quantity: updated[existingIndex].quantity + quantity
      };
      this.updateCart(updated);
    } else {
      this.updateCart([...current, { product, quantity }]);
    }
  }

  updateQuantity(productId: string, quantity: number): void {
    if (quantity <= 0) {
      this.removeFromCart(productId);
      return;
    }
    const updated = this.itemsSignal().map(item =>
      item.product.id === productId ? { ...item, quantity } : item
    );
    this.updateCart(updated);
  }

  removeFromCart(productId: string): void {
    const updated = this.itemsSignal().filter(item => item.product.id !== productId);
    this.updateCart(updated);
  }

  clearCart(): void {
    this.updateCart([]);
  }

  private updateCart(items: CartItem[]): void {
    this.itemsSignal.set(items);
    if (typeof localStorage !== 'undefined') {
      localStorage.setItem('ecomm_cart', JSON.stringify(items));
    }
  }

  private loadCartFromStorage(): CartItem[] {
    if (typeof localStorage !== 'undefined') {
      const saved = localStorage.getItem('ecomm_cart');
      if (saved) {
        try {
          return JSON.parse(saved);
        } catch {
          return [];
        }
      }
    }
    return [];
  }
}
