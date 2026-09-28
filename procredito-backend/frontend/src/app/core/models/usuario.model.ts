import { Rol } from './auth-response.model';

export interface Usuario {
  id: number;
  username: string;
  nombreCompleto: string;
  email: string;
  rol: Rol;
  activo: boolean;
}

export interface UsuarioRequest {
  username: string;
  password?: string;      // opcional al editar; obligatorio al crear
  nombreCompleto: string;
  email: string;
  rol: Rol;
}
