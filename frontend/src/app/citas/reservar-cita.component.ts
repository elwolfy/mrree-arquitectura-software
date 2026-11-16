import { CurrencyPipe } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MenuItem } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { DatePickerModule } from 'primeng/datepicker';
import { InputTextModule } from 'primeng/inputtext';
import { MessageModule } from 'primeng/message';
import { SelectModule } from 'primeng/select';
import { StepsModule } from 'primeng/steps';
import { TagModule } from 'primeng/tag';
import {
  CitaDto,
  CupoDisponibilidadDto,
  DisponibilidadDto,
  OficinaDto,
  TarifaDto,
  TipoTramite,
} from '../core/models/citas.model';
import { ErrorStateService } from '../core/services/error-state.service';
import { CitasService } from './citas.service';

@Component({
  selector: 'app-reservar-cita',
  standalone: true,
  imports: [
    FormsModule,
    RouterLink,
    CurrencyPipe,
    StepsModule,
    ButtonModule,
    DatePickerModule,
    InputTextModule,
    SelectModule,
    TagModule,
    MessageModule,
  ],
  template: `
    <div class="tarjeta-seccion">
      <h2 style="margin-top: 0;">Reservar Cita Consular</h2>
      <p-steps [model]="pasos" [ activeIndex]="pasoActual()" [readonly]="true" />
    </div>

    <!-- Paso 0: Selección de Oficina -->
    @if (pasoActual() === 0) {
      <div class="tarjeta-seccion" data-testid="paso-oficina">
        <h3 style="margin-top: 0;">1. Seleccione la Oficina Consular</h3>
        <div class="grilla-3">
          @for (oficina of oficinas(); track oficina.id) {
            <div
              class="tarjeta-oficina"
              [class.seleccionada]="oficinaSeleccionada()?.id === oficina.id"
              (click)="seleccionarOficina(oficina)"
              [attr.data-testid]="'oficina-' + oficina.id"
            >
              <div style="display: flex; justify-content: space-between; align-items: center;">
                <strong>{{ oficina.ciudad }} ({{ oficina.id }})</strong>
                <p-tag [value]="oficina.pais" severity="info" />
              </div>
              <p style="margin: 0.4rem 0; font-size: 0.88rem; color: var(--text-secondary);">
                {{ oficina.nombre }}
              </p>
              <small style="color: var(--text-secondary);">Zona: {{ oficina.zonaHoraria }}</small>
            </div>
          }
        </div>
        <div class="fila-acciones">
          <p-button
            label="Siguiente: Elegir fecha"
            icon="pi pi-arrow-right"
            iconPos="right"
            [disabled]="!oficinaSeleccionada()"
            (onClick)="irAPasoFecha()"
          />
        </div>
      </div>
    }

    <!-- Paso 1: Selección de Fecha -->
    @if (pasoActual() === 1) {
      <div class="tarjeta-seccion" data-testid="paso-fecha">
        <h3 style="margin-top: 0;">
          2. Seleccione la Fecha de Atención ({{ oficinaSeleccionada()?.ciudad }})
        </h3>
        <p style="color: var(--text-secondary);">
          Las reservas están habilitadas desde mañana hasta 30 días calendario en adelante.
        </p>

        <div class="campo-formulario" style="max-width: 340px;">
          <label for="fechaCita">Fecha de la cita</label>
          <p-datepicker
            inputId="fechaCita"
            [(ngModel)]="fechaSeleccionadaDate"
            [minDate]="fechaMinima"
            [maxDate]="fechaMaxima"
            dateFormat="yy-mm-dd"
            [showIcon]="true"
            placeholder="YYYY-MM-DD"
          />
        </div>

        <div class="fila-acciones">
          <p-button
            label="Anterior"
            icon="pi pi-arrow-left"
            [outlined]="true"
            (onClick)="pasoActual.set(0)"
          />
          <p-button
            label="Consultar disponibilidad"
            icon="pi pi-search"
            iconPos="right"
            [disabled]="!fechaSeleccionadaDate"
            [loading]="cargandoDisponibilidad()"
            (onClick)="cargarDisponibilidad()"
          />
        </div>
      </div>
    }

    <!-- Paso 2: Selección de Cupo y Origen de Datos (CAP) -->
    @if (pasoActual() === 2) {
      <div class="tarjeta-seccion" data-testid="paso-cupo">
        <div class="encabezado-disponibilidad">
          <div>
            <h3 style="margin: 0;">
              3. Seleccione un Horario Disponible ({{ oficinaSeleccionada()?.id }} — {{ fechaIso() }})
            </h3>
            <p style="margin: 0.25rem 0 0; color: var(--text-secondary); font-size: 0.88rem;">
              Seleccione un cupo con capacidad libre.
            </p>
          </div>

          @if (disponibilidad(); as disp) {
            <div class="insignia-origen" data-testid="disponibilidad-origen">
              <span style="font-size: 0.82rem; color: var(--text-secondary);">Origen de los datos:</span>
              <p-tag
                [value]="etiquetaOrigen()"
                [severity]="disp.origen === 'CACHE' ? 'warn' : 'success'"
                data-testid="etiqueta-origen-cap"
              />
              <p-button
                icon="pi pi-refresh"
                size="small"
                [outlined]="true"
                title="Refrescar disponibilidad"
                (onClick)="cargarDisponibilidad()"
              />
            </div>
          }
        </div>

        @if (disponibilidad(); as disp) {
          @if (disp.cupos.length === 0) {
            <p-message severity="warn">
              No hay turnos programados para la fecha seleccionada (verifique que sea día hábil).
            </p-message>
          } @else {
            <div class="grilla-cupos" style="margin-top: 1rem;">
              @for (cupo of disp.cupos; track cupo.hora) {
                <button
                  type="button"
                  class="boton-cupo"
                  [class.activo]="horaSeleccionada() === formatearHoraCorta(cupo.hora)"
                  [disabled]="libresDeCupo(cupo) <= 0"
                  (click)="seleccionarCupo(cupo)"
                  [attr.data-testid]="'cupo-' + formatearHoraCorta(cupo.hora)"
                >
                  <span class="hora-cupo">{{ formatearHoraCorta(cupo.hora) }}</span>
                  <span class="capacidad-cupo">
                    Libres: {{ libresDeCupo(cupo) }} / {{ cupo.capacidad }}
                  </span>
                </button>
              }
            </div>
          }
        }

        <div class="fila-acciones">
          <p-button
            label="Cambiar fecha"
            icon="pi pi-arrow-left"
            [outlined]="true"
            (onClick)="pasoActual.set(1)"
          />
          <p-button
            label="Siguiente: Datos del ciudadano"
            icon="pi pi-arrow-right"
            iconPos="right"
            [disabled]="!horaSeleccionada()"
            (onClick)="irAPasoDatos()"
          />
        </div>
      </div>
    }

    <!-- Paso 3: Datos del Ciudadano -->
    @if (pasoActual() === 3) {
      <div class="tarjeta-seccion" data-testid="paso-datos">
        <h3 style="margin-top: 0;">4. Datos del Ciudadano y Tipo de Trámite</h3>
        <p style="color: var(--text-secondary);">
          Oficina: <strong>{{ oficinaSeleccionada()?.id }}</strong> |
          Fecha: <strong>{{ fechaIso() }}</strong> |
          Hora: <strong>{{ horaSeleccionada() }}</strong>
        </p>

        <div class="grilla-2">
          <div class="campo-formulario">
            <label for="dniInput">DNI (8 dígitos)</label>
            <input
              pInputText
              id="dniInput"
              type="text"
              maxlength="8"
              [(ngModel)]="dni"
              placeholder="40000001"
              data-testid="input-dni"
            />
          </div>

          <div class="campo-formulario">
            <label for="nombresInput">Nombres y Apellidos</label>
            <input
              pInputText
              id="nombresInput"
              type="text"
              [(ngModel)]="nombres"
              placeholder="Ana Pérez"
              data-testid="input-nombres"
            />
          </div>

          <div class="campo-formulario">
            <label for="emailInput">Correo electrónico</label>
            <input
              pInputText
              id="emailInput"
              type="email"
              [(ngModel)]="email"
              placeholder="ana@correo.lab"
              data-testid="input-email"
            />
          </div>

          <div class="campo-formulario">
            <label for="tipoTramiteSelect">Tipo de Trámite</label>
            <p-select
              inputId="tipoTramiteSelect"
              [options]="opcionesTramite"
              [(ngModel)]="tipoTramite"
              optionLabel="etiqueta"
              optionValue="valor"
              (onChange)="actualizarTarifa()"
            />
          </div>
        </div>

        @if (tarifaCalculada(); as tarifa) {
          <div class="resumen-tarifa" data-testid="tarifa-estimada">
            <span>Tarifa consular estimada ({{ tarifa.estrategia }}):</span>
            <strong>{{ tarifa.monto | currency: tarifa.moneda : 'symbol' : '1.2-2' : 'es-PE' }}</strong>
          </div>
        }

        <div class="fila-acciones">
          <p-button
            label="Anterior"
            icon="pi pi-arrow-left"
            [outlined]="true"
            (onClick)="pasoActual.set(2)"
          />
          <p-button
            label="Confirmar Reserva"
            icon="pi pi-check"
            [loading]="enviandoReserva()"
            [disabled]="!formularioValido()"
            (onClick)="confirmarReserva()"
            data-testid="btn-confirmar-reserva"
          />
        </div>
      </div>
    }

    <!-- Paso 4: Confirmación y Estado de Notificación -->
    @if (pasoActual() === 4 && citaConfirmada(); as cita) {
      <div class="tarjeta-seccion" data-testid="paso-confirmacion">
        <h3 style="margin-top: 0; color: #15803d;">
          <i class="pi pi-check-circle" style="margin-right: 0.4rem;"></i>
          5. Cita Reservada Exitosamente
        </h3>

        <div class="grilla-3" style="margin: 1rem 0;">
          <div class="caja-confirmacion">
            <span>Código de Cita</span>
            <strong data-testid="cita-codigo">{{ cita.codigo }}</strong>
          </div>
          <div class="caja-confirmacion">
            <span>Estado de la Cita</span>
            <p-tag [value]="cita.estado" severity="success" data-testid="cita-estado" />
          </div>
          <div class="caja-confirmacion">
            <span>Estado de Notificación (S1)</span>
            <p-tag
              [value]="cita.notificacion"
              [severity]="cita.notificacion === 'ENVIADA' ? 'success' : 'warn'"
              data-testid="cita-notificacion"
            />
          </div>
        </div>

        <p style="color: var(--text-secondary); font-size: 0.9rem;">
          Ciudadano: <strong>{{ cita.nombres }}</strong> (DNI {{ cita.dni }}) —
          Oficina <strong>{{ cita.oficinaId }}</strong> el <strong>{{ cita.fecha }}</strong> a las
          <strong>{{ formatearHoraCorta(cita.hora) }}</strong> ({{ cita.tipoTramite }}).
        </p>

        <div class="fila-acciones">
          <p-button
            label="Reservar otra cita"
            icon="pi pi-plus"
            [outlined]="true"
            (onClick)="reiniciar()"
          />
          <a routerLink="/citas/consulta">
            <p-button label="Ir a consulta de citas" icon="pi pi-search" />
          </a>
        </div>
      </div>
    }
  `,
  styles: [
    `
      .tarjeta-oficina {
        border: 1px solid var(--border-subtle);
        border-radius: 8px;
        padding: 1rem;
        cursor: pointer;
        transition: border-color 0.15s, background-color 0.15s;
      }
      .tarjeta-oficina:hover {
        border-color: var(--brand-accent);
      }
      .tarjeta-oficina.seleccionada {
        border: 2px solid var(--brand-primary);
        background: #eff6ff;
      }
      .encabezado-disponibilidad {
        display: flex;
        flex-wrap: wrap;
        justify-content: space-between;
        align-items: center;
        gap: 1rem;
      }
      .insignia-origen {
        display: flex;
        align-items: center;
        gap: 0.5rem;
      }
      .grilla-cupos {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(145px, 1fr));
        gap: 0.75rem;
      }
      .boton-cupo {
        display: flex;
        flex-direction: column;
        align-items: center;
        padding: 0.75rem;
        border: 1px solid var(--border-subtle);
        border-radius: 8px;
        background: #ffffff;
        cursor: pointer;
      }
      .boton-cupo:hover:not(:disabled) {
        border-color: var(--brand-accent);
      }
      .boton-cupo.activo {
        border: 2px solid var(--brand-primary);
        background: #eff6ff;
      }
      .boton-cupo:disabled {
        opacity: 0.45;
        cursor: not-allowed;
      }
      .hora-cupo {
        font-weight: 700;
        font-size: 1.05rem;
      }
      .capacidad-cupo {
        font-size: 0.8rem;
        color: var(--text-secondary);
      }
      .resumen-tarifa {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 0.75rem 1rem;
        background: #f8fafc;
        border: 1px solid var(--border-subtle);
        border-radius: 8px;
        margin-top: 0.5rem;
      }
      .caja-confirmacion {
        display: flex;
        flex-direction: column;
        gap: 0.35rem;
        padding: 0.9rem;
        border: 1px solid var(--border-subtle);
        border-radius: 8px;
        background: #f8fafc;
      }
    `,
  ],
})
export class ReservarCitaComponent implements OnInit {
  private readonly citasService = inject(CitasService);
  private readonly errorState = inject(ErrorStateService);

