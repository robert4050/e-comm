import { Injectable, signal, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { UserProfile } from '../models/user.model';

declare global {
  interface Window {
    Clerk?: any;
    CLERK_PUBLISHABLE_KEY?: string;
  }
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);

  currentUser = signal<UserProfile | null>(this.loadUser());
  token = signal<string | null>(typeof localStorage !== 'undefined' ? localStorage.getItem('ecomm_token') : null);
  isAuthModalOpen = signal<boolean>(false);
  authModalTab = signal<'signin' | 'signup'>('signin');

  constructor() {
    this.fetchConfigAndInitClerk();
  }

  private fetchConfigAndInitClerk(): void {
    if (typeof window === 'undefined') return;
    this.http.get<any>('/api/v1/config/public').subscribe({
      next: (cfg) => {
        if (cfg?.clerkPublishableKey && !cfg.clerkPublishableKey.includes('placeholder')) {
          window.CLERK_PUBLISHABLE_KEY = cfg.clerkPublishableKey;
          this.loadClerkScript(cfg.clerkPublishableKey);
        } else {
          this.initClerk();
        }
      },
      error: () => this.initClerk()
    });
  }

  private loadClerkScript(key: string): void {
    if (typeof window === 'undefined' || window.Clerk) {
      this.initClerk();
      return;
    }
    const script = document.createElement('script');
    script.setAttribute('data-clerk-publishable-key', key);
    script.async = true;
    script.src = 'https://cdn.jsdelivr.net/npm/@clerk/clerk-js@latest/dist/clerk.browser.js';
    script.onload = async () => {
      if (window.Clerk) {
        await window.Clerk.load();
        this.initClerk();
      }
    };
    document.head.appendChild(script);
  }

  private initClerk(): void {
    if (typeof window !== 'undefined' && window.Clerk) {
      window.Clerk.addListener(async (payload: any) => {
        if (payload.user) {
          const clerkToken = await window.Clerk.session?.getToken();
          const user: UserProfile = {
            id: payload.user.id,
            username: payload.user.firstName ? `${payload.user.firstName} ${payload.user.lastName || ''}`.trim() : (payload.user.username || 'Customer'),
            email: payload.user.primaryEmailAddress?.emailAddress || 'customer@e-comm.local',
            roles: ['CUSTOMER'],
            avatarUrl: payload.user.imageUrl || undefined,
            authProvider: 'clerk'
          };
          this.setDirectSession(user, clerkToken || 'clerk_customer_token');
        }
      });
    }
  }

  openAuthModal(tab: 'signin' | 'signup' = 'signin'): void {
    this.authModalTab.set(tab);
    this.isAuthModalOpen.set(true);
  }

  closeAuthModal(): void {
    this.isAuthModalOpen.set(false);
  }

  loginWithGoogle(): void {
    if (typeof window !== 'undefined' && window.Clerk && window.Clerk.authenticateWithRedirect) {
      window.Clerk.authenticateWithRedirect({
        strategy: 'oauth_google',
        redirectUrl: window.location.origin,
        redirectUrlComplete: window.location.origin
      });
      return;
    }

    // Google Login simulation for local / dev testing
    const googleUser: UserProfile = {
      id: 'goog_cust_' + Math.floor(Math.random() * 9000 + 1000),
      username: 'Alex Rivera',
      email: 'alex.rivera.google@gmail.com',
      roles: ['CUSTOMER'],
      avatarUrl: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100&auto=format&fit=crop&q=80',
      authProvider: 'google'
    };

    const token = this.generateDevToken(googleUser.id, googleUser.email, ['CUSTOMER']);
    this.setDirectSession(googleUser, token);
    this.closeAuthModal();
  }

  loginWithPassword(email: string, password: string): { success: boolean; error?: string } {
    if (!email || !email.includes('@')) {
      return { success: false, error: 'Please enter a valid email address.' };
    }
    if (!password || password.length < 6) {
      return { success: false, error: 'Password must be at least 6 characters.' };
    }

    const username = email.split('@')[0];
    const user: UserProfile = {
      id: 'usr_cust_' + Math.floor(Math.random() * 9000 + 1000),
      username: username.charAt(0).toUpperCase() + username.slice(1),
      email: email,
      roles: ['CUSTOMER'],
      authProvider: 'password'
    };

    const token = this.generateDevToken(user.id, user.email, ['CUSTOMER']);
    this.setDirectSession(user, token);
    this.closeAuthModal();
    return { success: true };
  }

  signUp(name: string, email: string, password: string, confirmPassword?: string): { success: boolean; error?: string } {
    if (!name || name.trim().length < 2) {
      return { success: false, error: 'Please enter your full name.' };
    }
    if (!email || !email.includes('@')) {
      return { success: false, error: 'Please enter a valid email address.' };
    }
    if (!password || password.length < 6) {
      return { success: false, error: 'Password must be at least 6 characters.' };
    }
    if (confirmPassword && password !== confirmPassword) {
      return { success: false, error: 'Passwords do not match.' };
    }

    const user: UserProfile = {
      id: 'usr_cust_' + Math.floor(Math.random() * 9000 + 1000),
      username: name.trim(),
      email: email.trim(),
      roles: ['CUSTOMER'],
      authProvider: 'password'
    };

    const token = this.generateDevToken(user.id, user.email, ['CUSTOMER']);
    this.setDirectSession(user, token);
    this.closeAuthModal();
    return { success: true };
  }

  loginAsDemoCustomer(): void {
    const demoUser: UserProfile = {
      id: 'usr_demo_cust',
      username: 'Sarah Customer',
      email: 'customer@e-comm.local',
      roles: ['CUSTOMER'],
      authProvider: 'password'
    };
    const token = this.generateDevToken(demoUser.id, demoUser.email, ['CUSTOMER']);
    this.setDirectSession(demoUser, token);
    this.closeAuthModal();
  }

  logout(): void {
    if (typeof window !== 'undefined' && window.Clerk && window.Clerk.signOut) {
      window.Clerk.signOut();
    }
    this.currentUser.set(null);
    this.token.set(null);
    if (typeof localStorage !== 'undefined') {
      localStorage.removeItem('ecomm_token');
      localStorage.removeItem('ecomm_user');
    }
  }

  setDirectSession(user: UserProfile, token: string): void {
    this.currentUser.set(user);
    this.token.set(token);
    if (typeof localStorage !== 'undefined') {
      localStorage.setItem('ecomm_token', token);
      localStorage.setItem('ecomm_user', JSON.stringify(user));
    }
  }

  private loadUser(): UserProfile | null {
    if (typeof localStorage !== 'undefined') {
      const saved = localStorage.getItem('ecomm_user');
      if (saved) {
        try {
          return JSON.parse(saved);
        } catch {
          return null;
        }
      }
    }
    return null;
  }

  private generateDevToken(sub: string = 'cust_01', email: string, roles: string[]): string {
    const header = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' }))
      .replace(/=/g, '').replace(/\+/g, '-').replace(/\//g, '_');
    const now = Math.floor(Date.now() / 1000);
    const payload = btoa(JSON.stringify({
      sub: sub,
      email: email,
      iss: 'https://clerk.e-comm.local',
      roles: roles,
      iat: now,
      exp: now + 86400
    })).replace(/=/g, '').replace(/\+/g, '-').replace(/\//g, '_');

    return `${header}.${payload}.dev_token_signature`;
  }
}
