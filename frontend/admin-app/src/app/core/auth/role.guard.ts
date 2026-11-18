import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from './auth.service';
import { NotificationService } from '../services/notification.service';

export const roleGuard = (requiredRoles: string[]): CanActivateFn => {
  return () => {
    const authService = inject(AuthService);
    const router = inject(Router);
    const notificationService = inject(NotificationService);

    const user = authService.currentUser();
    if (!user) {
      router.navigate(['/login']);
      return false;
    }

    const hasPermission = requiredRoles.some(r => user.roles.includes(r));
    if (!hasPermission) {
      notificationService.error(`Access Denied: Requires one of [${requiredRoles.join(', ')}]`);
      router.navigate(['/login']);
      return false;
    }

    return true;
  };
};
