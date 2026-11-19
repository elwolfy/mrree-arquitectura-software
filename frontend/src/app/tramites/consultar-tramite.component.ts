import { DatePipe } from '@angular/common';
import { Component, OnDestroy, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Subscription, interval, take } from 'rxjs';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { MessageModule } from 'primeng/message';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { TimelineModule } from 'primeng/timeline';
import { ConfigService } from '../core/config/config.service';
import { EstadoTramite, EventoHistorialDto, TramiteDto } from '../core/models/tramites.model';
import { ErrorStateService } from '../core/services/error-state.service';
import { TramitesService } from './tramites.service';

@Component({
  selector: 'app-consultar-tramite',
  standalone: true,
  imports: [
    FormsModule,
    RouterLink,
    DatePipe,
    ButtonModule,
    InputTextModule,
    MessageModule,
    TagModule,
    TimelineModule,
    TableModule,
  ],
  template: `
    <div class="tarjeta-seccion">
      <h2 style="margin-top: 0;">Consulta y Seguimiento de Trámite</h2>
      <p style="color: var(--text-secondary);">
        Consulte el estado de un trámite y observe su evolución mediante sondeo automático cada 2 segundos durante 30 segundos.
      </p>

      <div class="fila-acciones">
        <input
          pInputText
          type="text"
          [(ngModel)]="tramiteIdBusqueda"
          placeholder="UUID del trámite"
          style="width: 340px;"
        />
        <p-button
          label="Consultar e Iniciar Sondeo (30 s)"
          icon="pi pi-sync"
          (onClick)="iniciarSeguimiento()"
        />
        @if (sondeando()) {
          <p-button
            label="Detener sondeo"
            icon="pi pi-stop"
            severity="secondary"
            [outlined]="true"
            (onClick)="detenerSondeo()"
          />
        }
      </div>
    </div>

    @if (sondeando()) {
      <div style="margin-bottom: 1rem;" data-testid="aviso-sondeo-asincrono">
        <p-message severity="info" styleClass="w-full">
          <i class="pi pi-spin pi-spinner" style="margin-right: 0.5rem;"></i>
          <strong>El estado se actualiza de forma asíncrona</strong> (sondeando cada 2 s...).
        </p-message>
      </div>
    }

    @if (tramiteActual(); as tramite) {
      <div class="tarjeta-seccion">
        <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 1rem;">
          <div>
            <h3 style="margin: 0;">Trámite {{ tramite.codigo }} ({{ tramite.tipoTramite }})</h3>
            <small style="color: var(--text-secondary);">ID: {{ tramite.id }} | DNI: {{ tramite.dni }}</small>
            @if (tramite.numeroDocumento) {
              <div style="margin-top: 0.4rem; color: #15803d; font-weight: 600;">
                Documento emitido: <code>{{ tramite.numeroDocumento }}</code>
              </div>
            }
            @if (tramite.motivoRechazo) {
              <div style="margin-top: 0.4rem; color: #b91c1c; font-weight: 600;">
                Motivo de rechazo: {{ tramite.motivoRechazo }}
              </div>
            }
          </div>
          <p-tag [value]="tramite.estado" severity="info" />
        </div>

        <div style="margin: 1.5rem 0;">
          <p-timeline [value]="pasosLineaTiempo()" layout="horizontal" align="top">
            <ng-template #content let-paso>
              <span [style.font-weight]="paso.activo ? '700' : '400'" [style.color]="paso.alcanzado ? '#1e3a8a' : '#94a3b8'">
                {{ paso.etiqueta }}
              </span>
            </ng-template>
          </p-timeline>
        </div>

        @if (config.funcionalidades().pagos && tramite.estado === 'PENDIENTE_PAGO') {
          <div class="fila-acciones">
            <a [routerLink]="['/tramites', tramite.id, 'pago']" [queryParams]="{ monto: tramite.monto || 30 }">
              <p-button label="Pagar Trámite" icon="pi pi-credit-card" />
            </a>
          </div>
        }
      </div>
    }

    @if (config.funcionalidades().historial && historial().length > 0) {
      <div class="tarjeta-seccion" data-testid="seccion-historial-eventos">
        <h3 style="margin-top: 0;">Historial de Eventos (Event Sourcing)</h3>
        <p-table [value]="historial()">
          <ng-template #header>
            <tr>
              <th>Versión</th>
              <th>Tipo de Evento</th>
              <th>Ocurrido En</th>
            </tr>
          </ng-template>
          <ng-template #body let-ev>
            <tr>
              <td><strong>v{{ ev.version }}</strong></td>
              <td><p-tag [value]="ev.tipo" severity="secondary" /></td>
              <td>{{ ev.ocurridoEn | date: 'medium' }}</td>
            </tr>
          </ng-template>
        </p-table>
      </div>
    }
  `,
})
export class ConsultarTramiteComponent implements OnInit, OnDestroy {
  readonly config = inject(ConfigService);
  private readonly route = inject(ActivatedRoute);
  private readonly tramitesService = inject(TramitesService);
  private readonly errorState = inject(ErrorStateService);

  private readonly ordenEstados: EstadoTramite[] = [
    'REGISTRADO',
    'PENDIENTE_PAGO',
    'PAGADO',
    'EN_EMISION',
    'EMITIDO',
  ];

  tramiteIdBusqueda = '';
  readonly tramiteActual = signal<TramiteDto | null>(null);
  readonly historial = signal<EventoHistorialDto[]>([]);
  readonly sondeando = signal<boolean>(false);

  private subSondeo?: Subscription;

  readonly pasosLineaTiempo = computed(() => {
    const actual = this.tramiteActual()?.estado;
    const idxActual = actual ? this.ordenEstados.indexOf(actual) : -1;
    return this.ordenEstados.map((estado, idx) => ({
      etiqueta: estado,
      alcanzado: idxActual >= idx,
      activo: actual === estado,
    }));
  });

  ngOnInit(): void {
    const idParam = this.route.snapshot.queryParamMap.get('id');
    if (idParam) {
      this.tramiteIdBusqueda = idParam;
      this.iniciarSeguimiento();
    }
  }

  ngOnDestroy(): void {
    this.detenerSondeo();
  }

  iniciarSeguimiento(): void {
    const id = this.tramiteIdBusqueda.trim();
    if (!id) {
      return;
    }
    this.detenerSondeo();
    this.errorState.limpiarError();
    this.cargarUnaVez(id);

    this.sondeando.set(true);
    this.subSondeo = interval(2000)
      .pipe(take(15))
      .subscribe({
        next: () => this.cargarUnaVez(id),
        complete: () => this.sondeando.set(false),
      });
  }

  detenerSondeo(): void {
    this.subSondeo?.unsubscribe();
    this.sondeando.set(false);
  }

  private cargarUnaVez(id: string): void {
    this.tramitesService.consultarTramitePorId(id).subscribe({
      next: (t) => {
        this.tramiteActual.set(t);
        if (t.estado === 'EMITIDO' || t.estado === 'RECHAZADO') {
          this.detenerSondeo();
        }
      },
      error: () => this.detenerSondeo(),
    });

    if (this.config.funcionalidades().historial) {
      this.tramitesService.consultarHistorialTramite(id).subscribe({
        next: (eventos) => this.historial.set(eventos),
        error: () => this.historial.set([]),
      });
    }
  }
}

