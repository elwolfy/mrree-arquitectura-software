import { CurrencyPipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { InputNumberModule } from 'primeng/inputnumber';
import { InputTextModule } from 'primeng/inputtext';
import { TagModule } from 'primeng/tag';
import { PagoDto } from '../core/models/tramites.model';
import { ErrorStateService } from '../core/services/error-state.service';
import { TramitesService } from './tramites.service';

@Component({
  selector: 'app-pago-tramite',
  standalone: true,
  imports: [
    FormsModule,
    RouterLink,
    CurrencyPipe,
    ButtonModule,
    InputTextModule,
    InputNumberModule,
    TagModule,
  ],
  template: `
    <div class="tarjeta-seccion">
      <h2 style="margin-top: 0;">Pago Simulado con Clave de Idempotencia</h2>
      <p style="color: var(--text-secondary);">
        Envía <code>POST /api/pagos</code> con cabecera <code>Idempotency-Key</code>. Si reenvía la misma clave, el servidor devuelve el pago original sin duplicar el cargo.
      </p>

      <div class="grilla-2">
        <div class="campo-formulario">
          <label for="tramiteIdInput">Identificador del Trámite (UUID)</label>
          <input pInputText id="tramiteIdInput" type="text" [(ngModel)]="tramiteId" />
        </div>

        <div class="campo-formulario">
          <label for="montoInput">Monto a pagar (PEN)</label>
          <p-inputnumber
            inputId="montoInput"
            [(ngModel)]="monto"
            mode="currency"
            currency="PEN"
            locale="es-PE"
          />
        </div>
      </div>

      <div class="campo-formulario">
        <label for="idempotencyInput">Idempotency-Key (UUID generado en el cliente)</label>
        <div class="fila-acciones" style="margin-top: 0;">
          <input
            pInputText
            id="idempotencyInput"
            type="text"
            [(ngModel)]="claveIdempotencia"
            style="flex: 1; font-family: monospace;"
          />
          <p-button
            label="Nueva clave UUID"
            icon="pi pi-refresh"
            [outlined]="true"
            size="small"
            (onClick)="regenerarClave()"
          />
        </div>
      </div>

      <div class="fila-acciones">
        <p-button
          label="Confirmar Pago"
          icon="pi pi-check"
          [loading]="procesando()"
          (onClick)="pagar()"
        />
        @if (pagoConfirmado()) {
          <p-button
            label="Reintentar con la misma Idempotency-Key"
            icon="pi pi-replay"
            severity="secondary"
            [outlined]="true"
            [loading]="procesando()"
            (onClick)="pagar()"
          />
        }
      </div>
    </div>

    @if (pagoConfirmado(); as pago) {
      <div class="tarjeta-seccion">
        <h3 style="margin-top: 0; color: #15803d;">Constancia de Pago</h3>
        <p>
          ID de Pago: <code>{{ pago.id }}</code> |
          Monto: <strong>{{ pago.monto | currency: 'PEN' : 'symbol' : '1.2-2' : 'es-PE' }}</strong> |
          Estado: <p-tag [value]="pago.estado" severity="success" />
        </p>
        <div class="fila-acciones">
          <a routerLink="/tramites/consulta" [queryParams]="{ id: pago.tramiteId }">
            <p-button label="Ver evolución del trámite (Saga)" icon="pi pi-arrow-right" iconPos="right" />
          </a>
        </div>
      </div>
    }
  `,
})
export class PagoTramiteComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly tramitesService = inject(TramitesService);
  private readonly errorState = inject(ErrorStateService);

  tramiteId = '';
  monto = 30;
  claveIdempotencia = this.nuevoUuid();

  readonly procesando = signal<boolean>(false);
  readonly pagoConfirmado = signal<PagoDto | null>(null);

  ngOnInit(): void {
    const idRuta = this.route.snapshot.paramMap.get('id');
    if (idRuta) {
      this.tramiteId = idRuta;
    }
    const montoParam = Number(this.route.snapshot.queryParamMap.get('monto'));
    if (!Number.isNaN(montoParam) && montoParam > 0) {
      this.monto = montoParam;
    }
  }

  regenerarClave(): void {
    this.claveIdempotencia = this.nuevoUuid();
  }

  pagar(): void {
    if (!this.tramiteId.trim()) {
      return;
    }
    this.errorState.limpiarError();
    this.procesando.set(true);
    this.tramitesService
      .registrarPago(
        {
          tramiteId: this.tramiteId.trim(),
          monto: this.monto,
        },
        this.claveIdempotencia
      )
      .subscribe({
        next: (pago) => {
          this.pagoConfirmado.set(pago);
          this.procesando.set(false);
        },
        error: () => {
          this.procesando.set(false);
        },
      });
  }

  private nuevoUuid(): string {
    if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
      return crypto.randomUUID();
    }
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
      const r = (Math.random() * 16) | 0;
      const v = c === 'x' ? r : (r & 0x3) | 0x8;
      return v.toString(16);
    });
  }
}

