import { Component, inject, signal, OnInit, computed } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { DecimalPipe } from '@angular/common';
import Swal from 'sweetalert2';
import { SolicitudService } from '../../../core/services/solicitud.service';
import { ClienteService } from '../../../core/services/cliente.service';
import { AuthService } from '../../../core/services/auth.service';
import { Cliente, SolicitudRequest } from '../../../core/models';

@Component({
  selector: 'app-form-solicitud',
  imports: [FormsModule, RouterLink, DecimalPipe],
  templateUrl: './form-solicitud.html',
  styleUrl: './form-solicitud.scss'
})
export class FormSolicitudComponent implements OnInit {

  private readonly solicitudService = inject(SolicitudService);
  private readonly clienteService = inject(ClienteService);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly clientes = signal<Cliente[]>([]);
  readonly cargandoClientes = signal(true);
  readonly guardando = signal(false);
  readonly error = signal<string | null>(null);

  clienteId = signal<number | null>(null);
  montoSolicitado = signal<number | null>(null);
  tasaInteres = signal<number>(18);
  plazoMeses = signal<number>(12);

  readonly cuotaEstimada = computed(() => {
    const monto = this.montoSolicitado();
    const tasa = this.tasaInteres();
    const plazo = this.plazoMeses();

    if (!monto || !tasa || !plazo) return null;

    const tasaMensual = tasa / 100 / 12;
    const unoMasI = 1 + tasaMensual;
    const potencia = Math.pow(unoMasI, plazo);
    const cuota = monto * (tasaMensual * potencia) / (potencia - 1);

    return Math.round(cuota * 100) / 100;
  });

  readonly totalPagar = computed(() => {
    const cuota = this.cuotaEstimada();
    const plazo = this.plazoMeses();
    if (!cuota || !plazo) return null;
    return Math.round(cuota * plazo * 100) / 100;
  });

  readonly interesesTotales = computed(() => {
    const monto = this.montoSolicitado();
    const total = this.totalPagar();
    if (!monto || !total) return null;
    return Math.round((total - monto) * 100) / 100;
  });

  ngOnInit(): void {
    // Bloqueo: admin no crea solicitudes
    if (this.authService.esAdmin()) {
      Swal.fire({
        title: 'Acción no permitida',
        text: 'El administrador no crea solicitudes. Solo supervisa. Pida al analista asignado que lo haga.',
        icon: 'info',
        confirmButtonColor: '#1e3a8a'
      }).then(() => {
        this.router.navigate(['/solicitudes']);
      });
      return;
    }

    this.cargarClientes();
  }

  cargarClientes(): void {
    this.cargandoClientes.set(true);

    this.clienteService.listarTodos().subscribe({
      next: (data) => {
        this.clientes.set(data);
        this.cargandoClientes.set(false);

        if (data.length === 1) {
          this.clienteId.set(data[0].id);
        }
      },
      error: (err) => {
        this.error.set('No se pudieron cargar los clientes.');
        this.cargandoClientes.set(false);
        console.error(err);
      }
    });
  }

  guardar(): void {
    if (!this.clienteId() || !this.montoSolicitado() || !this.tasaInteres() || !this.plazoMeses()) {
      this.error.set('Todos los campos son obligatorios.');
      return;
    }

    if (this.montoSolicitado()! < 100) {
      this.error.set('El monto mínimo es S/ 100.00');
      return;
    }

    if (this.tasaInteres()! < 0.1 || this.tasaInteres()! > 20) {
      this.error.set('La tasa debe estar entre 0.1% y 20.0%');
      return;
    }

    if (this.plazoMeses()! < 1 || this.plazoMeses()! > 60) {
      this.error.set('El plazo debe estar entre 1 y 60 meses');
      return;
    }

    this.guardando.set(true);
    this.error.set(null);

    const request: SolicitudRequest = {
      clienteId: this.clienteId()!,
      montoSolicitado: this.montoSolicitado()!,
      tasaInteres: this.tasaInteres()!,
      plazoMeses: this.plazoMeses()!
    };

    this.solicitudService.crear(request).subscribe({
      next: () => {
        this.guardando.set(false);
        this.router.navigate(['/solicitudes']);
      },
      error: (err) => {
        this.guardando.set(false);
        this.error.set(
          err?.error?.error ||
          err?.error?.message ||
          'No se pudo crear la solicitud.'
        );
        console.error(err);
      }
    });
  }

  cancelar(): void {
    this.router.navigate(['/solicitudes']);
  }
}
