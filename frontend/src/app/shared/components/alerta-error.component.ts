import { Component, inject } from '@angular/core';
import { MessageModule } from 'primeng/message';
import { ButtonModule } from 'primeng/button';
import { ErrorStateService } from '../../core/services/error-state.service';

@Component({
  selector: 'app-alerta-error',
  standalone: true,
  imports: [MessageModule, ButtonModule],
  template: `
    @if (errorState.ultimoError(); as problema) {
      <div class="contenedor-alerta" data-testid="problem-detail-banner">
        <p-message severity="error" styleClass="w-full">
          <div class="contenido-problema">
            <div class="texto-problema">
              <strong data-testid="problem-title">{{ problema.title }}</strong>
              @if (problema.status > 0) {
                <span class="codigo-http">(HTTP {{ problema.status }})</span>
              }
              <p data-testid="problem-detail">{{ problema.detail }}</p>
            </div>
            <p-button
              icon="pi pi-times"
              [text]="true"
              severity="danger"
              size="small"
              ariaLabel="Cerrar mensaje de error"
              (onClick)="errorState.limpiarError()"
            />
          </div>
        </p-message>
      </div>
    }
  `,
  styles: [
    `
      .contenedor-alerta {
        margin-bottom: 1.25rem;
      }
      .contenido-problema {
        display: flex;
        align-items: flex-start;
        justify-content: space-between;
        gap: 1rem;
        width: 100%;
      }
      .texto-problema p {
        margin: 0.25rem 0 0;
        font-size: 0.92rem;
      }
      .codigo-http {
        margin-left: 0.4rem;
        font-size: 0.82rem;
        opacity: 0.85;
      }
    `,
  ],
})
export class AlertaErrorComponent {
  readonly errorState = inject(ErrorStateService);
}

