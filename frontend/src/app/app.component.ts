import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { ConfigService } from './core/config/config.service';
import { AlertaErrorComponent } from './shared/components/alerta-error.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, AlertaErrorComponent],
  template: `
    <header class="barra-superior">
      <div class="contenedor-barra">
        <a routerLink="/" class="titulo-portal">
          <i class="pi pi-globe"></i>
          <span>Servicios Consulares y Legalización</span>
        </a>

        <nav class="menu-navegacion" data-testid="menu-principal">
          <a routerLink="/" routerLinkActive="activo" [routerLinkActiveOptions]="{ exact: true }">
            Inicio
          </a>

          @if (config.funcionalidades().citas) {
            <a routerLink="/citas/nueva" routerLinkActive="activo" data-testid="nav-reservar-cita">
              Reservar Cita
            </a>
            <a routerLink="/citas/consulta" routerLinkActive="activo" data-testid="nav-consultar-citas">
              Consultar Citas
            </a>
          }

          @if (config.funcionalidades().tramites) {
            <a routerLink="/tramites/nuevo" routerLinkActive="activo" data-testid="nav-nuevo-tramite">
              Iniciar Trámite
            </a>
            <a routerLink="/tramites/consulta" routerLinkActive="activo" data-testid="nav-consultar-tramite">
              Seguimiento Trámite
            </a>
          }

          @if (config.funcionalidades().bffInicio) {
            <a routerLink="/mi-espacio" routerLinkActive="activo" data-testid="nav-mi-espacio">
              Mi Espacio
            </a>
          }
        </nav>
      </div>
    </header>

    <main class="contenedor-principal">
      <app-alerta-error />
      <router-outlet />
    </main>

    <footer class="pie-pagina" data-testid="pie-pagina">
      <span>Módulo activo: <strong data-testid="footer-modulo">{{ config.modulo() }}</strong></span>
      <span class="separador">|</span>
      <span>API Base URL: <code data-testid="footer-api-url">{{ config.apiBaseUrl() }}</code></span>
    </footer>
  `,
  styles: [
    `
      .barra-superior {
        background: var(--brand-primary);
        color: #ffffff;
        box-shadow: 0 2px 4px rgba(15, 23, 42, 0.12);
      }
      .contenedor-barra {
        max-width: 1120px;
        margin: 0 auto;
        padding: 0.85rem 1.25rem;
        display: flex;
        flex-wrap: wrap;
        align-items: center;
        justify-content: space-between;
        gap: 1rem;
      }
      .titulo-portal {
        display: flex;
        align-items: center;
        gap: 0.55rem;
        color: #ffffff;
        text-decoration: none;
        font-weight: 700;
        font-size: 1.05rem;
      }
      .menu-navegacion {
        display: flex;
        flex-wrap: wrap;
        gap: 0.5rem;
      }
      .menu-navegacion a {
        color: rgba(255, 255, 255, 0.88);
        text-decoration: none;
        padding: 0.45rem 0.8rem;
        border-radius: 6px;
        font-size: 0.9rem;
        font-weight: 500;
      }
      .menu-navegacion a:hover,
      .menu-navegacion a.activo {
        background: rgba(255, 255, 255, 0.16);
        color: #ffffff;
      }
      .pie-pagina {
        border-top: 1px solid var(--border-subtle);
        background: #ffffff;
        padding: 0.85rem 1.25rem;
        text-align: center;
        font-size: 0.84rem;
        color: var(--text-secondary);
      }
      .separador {
        margin: 0 0.65rem;
        color: #cbd5e1;
      }
    `,
  ],
})
export class AppComponent {
  readonly config = inject(ConfigService);
}

