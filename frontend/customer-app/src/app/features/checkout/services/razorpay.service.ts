import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

export interface RazorpayPaymentSuccess {
  razorpay_payment_id: string;
  razorpay_order_id?: string;
  razorpay_signature?: string;
}

export interface RazorpayCheckoutOptions {
  key: string;
  amount: number; // in paise (e.g. 10000 = Rs 100.00)
  currency: string;
  name: string;
  description: string;
  image?: string;
  order_id?: string;
  prefill?: {
    name?: string;
    email?: string;
    contact?: string;
  };
  theme?: {
    color?: string;
  };
  handler: (response: RazorpayPaymentSuccess) => void;
  modal?: {
    ondismiss?: () => void;
  };
}

declare global {
  interface Window {
    Razorpay?: any;
    RAZORPAY_KEY_ID?: string;
  }
}

@Injectable({
  providedIn: 'root'
})
export class RazorpayService {
  private scriptLoaded = false;
  testKeyId = 'rzp_test_ecomm_prod_key';

  constructor(private http: HttpClient) {
    this.loadRazorpayScript();
    this.fetchConfig();
  }

  fetchConfig(): void {
    this.http.get<any>('/api/v1/config/public').subscribe({
      next: (res) => {
        if (res?.razorpayKeyId && !res.razorpayKeyId.includes('placeholder')) {
          this.testKeyId = res.razorpayKeyId;
        }
      },
      error: () => {}
    });
  }

  loadRazorpayScript(): Promise<boolean> {
    if (typeof window === 'undefined') return Promise.resolve(false);
    if (this.scriptLoaded || window.Razorpay) {
      this.scriptLoaded = true;
      return Promise.resolve(true);
    }

    return new Promise((resolve) => {
      const script = document.createElement('script');
      script.src = 'https://checkout.razorpay.com/v1/checkout.js';
      script.async = true;
      script.onload = () => {
        this.scriptLoaded = true;
        resolve(true);
      };
      script.onerror = () => {
        this.scriptLoaded = false;
        resolve(false);
      };
      document.body.appendChild(script);
    });
  }

  isLoaded(): boolean {
    return typeof window !== 'undefined' && !!window.Razorpay;
  }
}
