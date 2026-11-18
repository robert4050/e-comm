import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [CommonModule],
  template: `
    <footer class="app-footer">
      <div class="footer-content">
        <p>&copy; 2026 e-comm Store. All rights reserved.</p>
        <p class="sub">Fast, Secure Shopping & Nationwide Express Delivery • 100% Secure Checkout with Razorpay</p>
      </div>
    </footer>
  `,
  styles: [`
    .app-footer {
      background: #0f172a;
      color: #94a3b8;
      padding: 2rem 1.5rem;
      border-top: 1px solid #1e293b;
      margin-top: auto;
      text-align: center;
      font-size: 0.875rem;
    }
    .footer-content .sub {
      margin-top: 0.5rem;
      font-size: 0.75rem;
      color: #64748b;
    }
  `]
})
export class FooterComponent {}
