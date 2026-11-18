import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'catalog',
    pathMatch: 'full'
  },
  {
    path: 'catalog',
    loadChildren: () => import('./features/catalog/catalog.routes').then(m => m.CATALOG_ROUTES)
  },
  {
    path: 'my-orders',
    loadComponent: () => import('./features/orders/pages/my-orders-page/my-orders-page.component').then(m => m.MyOrdersPageComponent)
  },
  {
    path: 'orders',
    redirectTo: 'my-orders',
    pathMatch: 'full'
  },
  {
    path: '**',
    redirectTo: 'catalog'
  }
];
