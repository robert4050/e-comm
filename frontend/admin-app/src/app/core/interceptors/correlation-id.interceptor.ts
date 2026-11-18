import { HttpInterceptorFn } from '@angular/common/http';

export const correlationIdInterceptor: HttpInterceptorFn = (req, next) => {
  const correlationId = req.headers.get('X-Correlation-ID') || crypto.randomUUID();
  const cloned = req.clone({
    setHeaders: {
      'X-Correlation-ID': correlationId
    }
  });
  return next(cloned);
};
