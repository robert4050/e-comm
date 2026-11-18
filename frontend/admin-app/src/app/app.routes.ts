import { Routes } from '@angular/router';
import { roleGuard } from './core/auth/role.guard';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'dashboard',
    pathMatch: 'full'
  },
  {
    path: 'login',
    loadComponent: () => import('./features/auth/pages/login-page/login-page.component').then(m => m.LoginPageComponent)
  },
  {
    path: 'dashboard',
    canActivate: [roleGuard(['SUPER_ADMIN', 'ADMIN', 'PRODUCT_MANAGER'])],
    loadChildren: () => import('./features/dashboard/dashboard.routes').then(m => m.DASHBOARD_ROUTES)
  },
  {
    path: 'products',
    canActivate: [roleGuard(['SUPER_ADMIN', 'ADMIN', 'PRODUCT_MANAGER'])],
    loadChildren: () => import('./features/products/products.routes').then(m => m.PRODUCTS_ROUTES)
  },
  {
    path: 'enterprise-stack',
    canActivate: [roleGuard(['SUPER_ADMIN', 'ADMIN', 'PRODUCT_MANAGER'])],
    loadComponent: () => import('./features/enterprise-stack/pages/enterprise-stack-page/enterprise-stack-page.component').then(m => m.EnterpriseStackPageComponent)
  },
  {
    path: 'categories',
    canActivate: [roleGuard(['SUPER_ADMIN', 'ADMIN', 'PRODUCT_MANAGER'])],
    loadComponent: () => import('./features/categories/pages/categories-page/categories-page.component').then(m => m.CategoriesPageComponent)
  },
  {
    path: 'orders',
    canActivate: [roleGuard(['SUPER_ADMIN', 'ADMIN', 'PRODUCT_MANAGER'])],
    loadComponent: () => import('./features/orders/pages/orders-page/orders-page.component').then(m => m.OrdersPageComponent)
  },
  {
    path: 'sagas',
    canActivate: [roleGuard(['SUPER_ADMIN', 'ADMIN', 'PRODUCT_MANAGER'])],
    loadComponent: () => import('./features/sagas/pages/sagas-page/sagas-page.component').then(m => m.SagasPageComponent)
  },
  {
    path: 'users',
    canActivate: [roleGuard(['SUPER_ADMIN', 'ADMIN'])],
    loadComponent: () => import('./features/users/pages/users-page/users-page.component').then(m => m.UsersPageComponent)
  },
  {
    path: '**',
    redirectTo: 'dashboard'
  }
];
