import { TipoTramite } from './citas.model';

export type EstadoTramite =
  | 'REGISTRADO'
  | 'PENDIENTE_PAGO'
  | 'PAGADO'
  | 'EN_EMISION'
  | 'EMITIDO'
  | 'OBSERVADO'
  | 'RECHAZADO';

export type EstadoPago = 'PENDIENTE' | 'CONFIRMADO' | 'ANULADO';

export interface IniciarTramiteRequest {
  dni: string;
  email: string;
  tipoTramite: TipoTramite;
  pais: string;
  folios?: number;
  idioma?: string;
}

export interface TramiteDto {
  id: string;
  codigo: string;
  dni: string;
  nombresCiudadano?: string;
  email?: string;
  tipoTramite: TipoTramite;
  pais?: string;
  estado: EstadoTramite;
  monto?: number;
  moneda?: string;
  numeroDocumento?: string;
  creadoEn?: string;
  actualizadoEn?: string;
}

export interface EventoHistorialDto {
  version: number;
  tipo: string;
  datos: Record<string, unknown>;
  ocurridoEn: string;
}

export interface RegistrarPagoRequest {
  tramiteId: string;
  monto: number;
  metodoPago?: string;
}

export interface PagoDto {
  id: string;
  tramiteId: string;
  monto: number;
  estado: EstadoPago;
  claveIdempotencia?: string;
  creadoEn: string;
}

