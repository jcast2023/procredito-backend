import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Usuario, UsuarioRequest } from '../models';

@Injectable({ providedIn: 'root' })
export class UsuarioService {

  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/usuarios`;

  /**
   * Lista solo analistas (para asignar clientes).
   */
  listarAnalistas(): Observable<Usuario[]> {
    return this.http.get<Usuario[]>(`${this.apiUrl}/analistas`);
  }

  /**
   * Lista todos los usuarios. Solo admin.
   */
  listarTodos(): Observable<Usuario[]> {
    return this.http.get<Usuario[]>(this.apiUrl);
  }

  /**
   * Obtiene un usuario por ID. Solo admin.
   */
  obtenerPorId(id: number): Observable<Usuario> {
    return this.http.get<Usuario>(`${this.apiUrl}/${id}`);
  }

  /**
   * Crea un nuevo usuario. Solo admin.
   */
  crear(request: UsuarioRequest): Observable<Usuario> {
    return this.http.post<Usuario>(this.apiUrl, request);
  }

  /**
   * Actualiza un usuario. Solo admin.
   */
  actualizar(id: number, request: UsuarioRequest): Observable<Usuario> {
    return this.http.put<Usuario>(`${this.apiUrl}/${id}`, request);
  }

  /**
   * Activa o desactiva un usuario (soft delete). Solo admin.
   */
  cambiarActivo(id: number, activo: boolean): Observable<Usuario> {
    return this.http.patch<Usuario>(`${this.apiUrl}/${id}/activo?activo=${activo}`, {});
  }
}