  readonly pasos: MenuItem[] = [
    { label: 'Oficina' },
    { label: 'Fecha' },
    { label: 'Cupo' },
    { label: 'Datos' },
    { label: 'Confirmación' },
  ];

  readonly opcionesTramite: { etiqueta: string; valor: TipoTramite }[] = [
    { etiqueta: 'Legalización de firma', valor: 'LEGALIZACION' },
    { etiqueta: 'Apostilla de La Haya', valor: 'APOSTILLA' },
    { etiqueta: 'Registro Consular', valor: 'REGISTRO_CONSULAR' },
  ];

  readonly pasoActual = signal<number>(0);
  readonly oficinas = signal<OficinaDto[]>([]);
  readonly oficinaSeleccionada = signal<OficinaDto | null>(null);

  readonly fechaMinima = this.calcularFechaDesplazada(1);
  readonly fechaMaxima = this.calcularFechaDesplazada(30);
  fechaSeleccionadaDate: Date | null = this.calcularProximoDiaHabil();

  readonly disponibilidad = signal<DisponibilidadDto | null>(null);
  readonly cargandoDisponibilidad = signal<boolean>(false);
  readonly horaSeleccionada = signal<string | null>(null);

  dni = '40000001';
  nombres = 'Ana Pérez';
  email = 'ana@correo.lab';
  tipoTramite: TipoTramite = 'LEGALIZACION';

