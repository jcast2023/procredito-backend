import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { DashboardResumen, AnalistaResumen } from '../models';

@Injectable({ providedIn: 'root' })
export class DashboardService {

  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/dashboard`;

  /**
   * Resumen general. Según rol:
   * - ADMIN: totales globales.
   * - ANALISTA: solo sus totales.
   */
  obtenerResumen(): Observable<DashboardResumen> {
    return this.http.get<DashboardResumen>(`${this.apiUrl}/resumen`);
  }

  /**
   * Resumen por analista. Solo ADMIN.
   */
  obtenerResumenPorAnalista(): Observable<AnalistaResumen[]> {
    return this.http.get<AnalistaResumen[]>(`${this.apiUrl}/por-analista`);
  }
}
