import { Component, inject, signal, OnInit } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { DashboardService } from '../../core/services/dashboard.service';
import { DashboardResumen } from '../../core/models';

@Component({
  selector: 'app-dashboard',
  imports: [DecimalPipe],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss'
})
export class DashboardComponent implements OnInit {

  private readonly dashboardService = inject(DashboardService);

  readonly resumen = signal<DashboardResumen | null>(null);
  readonly cargando = signal(true);
  readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.cargarResumen();
  }

  cargarResumen(): void {
    this.cargando.set(true);
    this.error.set(null);

    this.dashboardService.obtenerResumen().subscribe({
      next: (data) => {
        this.resumen.set(data);
        this.cargando.set(false);
      },
      error: (err) => {
        this.error.set('No se pudo cargar el resumen del dashboard.');
        this.cargando.set(false);
        console.error(err);
      }
    });
  }
}