  readonly tarifaCalculada = signal<TarifaDto | null>(null);
  readonly enviandoReserva = signal<boolean>(false);
  readonly citaConfirmada = signal<CitaDto | null>(null);

  readonly etiquetaOrigen = computed(() => {
    const disp = this.disponibilidad();
    if (!disp) {
      return '';
    }
    if (disp.origen === 'CACHE') {
      const segundos = disp.actualizadoHaceSegundos ?? 0;
      return `Caché – actualizado hace ${segundos} s`;
    }
    return 'Base de datos';
  });

  ngOnInit(): void {
    this.citasService.listarOficinas().subscribe({
      next: (lista) => this.oficinas.set(lista),
    });
  }

  seleccionarOficina(oficina: OficinaDto): void {
    this.oficinaSeleccionada.set(oficina);
  }

  irAPasoFecha(): void {
    if (this.oficinaSeleccionada()) {
      this.pasoActual.set(1);
    }
  }

  cargarDisponibilidad(): void {
    const oficina = this.oficinaSeleccionada();
    const fecha = this.fechaIso();
    if (!oficina || !fecha) {
      return;
    }
    this.errorState.limpiarError();
    this.cargandoDisponibilidad.set(true);
    this.horaSeleccionada.set(null);
    this.citasService.consultarDisponibilidad(oficina.id, fecha).subscribe({
      next: (disp) => {
        this.disponibilidad.set(disp);
        this.cargandoDisponibilidad.set(false);
        this.pasoActual.set(2);
      },
      error: () => {
        this.cargandoDisponibilidad.set(false);
      },
    });
  }

