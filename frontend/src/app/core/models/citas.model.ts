export type TipoTramite = 'LEGALIZACION' | 'APOSTILLA' | 'REGISTRO_CONSULAR';

export type EstadoCita = 'RESERVADA' | 'REPROGRAMADA' | 'CANCELADA' | 'ATENDIDA';

export type EstadoNotificacion = 'ENVIADA' | 'PENDIENTE' | 'FALLIDA';

export interface OficinaDto {
  id: string;
  nombre: string;
  pais: string;
  ciudad: string;
  zonaHoraria: string;
}

export interface CupoDisponibilidadDto {
  cupoId?: string;
  hora: string;
  capacidad: number;
  ocupados?: number;
  libres?: number;
  disponibles?: number;
}

export interface DisponibilidadDto {
  oficinaId: string;
  fecha: string;
  cupos: CupoDisponibilidadDto[];
  origen?: 'BASE_DE_DATOS' | 'CACHE' | string;
  actualizadoHaceSegundos?: number;
  modoConsistencia?: string;
}

export interface RegistrarCitaRequest {
  dni: string;
  nombres: string;
  email: string;
  oficinaId: string;
  fecha: string;
  hora: string;
  tipoTramite: TipoTramite;
}

export interface CitaDto {
  id: string;
  codigo: string;
  dni: string;
  nombres: string;
  email?: string;
  oficinaId: string;
  fecha: string;
  hora: string;
  tipoTramite: TipoTramite;
  estado: EstadoCita;
  notificacion: EstadoNotificacion;
  creadaEn: string;
}

export interface TarifaDto {
  tipoTramite: TipoTramite;
  pais: string;
  moneda: string;
  monto: number;
  estrategia: string;
}

