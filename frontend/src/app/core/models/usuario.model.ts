import { Rol } from './auth-response.model';

export interface Usuario {
  id: number;
  username: string;
  nombreCompleto: string;
  rol: Rol;
}
