import { inject } from '@angular/core';
import { CanActivateFn, Router, Routes } from '@angular/router';
import { FuncionalidadesConfig } from './core/config/app-config.model';
import { ConfigService } from './core/config/config.service';

function requiereFuncionalidad(clave: keyof FuncionalidadesConfig): CanActivateFn {
  return () => {
    const config = inject(ConfigService);
    const router = inject(Router);
    if (config.funcionalidades()[clave]) {
      return true;
    }
    return router.createUrlTree(['/']);
  };
}

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./inicio/inicio.component').then((m) => m.InicioComponent),
  },
  {
    path: 'citas/nueva',
    canActivate: [requiereFuncionalidad('citas')],
    loadComponent: () =>
      import('./citas/reservar-cita.component').then((m) => m.ReservarCitaComponent),
  },
  {
    path: 'citas/consulta',
    canActivate: [requiereFuncionalidad('citas')],
    loadComponent: () =>
      import('./citas/consultar-citas.component').then((m) => m.ConsultarCitasComponent),
  },
  {
    path: 'tramites/nuevo',
    canActivate: [requiereFuncionalidad('tramites')],
    loadComponent: () =>
      import('./tramites/nuevo-tramite.component').then((m) => m.NuevoTramiteComponent),
  },
  {
    path: 'tramites/:id/pago',
    canActivate: [requiereFuncionalidad('pagos')],
    loadComponent: () =>
      import('./tramites/pago-tramite.component').then((m) => m.PagoTramiteComponent),
  },
  {
    path: 'tramites/consulta',
    canActivate: [requiereFuncionalidad('tramites')],
    loadComponent: () =>
      import('./tramites/consultar-tramite.component').then((m) => m.ConsultarTramiteComponent),
  },
  {
    path: 'mi-espacio',
    canActivate: [requiereFuncionalidad('bffInicio')],
    loadComponent: () => import('./inicio/mi-espacio.component').then((m) => m.MiEspacioComponent),
  },
  {
    path: '**',
    redirectTo: '',
  },
];

