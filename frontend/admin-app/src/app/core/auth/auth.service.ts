import { Injectable, signal, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { map, catchError, switchMap } from 'rxjs/operators';
import { AdminUser } from '../models/user.model';

declare global {
  interface Window {
    Clerk?: any;
    CLERK_PUBLISHABLE_KEY?: string;
  }
}

/**
 * Clerk Authentication Service for Admin Back-Office Portal.
 * Supports Google Login, Email/Password Staff Login, and Sign Up.
 */
@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);

  currentUser = signal<AdminUser | null>(this.loadUser());
  token = signal<string | null>(typeof localStorage !== 'undefined' ? localStorage.getItem('ecomm_admin_token') : null);
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
          const roles = payload.user.publicMetadata?.roles || ['PRODUCT_MANAGER', 'ADMIN'];
          const user: AdminUser = {
            id: payload.user.id,
            username: payload.user.firstName ? `${payload.user.firstName} ${payload.user.lastName || ''}`.trim() : (payload.user.username || 'Staff'),
            email: payload.user.primaryEmailAddress?.emailAddress || 'admin@e-comm.local',
            roles: Array.isArray(roles) ? roles : [roles],
            avatarUrl: payload.user.imageUrl || undefined,
            authProvider: 'clerk'
          };
          this.currentUser.set(user);
          if (clerkToken) {
            this.token.set(clerkToken);
            localStorage.setItem('ecomm_admin_token', clerkToken);
          }
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

    // Google Workspace Staff Login simulation
    const googleAdmin: AdminUser = {
      id: 'goog_adm_' + Math.floor(Math.random() * 9000 + 1000),
      username: 'Sarah Jenkins',
      email: 'sarah.jenkins@e-comm.local',
      roles: ['PRODUCT_MANAGER', 'ADMIN'],
      avatarUrl: 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=100&auto=format&fit=crop&q=80',
      authProvider: 'google'
    };

    const token = this.createDevJwt(googleAdmin.username, googleAdmin.roles);
    this.setDirectSession(googleAdmin, token);
    this.closeAuthModal();
  }

  loginWithPassword(email: string, password: string): Observable<{ success: boolean; error?: string }> {
    if (!email || !email.includes('@')) {
      return of({ success: false, error: 'Please enter a valid staff email address.' });
    }
    if (!password || password.length < 4) {
      return of({ success: false, error: 'Password must be at least 4 characters.' });
    }

    return this.http.post<any>('/api/v1/auth/login', { email, password }).pipe(
      map(res => {
        const roles = Array.isArray(res.user?.role) 
          ? res.user.role 
          : (res.user?.role === 'SUPER_ADMIN' ? ['SUPER_ADMIN', 'ADMIN'] : (res.user?.role === 'ADMIN' ? ['ADMIN'] : [res.user?.role || 'USER']));
        const user: AdminUser = {
          id: res.user?.id || 'usr_' + Date.now(),
          username: res.user?.fullName || res.user?.email?.split('@')[0] || 'Staff User',
          email: res.user?.email || email,
          roles: roles,
          avatarUrl: res.user?.avatarUrl,
          authProvider: 'password'
        };
        this.setDirectSession(user, res.token);
        this.closeAuthModal();
        return { success: true };
      }),
      catchError(err => {
        // Fallback for offline/local simulation
        if (email.toLowerCase() === 'admin@gmail.com' && (password === 'admin1234' || password === 'admin')) {
          const user: AdminUser = {
            id: 'usr_superadmin_01',
            username: 'Super Administrator',
            email: 'admin@gmail.com',
            roles: ['SUPER_ADMIN', 'ADMIN'],
            avatarUrl: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100&auto=format&fit=crop&q=80',
            authProvider: 'password'
          };
          const token = this.createDevJwt('Super Administrator', user.roles);
          this.setDirectSession(user, token);
          this.closeAuthModal();
          return of({ success: true });
        }
        const errorMsg = err.error?.detail || err.error?.message || 'Invalid email or password.';
        return of({ success: false, error: errorMsg });
      })
    );
  }

  signUp(name: string, email: string, password: string, role: string = 'PRODUCT_MANAGER'): Observable<{ success: boolean; error?: string }> {
    if (!name || name.trim().length < 2) {
      return of({ success: false, error: 'Please enter staff full name.' });
    }
    if (!email || !email.includes('@')) {
      return of({ success: false, error: 'Please enter a valid work email.' });
    }
    if (!password || password.length < 4) {
      return of({ success: false, error: 'Password must be at least 4 characters.' });
    }

    return this.http.post<any>('/api/v1/auth/register', {
      email: email.trim(),
      password: password,
      fullName: name.trim(),
      role: role
    }).pipe(
      switchMap(() => this.loginWithPassword(email, password)),
      catchError(err => {
        const errorMsg = err.error?.detail || err.error?.message || 'Account registration failed.';
        return of({ success: false, error: errorMsg });
      })
    );
  }

  loginDirect(username: string = 'manager', password: string = 'manager123'): Observable<any> {
    const roles = username === 'admin' ? ['ADMIN', 'PRODUCT_MANAGER'] : ['PRODUCT_MANAGER'];
    const user: AdminUser = { username, roles };
    const token = this.createDevJwt(username, roles);

    this.setDirectSession(user, token);
    return of({ access_token: token, user });
  }

  setDirectSession(user: AdminUser, token: string): void {
    this.currentUser.set(user);
    this.token.set(token);
    if (typeof localStorage !== 'undefined') {
      localStorage.setItem('ecomm_admin_token', token);
      localStorage.setItem('ecomm_admin_user', JSON.stringify(user));
    }
  }

  logout(): void {
    if (typeof window !== 'undefined' && window.Clerk && window.Clerk.signOut) {
      window.Clerk.signOut();
    }
    this.currentUser.set(null);
    this.token.set(null);
    if (typeof localStorage !== 'undefined') {
      localStorage.removeItem('ecomm_admin_token');
      localStorage.removeItem('ecomm_admin_user');
    }
  }

  hasRole(role: string): boolean {
    const user = this.currentUser();
    return !!user && user.roles.includes(role);
  }

  private loadUser(): AdminUser | null {
    if (typeof localStorage !== 'undefined') {
      const saved = localStorage.getItem('ecomm_admin_user');
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

  private createDevJwt(username: string, roles: string[]): string {
    const header = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' }))
      .replace(/=/g, '').replace(/\+/g, '-').replace(/\//g, '_');
    const now = Math.floor(Date.now() / 1000);
    const payload = btoa(JSON.stringify({
      sub: 'usr_' + username,
      iss: 'https://clerk.e-comm.local',
      roles: roles,
      iat: now,
      exp: now + 86400
    })).replace(/=/g, '').replace(/\+/g, '-').replace(/\//g, '_');

    return `${header}.${payload}.dev_token_signature`;
  }
}
