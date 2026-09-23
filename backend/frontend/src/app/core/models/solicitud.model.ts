export type EstadoSolicitud = 'PENDIENTE' | 'APROBADO' | 'RECHAZADO' | 'DESEMBOLSADO';

export interface Solicitud {
  id: number;
  clienteId: number;
  clienteNombres: string;
  clienteDocumento: string;
  montoSolicitado: number;
  tasaInteres: number;
  plazoMeses: number;
  cuotaMensual: number;
  estado: EstadoSolicitud;
  fechaSolicitud: string;
  fechaActualizacion: string | null;
}

export interface SolicitudRequest {
  clienteId: number;
  montoSolicitado: number;
  tasaInteres: number;
  plazoMeses: number;
}

export interface CambioEstadoRequest {
  nuevoEstado: EstadoSolicitud;
}
