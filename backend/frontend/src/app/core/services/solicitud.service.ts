import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  Solicitud,
  SolicitudRequest,
  CambioEstadoRequest,
  EstadoSolicitud
} from '../models';

@Injectable({ providedIn: 'root' })
export class SolicitudService {

  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/solicitudes`;

  listarTodas(): Observable<Solicitud[]> {
    return this.http.get<Solicitud[]>(this.apiUrl);
  }

  listarPorEstado(estado: EstadoSolicitud): Observable<Solicitud[]> {
    return this.http.get<Solicitud[]>(`${this.apiUrl}/estado/${estado}`);
  }

  obtenerPorId(id: number): Observable<Solicitud> {
    return this.http.get<Solicitud>(`${this.apiUrl}/${id}`);
  }

  crear(request: SolicitudRequest): Observable<Solicitud> {
    return this.http.post<Solicitud>(this.apiUrl, request);
  }

  cambiarEstado(id: number, request: CambioEstadoRequest): Observable<Solicitud> {
    return this.http.patch<Solicitud>(`${this.apiUrl}/${id}/estado`, request);
  }
}
