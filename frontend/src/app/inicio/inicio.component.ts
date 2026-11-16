import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { CitasService } from '../citas/citas.service';
import { ConfigService } from '../core/config/config.service';
import { InfoSistemaDto } from '../core/models/sistema.model';

@Component({
  selector: 'app-inicio',
  standalone: true,
  imports: [RouterLink, ButtonModule, TagModule],
  template: `
    <div class="tarjeta-seccion">
      <h1 style="margin-top: 0;">Plataforma Digital de Servicios Consulares</h1>
      <p style="color: var(--text-secondary); margin-bottom: 0;">
        Entorno didáctico para explorar estilos arquitectónicos, patrones de integración, consistencia y observabilidad.
      </p>
    </div>

    @if (config.modulo() === 'm1') {
      <div class="tarjeta-seccion" data-testid="tarjeta-sistema-ejecucion">
        <div class="encabezado-sistema">
          <h2 style="margin: 0; font-size: 1.15rem;">
            <i class="pi pi-server" style="margin-right: 0.5rem;"></i>
            Sistema en ejecución
          </h2>
          <p-button
            label="Actualizar"
            icon="pi pi-refresh"
            size="small"
            [outlined]="true"
            (onClick)="cargarInfoSistema()"
          />
        </div>

        @if (infoSistema(); as info) {
          <div class="grilla-3" style="margin-top: 1rem;">
            <div class="item-metadato">
              <span class="etiqueta-metadato">Variante arquitectónica</span>
              <p-tag [value]="info.variante" severity="info" data-testid="info-variante" />
            </div>
            <div class="item-metadato">
              <span class="etiqueta-metadato">Arquitectura interna</span>
              <p-tag [value]="info.arquitecturaInterna" severity="secondary" data-testid="info-arquitectura" />
            </div>
            <div class="item-metadato">
              <span class="etiqueta-metadato">Modo de consistencia (CAP)</span>
              <p-tag
                [value]="info.modoConsistencia"
                [severity]="info.modoConsistencia === 'AP' ? 'warn' : 'success'"
                data-testid="info-consistencia"
              />
            </div>
          </div>
          <p style="margin: 0.75rem 0 0; font-size: 0.82rem; color: var(--text-secondary);">
            Versión del artefacto: <strong>{{ info.version }}</strong>
          </p>
        } @else if (cargandoInfo()) {
          <p style="color: var(--text-secondary); margin-top: 0.75rem;">Consultando estado del backend...</p>
        }
      </div>
    }

    <div class="grilla-2">
      @if (config.funcionalidades().citas) {
        <div class="tarjeta-seccion">
          <h3 style="margin-top: 0;">
            <i class="pi pi-calendar-plus" style="margin-right: 0.4rem;"></i>
            Reservar Cita Consular
          </h3>
          <p style="color: var(--text-secondary);">
            Seleccione una oficina consular, verifique la disponibilidad de cupos en tiempo real y registre su cita.
          </p>
          <div class="fila-acciones">
            <a routerLink="/citas/nueva">
              <p-button label="Reservar cita" icon="pi pi-arrow-right" iconPos="right" />
            </a>
            <a routerLink="/citas/consulta">
              <p-button label="Consultar o cancelar cita" [outlined]="true" icon="pi pi-search" />
            </a>
          </div>
        </div>
      }

      @if (config.funcionalidades().tramites) {
        <div class="tarjeta-seccion">
          <h3 style="margin-top: 0;">
            <i class="pi pi-file-edit" style="margin-right: 0.4rem;"></i>
            Trámites Consulares y Legalización
          </h3>
          <p style="color: var(--text-secondary);">
            Inicie solicitudes de legalización, apostilla o registro consular con validación de identidad PIDE.
          </p>
          <div class="fila-acciones">
            <a routerLink="/tramites/nuevo">
              <p-button label="Iniciar trámite" icon="pi pi-plus" />
            </a>
            <a routerLink="/tramites/consulta">
              <p-button label="Seguimiento de trámite" [outlined]="true" icon="pi pi-clock" />
            </a>
          </div>
        </div>
      }

      @if (config.funcionalidades().bffInicio) {
        <div class="tarjeta-seccion">
          <h3 style="margin-top: 0;">
            <i class="pi pi-id-card" style="margin-right: 0.4rem;"></i>
            Mi Espacio Ciudadano (BFF)
          </h3>
          <p style="color: var(--text-secondary);">
            Vista consolidada de citas, trámites y avisos con degradación elegante ante caída parcial de servicios.
          </p>
          <div class="fila-acciones">
            <a routerLink="/mi-espacio">
              <p-button label="Abrir Mi Espacio" icon="pi pi-external-link" [outlined]="true" />
            </a>
          </div>
        </div>
      }
    </div>
  `,
  styles: [
    `
      .encabezado-sistema {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 1rem;
      }
      .item-metadato {
        display: flex;
        flex-direction: column;
        align-items: flex-start;
        gap: 0.4rem;
        padding: 0.85rem;
        border: 1px solid var(--border-subtle);
        border-radius: 8px;
        background: #f8fafc;
      }
      .etiqueta-metadato {
        font-size: 0.8rem;
        color: var(--text-secondary);
        font-weight: 600;
      }
    `,
  ],
})
export class InicioComponent implements OnInit {
  readonly config = inject(ConfigService);
  private readonly citasService = inject(CitasService);

  readonly infoSistema = signal<InfoSistemaDto | null>(null);
  readonly cargandoInfo = signal<boolean>(false);

  ngOnInit(): void {
    if (this.config.modulo() === 'm1') {
      this.cargarInfoSistema();
    }
  }

  cargarInfoSistema(): void {
    this.cargandoInfo.set(true);
    this.citasService.obtenerInfoSistema().subscribe({
      next: (info) => {
        this.infoSistema.set(info);
        this.cargandoInfo.set(false);
      },
      error: () => {
        this.cargandoInfo.set(false);
      },
    });
  }
}

