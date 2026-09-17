export type Rol = 'ADMIN' | 'ANALISTA';

export interface AuthResponse {
  token: string;
  username: string;
  nombreCompleto: string;
  rol: Rol;
}
