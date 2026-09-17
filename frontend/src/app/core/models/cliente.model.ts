export interface Cliente {
  id: number;
  documento: string;
  nombres: string;
  telefono: string;
  direccion: string;
  tipoNegocio: string;
}

export interface ClienteRequest {
  documento: string;
  nombres: string;
  telefono: string;
  direccion: string;
  tipoNegocio: string;
}
