import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ConfigService } from '../core/config/config.service';
import {
  CitaDto,
  DisponibilidadDto,
  OficinaDto,
  RegistrarCitaRequest,
  TarifaDto,
  TipoTramite,
} from '../core/models/citas.model';
import { InfoSistemaDto } from '../core/models/sistema.model';

@Injectable({ providedIn: 'root' })
export class CitasService {
  private readonly http = inject(HttpClient);
  private readonly config = inject(ConfigService);

  obtenerInfoSistema(): Observable<InfoSistemaDto> {
    return this.http.get<InfoSistemaDto>(`${this.config.apiBaseUrl()}/api/sistema/info`);
  }

  listarOficinas(): Observable<OficinaDto[]> {
    return this.http.get<OficinaDto[]>(`${this.config.apiBaseUrl()}/api/oficinas`);
  }

  consultarDisponibilidad(oficinaId: string, fecha: string): Observable<DisponibilidadDto> {
    const params = new HttpParams().set('fecha', fecha);
    return this.http.get<DisponibilidadDto>(
      `${this.config.apiBaseUrl()}/api/oficinas/${encodeURIComponent(oficinaId)}/disponibilidad`,
      { params }
    );
  }

  reservarCita(solicitud: RegistrarCitaRequest): Observable<CitaDto> {
    return this.http.post<CitaDto>(`${this.config.apiBaseUrl()}/api/citas`, solicitud);
  }

  consultarCitaPorId(citaId: string): Observable<CitaDto> {
    return this.http.get<CitaDto>(
      `${this.config.apiBaseUrl()}/api/citas/${encodeURIComponent(citaId)}`
    );
  }

  consultarCitasPorDni(dni: string): Observable<CitaDto[]> {
    const params = new HttpParams().set('dni', dni);
    return this.http.get<CitaDto[]>(`${this.config.apiBaseUrl()}/api/citas`, { params });
  }

  cancelarCita(citaId: string): Observable<void> {
    return this.http.delete<void>(
      `${this.config.apiBaseUrl()}/api/citas/${encodeURIComponent(citaId)}`
    );
  }

  calcularTarifa(tipoTramite: TipoTramite, pais: string): Observable<TarifaDto> {
    const params = new HttpParams().set('tipoTramite', tipoTramite).set('pais', pais);
    return this.http.get<TarifaDto>(`${this.config.apiBaseUrl()}/api/tarifas`, { params });
  }
}

