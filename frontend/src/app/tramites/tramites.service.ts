import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ConfigService } from '../core/config/config.service';
import { MiEspacioDto } from '../core/models/sistema.model';
import {
  EventoHistorialDto,
  IniciarTramiteRequest,
  PagoDto,
  RegistrarPagoRequest,
  TramiteDto,
} from '../core/models/tramites.model';

@Injectable({ providedIn: 'root' })
export class TramitesService {
  private readonly http = inject(HttpClient);
  private readonly config = inject(ConfigService);

  iniciarTramiteBff(solicitud: IniciarTramiteRequest): Observable<TramiteDto> {
    return this.http.post<TramiteDto>(`${this.config.apiBaseUrl()}/bff/tramites`, solicitud);
  }

  consultarTramitePorId(tramiteId: string): Observable<TramiteDto> {
    return this.http.get<TramiteDto>(
      `${this.config.apiBaseUrl()}/api/tramites/${encodeURIComponent(tramiteId)}`
    );
  }

  consultarTramitesPorDni(dni: string): Observable<TramiteDto[]> {
    const params = new HttpParams().set('dni', dni);
    return this.http.get<TramiteDto[]>(`${this.config.apiBaseUrl()}/api/tramites`, { params });
  }

  consultarHistorialTramite(tramiteId: string): Observable<EventoHistorialDto[]> {
    return this.http.get<EventoHistorialDto[]>(
      `${this.config.apiBaseUrl()}/api/tramites/${encodeURIComponent(tramiteId)}/historial`
    );
  }

  registrarPago(solicitud: RegistrarPagoRequest, claveIdempotencia?: string): Observable<PagoDto> {
    let headers = new HttpHeaders();
    if (claveIdempotencia && claveIdempotencia.trim().length > 0) {
      headers = headers.set('Idempotency-Key', claveIdempotencia.trim());
    }
    return this.http.post<PagoDto>(`${this.config.apiBaseUrl()}/api/pagos`, solicitud, { headers });
  }

  obtenerMiEspacio(dni: string): Observable<MiEspacioDto> {
    const params = new HttpParams().set('dni', dni);
    return this.http.get<MiEspacioDto>(`${this.config.apiBaseUrl()}/bff/inicio`, { params });
  }
}

