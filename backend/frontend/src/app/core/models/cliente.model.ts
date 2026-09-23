export interface Cliente {
  id: number;
  documento: string;
  nombres: string;
  telefono: string;
  direccion: string;
  tipoNegocio: string;
  analistaId: number;
  analistaNombre: string;
}

export interface ClienteRequest {
  documento: string;
  nombres: string;
  telefono: string;
  direccion: string;
  tipoNegocio: string;
  /**
   * Solo lo envía el ADMIN al crear/editar.
   * Si lo crea un ANALISTA, se ignora (el backend auto-asigna).
   */
  analistaId?: number;
}
