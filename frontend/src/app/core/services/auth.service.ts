import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthResponse, LoginRequest, Rol } from '../models';

@Injectable({ providedIn: 'root' })
export class AuthService {

  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/auth`;
  private readonly TOKEN_KEY = 'procredito_token';
  private readonly USER_KEY = 'procredito_user';

  // Signal para saber si hay sesión activa (reactivo)
  private readonly _usuario = signal<AuthResponse | null>(this.leerUsuarioDeStorage());

  readonly usuario = this._usuario.asReadonly();

  /**
   * Realiza login contra el backend y guarda el token + datos del usuario.
   */
  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, request).pipe(
      tap(response => {
        localStorage.setItem(this.TOKEN_KEY, response.token);
        localStorage.setItem(this.USER_KEY, JSON.stringify(response));
        this._usuario.set(response);
      })
    );
  }

  /**
   * Registra un nuevo usuario en el backend.
   */
  register(request: {
    username: string;
    password: string;
    nombreCompleto: string;
    rol: Rol;
  }): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/register`, request);
  }

  /**
   * Cierra la sesión y limpia el storage.
   */
  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    this._usuario.set(null);
  }

  /**
   * Devuelve el token JWT actual, o null si no hay sesión.
   */
  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  /**
   * Indica si el usuario está autenticado.
   */
  estaAutenticado(): boolean {
    return this.getToken() !== null;
  }

  /**
   * Devuelve true si el usuario tiene rol ADMIN.
   */
  esAdmin(): boolean {
    return this._usuario()?.rol === 'ADMIN';
  }

  /**
   * Lectura inicial del usuario desde localStorage.
   */
  private leerUsuarioDeStorage(): AuthResponse | null {
    const raw = localStorage.getItem(this.USER_KEY);
    if (!raw) return null;
    try {
      return JSON.parse(raw) as AuthResponse;
    } catch {
      return null;
    }
  }

  /**
 * Solicita un código de restablecimiento de contraseña al correo del usuario.
 */
solicitarReset(email: string): Observable<{ mensaje: string }> {
  return this.http.post<{ mensaje: string }>(`${this.apiUrl}/solicitar-reset`, { email });
}

/**
 * Confirma el restablecimiento con el token recibido por correo y la nueva contraseña.
 */
confirmarReset(token: string, nuevaPassword: string): Observable<{ mensaje: string }> {
  return this.http.post<{ mensaje: string }>(`${this.apiUrl}/confirmar-reset`, { token, nuevaPassword });
}

}
