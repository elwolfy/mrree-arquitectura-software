import { HttpInterceptorFn } from '@angular/common/http';

export const idempotenciaInterceptor: HttpInterceptorFn = (req, next) => {
  const esPostPagos = req.method.toUpperCase() === 'POST' && /\/api\/pagos(\/|$|\?)/.test(req.url);
  if (esPostPagos && !req.headers.has('Idempotency-Key')) {
    const claveIdempotencia = generarUuid();
    const clonada = req.clone({
      setHeaders: {
        'Idempotency-Key': claveIdempotencia,
      },
    });
    return next(clonada);
  }
  return next(req);
};

function generarUuid(): string {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return crypto.randomUUID();
  }
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const r = (Math.random() * 16) | 0;
    const v = c === 'x' ? r : (r & 0x3) | 0x8;
    return v.toString(16);
  });
}

