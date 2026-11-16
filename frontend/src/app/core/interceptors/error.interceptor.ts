import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { ProblemDetail } from '../models/problem-detail.model';
import { ErrorStateService } from '../services/error-state.service';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const errorState = inject(ErrorStateService);

  return next(req).pipe(
    catchError((err: unknown) => {
      if (err instanceof HttpErrorResponse) {
        const problem = extraerProblemDetail(err);
        errorState.registrarError(problem);
      }
      return throwError(() => err);
    })
  );
};

export function extraerProblemDetail(error: HttpErrorResponse): ProblemDetail {
  const body = error.error;
  if (body && typeof body === 'object' && ('title' in body || 'detail' in body)) {
    const detalleBase = String(body.detail ?? error.message ?? 'Se produjo un error al procesar la solicitud.');
    const erroresCampos =
      body.errores && typeof body.errores === 'object'
        ? Object.entries(body.errores as Record<string, string>)
            .map(([campo, msg]) => `${campo}: ${msg}`)
            .join(' | ')
        : '';
    return {
      type: typeof body.type === 'string' ? body.type : undefined,
      title: String(body.title ?? `Error HTTP ${error.status}`),
      status: Number(body.status ?? error.status ?? 500),
      detail: erroresCampos ? `${detalleBase} (${erroresCampos})` : detalleBase,
      instance: typeof body.instance === 'string' ? body.instance : undefined,
    };
  }

  if (error.status === 0) {
    return {
      title: 'Servicio no disponible',
      status: 0,
      detail: 'No se pudo establecer conexión con el servidor de API. Verifique que el contenedor del módulo esté en ejecución.',
    };
  }

  return {
    title: `Error HTTP ${error.status}`,
    status: error.status,
    detail: typeof body === 'string' && body.trim().length > 0 ? body : error.message,
  };
}

