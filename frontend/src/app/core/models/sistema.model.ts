import { CitaDto } from './citas.model';
import { TramiteDto } from './tramites.model';

export interface InfoSistemaDto {
  variante: string;
  arquitecturaInterna: string;
  modoConsistencia: 'CP' | 'AP' | string;
  version: string;
  modulo?: string;
}

export interface AvisoDto {
  id: string;
  destinatario: string;
  asunto: string;
  cuerpo: string;
  canal: string;
  estado: string;
  creadoEn: string;
  referencia?: string;
}

export interface MiEspacioDto {
  dni: string;
  citas: CitaDto[];
  tramites: TramiteDto[];
  avisos: AvisoDto[];
  avisosDeDegradacion: string[];
}

