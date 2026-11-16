import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { CitaDto } from '../core/models/citas.model';
import { ErrorStateService } from '../core/services/error-state.service';
import { CitasService } from './citas.service';

@Component({
  selector: 'app-consultar-citas',
  standalone: true,
  imports: [FormsModule, ButtonModule, InputTextModule, TableModule, TagModule],
  template: `
    <div class="tarjeta-seccion">
      <h2 style="margin-top: 0;">Consultar y Cancelar Citas</h2>
      <p style="color: var(--text-secondary);">
        Busque las citas de un ciudadano por su número de DNI (8 dígitos) o por el identificador UUID de la cita.
      </p>

      <div class="grilla-2">
        <div class="campo-formulario">
          <label for="dniConsulta">Buscar por DNI</label>
          <div class="fila-acciones" style="margin-top: 0;">
            <input
              pInputText
              id="dniConsulta"
              type="text"
              maxlength="8"
              [(ngModel)]="dniBusqueda"
              placeholder="40000001"
              data-testid="consulta-dni-input"
            />
            <p-button
              label="Buscar por DNI"
              icon="pi pi-search"
              [loading]="cargando()"
              (onClick)="buscarPorDni()"
              data-testid="btn-buscar-dni"
            />
          </div>
        </div>

        <div class="campo-formulario">
          <label for="idConsulta">Buscar por ID (UUID)</label>
          <div class="fila-acciones" style="margin-top: 0;">
            <input
              pInputText
              id="idConsulta"
              type="text"
              [(ngModel)]="citaIdBusqueda"
              placeholder="UUID de la cita"
              data-testid="consulta-id-input"
            />
            <p-button
              label="Buscar por ID"
              icon="pi pi-search"
              [outlined]="true"
              [loading]="cargando()"
              (onClick)="buscarPorId()"
              data-testid="btn-buscar-id"
            />
          </div>
        </div>
      </div>
    </div>

    <div class="tarjeta-seccion">
      <h3 style="margin-top: 0;">Resultados ({{ citas().length }})</h3>
      @if (citas().length === 0) {
        <p style="color: var(--text-secondary);">No se encontraron citas para el criterio ingresado.</p>
      } @else {
        <p-table [value]="citas()" [tableStyle]="{ 'min-width': '50rem' }">
          <ng-template #header>
            <tr>
              <th>Código</th>
              <th>Ciudadano</th>
              <th>Oficina</th>
              <th>Fecha y Hora</th>
              <th>Trámite</th>
              <th>Estado</th>
              <th>Notificación</th>
              <th>Acciones</th>
            </tr>
          </ng-template>
          <ng-template #body let-cita>
            <tr>
              <td><strong>{{ cita.codigo }}</strong></td>
              <td>{{ cita.nombres }} ({{ cita.dni }})</td>
              <td>{{ cita.oficinaId }}</td>
              <td>{{ cita.fecha }} {{ cita.hora?.substring(0, 5) }}</td>
              <td>{{ cita.tipoTramite }}</td>
              <td>
                <p-tag
                  [value]="cita.estado"
                  [severity]="cita.estado === 'CANCELADA' ? 'danger' : 'success'"
                />
              </td>
              <td>
                <p-tag
                  [value]="cita.notificacion"
                  [severity]="cita.notificacion === 'ENVIADA' ? 'success' : 'warn'"
                />
              </td>
              <td>
                @if (cita.estado !== 'CANCELADA' && cita.estado !== 'ATENDIDA') {
                  <p-button
                    label="Cancelar"
                    icon="pi pi-times"
                    severity="danger"
                    size="small"
                    [outlined]="true"
                    (onClick)="cancelar(cita)"
                    [attr.data-testid]="'cancelar-' + cita.id"
                  />
                }
              </td>
            </tr>
          </ng-template>
        </p-table>
      }
    </div>
  `,
})
export class ConsultarCitasComponent {
  private readonly citasService = inject(CitasService);
  private readonly errorState = inject(ErrorStateService);

  dniBusqueda = '40000001';
  citaIdBusqueda = '';
  readonly citas = signal<CitaDto[]>([]);
  readonly cargando = signal<boolean>(false);

  buscarPorDni(): void {
    const dni = this.dniBusqueda.trim();
    if (!/^\d{8}$/.test(dni)) {
      return;
    }
    this.errorState.limpiarError();
    this.cargando.set(true);
    this.citasService.consultarCitasPorDni(dni).subscribe({
      next: (lista) => {
        this.citas.set(lista);
        this.cargando.set(false);
      },
      error: () => {
        this.cargando.set(false);
      },
    });
  }

  buscarPorId(): void {
    const id = this.citaIdBusqueda.trim();
    if (!id) {
      return;
    }
    this.errorState.limpiarError();
    this.cargando.set(true);
    this.citasService.consultarCitaPorId(id).subscribe({
      next: (cita) => {
        this.citas.set([cita]);
        this.cargando.set(false);
      },
      error: () => {
        this.citas.set([]);
        this.cargando.set(false);
      },
    });
  }

  cancelar(cita: CitaDto): void {
    this.errorState.limpiarError();
    this.citasService.cancelarCita(cita.id).subscribe({
      next: () => {
        this.citas.update((actuales) =>
          actuales.map((c) => (c.id === cita.id ? { ...c, estado: 'CANCELADA' } : c))
        );
      },
    });
  }
}

