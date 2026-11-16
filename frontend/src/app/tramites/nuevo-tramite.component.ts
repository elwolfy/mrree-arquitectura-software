import { CurrencyPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { InputNumberModule } from 'primeng/inputnumber';
import { InputTextModule } from 'primeng/inputtext';
import { SelectModule } from 'primeng/select';
import { TagModule } from 'primeng/tag';
import { ConfigService } from '../core/config/config.service';
import { TipoTramite } from '../core/models/citas.model';
import { TramiteDto } from '../core/models/tramites.model';
import { ErrorStateService } from '../core/services/error-state.service';
import { TramitesService } from './tramites.service';

@Component({
  selector: 'app-nuevo-tramite',
  standalone: true,
  imports: [
    FormsModule,
    RouterLink,
    CurrencyPipe,
    ButtonModule,
    InputTextModule,
    InputNumberModule,
    SelectModule,
    TagModule,
  ],
  template: `
    <div class="tarjeta-seccion">
      <h2 style="margin-top: 0;">Iniciar Trámite Consular (Fachada BFF)</h2>
      <p style="color: var(--text-secondary);">
        Invoca <code>POST /bff/tramites</code>: valida la identidad del ciudadano ante la PIDE simulada, calcula la tarifa según estrategia y registra la solicitud.
      </p>

      <div class="grilla-2">
        <div class="campo-formulario">
          <label for="dniTramite">DNI del ciudadano</label>
          <input
            pInputText
            id="dniTramite"
            type="text"
            maxlength="8"
            [(ngModel)]="dni"
            placeholder="40000001"
          />
          <small>DNIs de prueba: 40000001–40000050 (válidos), 49999999 (fallecido), 48888888 (no encontrado).</small>
        </div>

        <div class="campo-formulario">
          <label for="emailTramite">Correo electrónico</label>
          <input
            pInputText
            id="emailTramite"
            type="email"
            [(ngModel)]="email"
            placeholder="ciudadano@correo.lab"
          />
        </div>

        <div class="campo-formulario">
          <label for="tipoTramiteBff">Tipo de trámite</label>
          <p-select
            inputId="tipoTramiteBff"
            [options]="opcionesTramite"
            [(ngModel)]="tipoTramite"
            optionLabel="etiqueta"
            optionValue="valor"
          />
        </div>

        <div class="campo-formulario">
          <label for="paisDestino">País de destino (ISO-2)</label>
          <input
            pInputText
            id="paisDestino"
            type="text"
            maxlength="2"
            [(ngModel)]="pais"
            placeholder="ES"
          />
        </div>

        <div class="campo-formulario">
          <label for="foliosInput">Número de folios</label>
          <p-inputnumber inputId="foliosInput" [(ngModel)]="folios" [min]="1" [max]="50" />
        </div>

        <div class="campo-formulario">
          <label for="idiomaInput">Idioma del documento</label>
          <input pInputText id="idiomaInput" type="text" [(ngModel)]="idioma" placeholder="ES" />
        </div>
      </div>

      <div class="fila-acciones">
        <p-button
          label="Validar Identidad e Iniciar Trámite"
          icon="pi pi-send"
          [loading]="enviando()"
          (onClick)="iniciar()"
        />
      </div>
    </div>

    @if (tramiteCreado(); as tramite) {
      <div class="tarjeta-seccion">
        <h3 style="margin-top: 0; color: #15803d;">Trámite Registrado: {{ tramite.codigo }}</h3>
        <p>
          Identificador: <code>{{ tramite.id }}</code> |
          Estado: <p-tag [value]="tramite.estado" severity="info" />
          @if (tramite.monto !== undefined) {
            | Monto: <strong>{{ tramite.monto | currency: (tramite.moneda || 'PEN') : 'symbol' : '1.2-2' : 'es-PE' }}</strong>
          }
        </p>
        <div class="fila-acciones">
          @if (config.funcionalidades().pagos) {
            <a [routerLink]="['/tramites', tramite.id, 'pago']" [queryParams]="{ monto: tramite.monto || 30 }">
              <p-button label="Ir a Pago en Línea" icon="pi pi-credit-card" />
            </a>
          }
          <a routerLink="/tramites/consulta" [queryParams]="{ id: tramite.id }">
            <p-button label="Consultar estado en tiempo real" [outlined]="true" icon="pi pi-clock" />
          </a>
        </div>
      </div>
    }
  `,
})
export class NuevoTramiteComponent {
  readonly config = inject(ConfigService);
  private readonly tramitesService = inject(TramitesService);
  private readonly errorState = inject(ErrorStateService);

  readonly opcionesTramite: { etiqueta: string; valor: TipoTramite }[] = [
    { etiqueta: 'Legalización', valor: 'LEGALIZACION' },
    { etiqueta: 'Apostilla', valor: 'APOSTILLA' },
    { etiqueta: 'Registro Consular', valor: 'REGISTRO_CONSULAR' },
  ];

  dni = '40000001';
  email = 'ana@correo.lab';
  tipoTramite: TipoTramite = 'APOSTILLA';
  pais = 'ES';
  folios = 1;
  idioma = 'ES';

  readonly enviando = signal<boolean>(false);
  readonly tramiteCreado = signal<TramiteDto | null>(null);

  iniciar(): void {
    this.errorState.limpiarError();
    this.enviando.set(true);
    this.tramitesService
      .iniciarTramiteBff({
        dni: this.dni.trim(),
        email: this.email.trim(),
        tipoTramite: this.tipoTramite,
        pais: this.pais.trim().toUpperCase(),
        folios: this.folios,
        idioma: this.idioma.trim(),
      })
      .subscribe({
        next: (tramite) => {
          this.tramiteCreado.set(tramite);
          this.enviando.set(false);
        },
        error: () => {
          this.enviando.set(false);
        },
      });
  }
}

