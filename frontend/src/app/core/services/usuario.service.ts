import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Usuario } from '../models';

@Injectable({ providedIn: 'root' })
export class UsuarioService {

  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/usuarios`;

  /**
   * Lista solo los usuarios con rol ANALISTA.
   * Solo ADMIN puede ejecutarlo.
   */
  listarAnalistas(): Observable<Usuario[]> {
    return this.http.get<Usuario[]>(`${this.apiUrl}/analistas`);
  }
}
