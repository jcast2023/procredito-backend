import { Component, inject, signal, OnInit, OnDestroy, computed } from '@angular/core';
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
  LucideRefreshCw,
  LucideUserCog
} from '@lucide/angular';
import { DashboardService } from '../../core/services/dashboard.service';
import { AuthService } from '../../core/services/auth.service';
import { DashboardResumen, AnalistaResumen } from '../../core/models';

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
    LucideRefreshCw,
    LucideUserCog
  ],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss'
})
export class DashboardComponent implements OnInit, OnDestroy {

  private readonly dashboardService = inject(DashboardService);
  private readonly authService = inject(AuthService);

  private readonly refreshInterval = 30000;
  private timer?: ReturnType<typeof setInterval>;

  readonly resumen = signal<DashboardResumen | null>(null);
  readonly analistasResumen = signal<AnalistaResumen[]>([]);
  readonly cargando = signal(true);
  readonly error = signal<string | null>(null);
  readonly ultimaActualizacion = signal<Date | null>(null);

  readonly esAdmin = computed(() => this.authService.esAdmin());

  // Totales calculados de la tabla por analista
  readonly totalesAnalistas = computed(() => {
    const lista = this.analistasResumen();
    return {
      clientes: lista.reduce((sum, a) => sum + a.totalClientes, 0),
      solicitudes: lista.reduce((sum, a) => sum + a.totalSolicitudes, 0),
      montoSolicitado: lista.reduce((sum, a) => sum + a.montoTotalSolicitado, 0),
      montoDesembolsado: lista.reduce((sum, a) => sum + a.montoTotalDesembolsado, 0)
    };
  });

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

    // Cargar resumen por analista solo si es admin
    if (this.esAdmin()) {
      this.dashboardService.obtenerResumenPorAnalista().subscribe({
        next: (data) => this.analistasResumen.set(data),
        error: (err) => console.error('Error al cargar resumen por analista:', err)
      });
    }
  }

  porcentajeDesembolsado(): number {
    const r = this.resumen();
    if (!r || r.montoTotalSolicitado === 0) return 0;
    return Math.min(100, (r.montoTotalDesembolsado / r.montoTotalSolicitado) * 100);
  }
}