  libresDeCupo(cupo: CupoDisponibilidadDto): number {
    if (typeof cupo.libres === 'number') {
      return cupo.libres;
    }
    if (typeof cupo.disponibles === 'number') {
      return cupo.disponibles;
    }
    return Math.max(0, cupo.capacidad - (cupo.ocupados ?? 0));
  }

  formatearHoraCorta(hora: string): string {
    return hora ? hora.substring(0, 5) : '';
  }

  seleccionarCupo(cupo: CupoDisponibilidadDto): void {
    if (this.libresDeCupo(cupo) > 0) {
      this.horaSeleccionada.set(this.formatearHoraCorta(cupo.hora));
    }
  }

  irAPasoDatos(): void {
    if (this.horaSeleccionada()) {
      this.pasoActual.set(3);
      this.actualizarTarifa();
    }
  }

  actualizarTarifa(): void {
    const oficina = this.oficinaSeleccionada();
    if (!oficina) {
      return;
    }
    this.citasService.calcularTarifa(this.tipoTramite, oficina.pais).subscribe({
      next: (t) => this.tarifaCalculada.set(t),
      error: () => this.tarifaCalculada.set(null),
    });
  }

  formularioValido(): boolean {
    return (
      /^\d{8}$/.test(this.dni.trim()) &&
      this.nombres.trim().length >= 3 &&
      this.email.includes('@') &&
      Boolean(this.oficinaSeleccionada()) &&
      Boolean(this.fechaIso()) &&
      Boolean(this.horaSeleccionada())
    );
  }

