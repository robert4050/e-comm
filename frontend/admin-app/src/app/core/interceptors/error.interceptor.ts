import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { NotificationService } from '../services/notification.service';
import { ProblemDetails } from '../models/problem-details.model';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const notificationService = inject(NotificationService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      let errorMessage = 'An unexpected server error occurred.';

      if (error.error && typeof error.error === 'object') {
        const errObj = error.error as any;
        if (errObj.detail) {
          errorMessage = errObj.detail;
        } else if (errObj.message) {
          errorMessage = errObj.message;
        } else if (errObj.title) {
          errorMessage = errObj.title;
        } else if (errObj.error) {
          errorMessage = typeof errObj.error === 'string' ? errObj.error : JSON.stringify(errObj.error);
        }
      } else if (typeof error.error === 'string' && error.error.trim().length > 0) {
        errorMessage = error.error;
      } else if (error.status === 0) {
        errorMessage = 'Unable to connect to backend service. Check network or server status.';
      } else if (error.status === 401) {
        errorMessage = 'Session expired or authentication required. Please sign in.';
      } else if (error.status === 403) {
        errorMessage = 'Forbidden. Requires elevated administrative roles.';
      }

      console.warn(`[Admin HTTP Error ${error.status}] on ${req.url}:`, errorMessage);
      notificationService.error(errorMessage);
      return throwError(() => error);
    })
  );
};
