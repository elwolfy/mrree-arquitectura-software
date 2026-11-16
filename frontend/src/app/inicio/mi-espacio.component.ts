import { DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { MessageModule } from 'primeng/message';
import { TagModule } from 'primeng/tag';
import { MiEspacioDto } from '../core/models/sistema.model';
import { TramitesService } from '../tramites/tramites.service';

@Component({
  selector: 'app-mi-espacio',
  standalone: true,
  imports: [FormsModule, DatePipe, ButtonModule, InputTextModule, MessageModule, TagModule],
  template: `
    <div class="tarjeta-seccion">
      <h2 style="margin-top: 0;">Mi Espacio Ciudadano (Vista Agregada BFF)</h2>
      <p style="color: var(--text-secondary);">
        Consulta agregada en paralelo (<code>GET /bff/inicio?dni=</code>) de citas, trámites y avisos del ciudadano.
      </p>

      <div class="fila-acciones">
        <input
          pInputText
          type="text"
          maxlength="8"
          [(ngModel)]="dniBusqueda"
          placeholder="Ingrese DNI de 8 dígitos (ej. 40000001)"
          style="width: 280px;"
        />
        <p-button
          label="Consultar BFF"
          icon="pi pi-search"
          [loading]="cargando()"
          (onClick)="consultar()"
        />
      </div>
    </div>

    @if (datos(); as espacio) {
      @if (espacio.avisosDeDegradacion && espacio.avisosDeDegradacion.length > 0) {
        <div style="margin-bottom: 1rem;">
          @for (avisoDeg of espacio.avisosDeDegradacion; track avisoDeg) {
            <p-message severity="warn" styleClass="w-full">
              <strong>Degradación parcial detectada:</strong> {{ avisoDeg }}
            </p-message>
          }
        </div>
      }

      <div class="grilla-3">
        <div class="tarjeta-seccion">
          <h3 style="margin-top: 0;">Citas ({{ espacio.citas.length }})</h3>
          @if (espacio.citas.length === 0) {
            <p style="color: var(--text-secondary);">Sin citas registradas.</p>
          } @else {
            @for (cita of espacio.citas; track cita.id) {
              <div class="item-resumen">
                <strong>{{ cita.codigo }}</strong> — {{ cita.oficinaId }} ({{ cita.fecha }} {{ cita.hora }})
                <div>
                  <p-tag [value]="cita.estado" severity="info" />
                </div>
              </div>
            }
          }
        </div>

        <div class="tarjeta-seccion">
          <h3 style="margin-top: 0;">Trámites ({{ espacio.tramites.length }})</h3>
          @if (espacio.tramites.length === 0) {
            <p style="color: var(--text-secondary);">Sin trámites registrados.</p>
          } @else {
            @for (tramite of espacio.tramites; track tramite.id) {
              <div class="item-resumen">
                <strong>{{ tramite.codigo }}</strong> — {{ tramite.tipoTramite }}
                <div>
                  <p-tag [value]="tramite.estado" severity="secondary" />
                </div>
              </div>
            }
          }
        </div>

        <div class="tarjeta-seccion">
          <h3 style="margin-top: 0;">Avisos ({{ espacio.avisos.length }})</h3>
          @if (espacio.avisos.length === 0) {
            <p style="color: var(--text-secondary);">Sin avisos disponibles.</p>
          } @else {
            @for (aviso of espacio.avisos; track aviso.id) {
              <div class="item-resumen">
                <strong>{{ aviso.asunto }}</strong>
                <div style="font-size: 0.8rem; color: var(--text-secondary);">
                  {{ aviso.creadoEn | date: 'short' }} ({{ aviso.canal }})
                </div>
              </div>
            }
          }
        </div>
      </div>
    }
  `,
  styles: [
    `
      .item-resumen {
        padding: 0.65rem 0;
        border-bottom: 1px solid var(--border-subtle);
        display: flex;
        flex-direction: column;
        gap: 0.25rem;
      }
    `,
  ],
})
export class MiEspacioComponent {
  private readonly tramitesService = inject(TramitesService);

  dniBusqueda = '40000001';
  readonly datos = signal<MiEspacioDto | null>(null);
  readonly cargando = signal<boolean>(false);

  consultar(): void {
    const dni = this.dniBusqueda.trim();
    if (!/^\d{8}$/.test(dni)) {
      return;
    }
    this.cargando.set(true);
    this.tramitesService.obtenerMiEspacio(dni).subscribe({
      next: (res) => {
        this.datos.set(res);
        this.cargando.set(false);
      },
      error: () => {
        this.cargando.set(false);
      },
    });
  }
}

