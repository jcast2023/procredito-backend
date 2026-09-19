import { Component, inject, signal, OnInit, OnDestroy } from '@angular/core';
import { DecimalPipe, DatePipe } from '@angular/common';
import {
  LucideUsers,
  LucideFileText,
  LucideBanknote,
  LucideCheckCircle,
  LucideClock,
  LucideXCircle,
  LucideTrendingUp,
  LucideCalendar,
  LucideRefreshCw
} from '@lucide/angular';
import { DashboardService } from '../../core/services/dashboard.service';
import { DashboardResumen } from '../../core/models';

@Component({
  selector: 'app-dashboard',
  imports: [
    DecimalPipe,
    DatePipe,
    LucideUsers,
    LucideFileText,
    LucideBanknote,
    LucideCheckCircle,
    LucideClock,
    LucideXCircle,
    LucideTrendingUp,
    LucideCalendar,
    LucideRefreshCw
  ],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss'
})
export class DashboardComponent implements OnInit, OnDestroy {

  private readonly dashboardService = inject(DashboardService);
  private readonly refreshInterval = 30000;
  private timer?: ReturnType<typeof setInterval>;

  readonly resumen = signal<DashboardResumen | null>(null);
  readonly cargando = signal(true);
  readonly error = signal<string | null>(null);
  readonly ultimaActualizacion = signal<Date | null>(null);

  ngOnInit(): void {
    this.cargarResumen();
    this.timer = setInterval(() => this.cargarResumen(), this.refreshInterval);
  }

  ngOnDestroy(): void {
    if (this.timer) {
      clearInterval(this.timer);
    }
  }

  cargarResumen(): void {
    // Solo muestra el spinner la primera vez; en recargas automáticas actualiza en silencio
    if (!this.resumen()) {
      this.cargando.set(true);
    }
    this.error.set(null);

    this.dashboardService.obtenerResumen().subscribe({
      next: (data) => {
        this.resumen.set(data);
        this.ultimaActualizacion.set(new Date());
        this.cargando.set(false);
      },
      error: (err) => {
        this.error.set('No se pudo cargar el resumen del dashboard.');
        this.cargando.set(false);
        console.error(err);
      }
    });
  }

  porcentajeDesembolsado(): number {
    const r = this.resumen();
    if (!r || r.montoTotalSolicitado === 0) return 0;
    return Math.min(100, (r.montoTotalDesembolsado / r.montoTotalSolicitado) * 100);
  }
}