  confirmarReserva(): void {
    const oficina = this.oficinaSeleccionada();
    const hora = this.horaSeleccionada();
    const fecha = this.fechaIso();
    if (!oficina || !hora || !fecha || !this.formularioValido()) {
      return;
    }

    this.errorState.limpiarError();
    this.enviandoReserva.set(true);
    this.citasService
      .reservarCita({
        dni: this.dni.trim(),
        nombres: this.nombres.trim(),
        email: this.email.trim(),
        oficinaId: oficina.id,
        fecha,
        hora,
        tipoTramite: this.tipoTramite,
      })
      .subscribe({
        next: (cita) => {
          this.citaConfirmada.set(cita);
          this.enviandoReserva.set(false);
          this.pasoActual.set(4);
        },
        error: () => {
          this.enviandoReserva.set(false);
        },
      });
  }

  reiniciar(): void {
    this.citaConfirmada.set(null);
    this.horaSeleccionada.set(null);
    this.pasoActual.set(0);
  }

  fechaIso(): string {
    if (!this.fechaSeleccionadaDate) {
      return '';
    }
    const anio = this.fechaSeleccionadaDate.getFullYear();
    const mes = String(this.fechaSeleccionadaDate.getMonth() + 1).padStart(2, '0');
    const dia = String(this.fechaSeleccionadaDate.getDate()).padStart(2, '0');
    return `${anio}-${mes}-${dia}`;
  }

  private calcularFechaDesplazada(dias: number): Date {
    const fecha = new Date();
    fecha.setHours(0, 0, 0, 0);
    fecha.setDate(fecha.getDate() + dias);
    return fecha;
  }

  private calcularProximoDiaHabil(): Date {
    const fecha = this.calcularFechaDesplazada(2);
    while (fecha.getDay() === 0 || fecha.getDay() === 6) {
      fecha.setDate(fecha.getDate() + 1);
    }
    return fecha;
  }
}

