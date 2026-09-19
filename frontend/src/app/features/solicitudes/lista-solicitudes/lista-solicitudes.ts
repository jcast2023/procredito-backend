import { Component, inject, signal, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DecimalPipe } from '@angular/common';
import Swal from 'sweetalert2';
import { SolicitudService } from '../../../core/services/solicitud.service';
import { Solicitud, EstadoSolicitud } from '../../../core/models';

@Component({
  selector: 'app-lista-solicitudes',
  imports: [RouterLink, DecimalPipe],
  templateUrl: './lista-solicitudes.html',
  styleUrl: './lista-solicitudes.scss'
})
export class ListaSolicitudesComponent implements OnInit {

  private readonly solicitudService = inject(SolicitudService);

  readonly solicitudes = signal<Solicitud[]>([]);
  readonly cargando = signal(true);
  readonly error = signal<string | null>(null);
  readonly filtroEstado = signal<EstadoSolicitud | 'TODAS'>('TODAS');

  readonly estados: (EstadoSolicitud | 'TODAS')[] = [
    'TODAS', 'PENDIENTE', 'APROBADO', 'RECHAZADO', 'DESEMBOLSADO'
  ];

  ngOnInit(): void {
    this.cargarSolicitudes();
  }

  cargarSolicitudes(): void {
    this.cargando.set(true);
    this.error.set(null);

    this.solicitudService.listarTodas().subscribe({
      next: (data) => {
        this.solicitudes.set(data);
        this.cargando.set(false);
      },
      error: (err) => {
        this.error.set('No se pudo cargar la lista de solicitudes.');
        this.cargando.set(false);
        console.error(err);
      }
    });
  }

  solicitudesFiltradas(): Solicitud[] {
    const filtro = this.filtroEstado();
    if (filtro === 'TODAS') return this.solicitudes();
    return this.solicitudes().filter(s => s.estado === filtro);
  }

  cambiarFiltro(estado: EstadoSolicitud | 'TODAS'): void {
    this.filtroEstado.set(estado);
  }

  /**
   * Cambio de estado con SweetAlert2 (confirmación bonita)
   */
  async cambiarEstado(solicitud: Solicitud, nuevoEstado: EstadoSolicitud): Promise<void> {

    const { icono, titulo, color, textoBoton } = this.configMensaje(nuevoEstado);

    const resultado = await Swal.fire({
      title: titulo,
      html: `
        <p style="color: #475569; margin: 0.5rem 0;">
          ¿Confirma cambiar el estado de la solicitud <strong>#${solicitud.id}</strong>?
        </p>
        <p style="color: #64748b; font-size: 0.875rem; margin-top: 0.75rem;">
          <strong>Cliente:</strong> ${solicitud.clienteNombres}<br>
          <strong>Monto:</strong> S/ ${solicitud.montoSolicitado.toFixed(2)}<br>
          <strong>Estado actual:</strong> ${solicitud.estado}
        </p>
      `,
      icon: icono,
      showCancelButton: true,
      confirmButtonText: textoBoton,
      cancelButtonText: 'Cancelar',
      confirmButtonColor: color,
      cancelButtonColor: '#64748b',
      reverseButtons: true,
      focusCancel: true
    });

    if (!resultado.isConfirmed) return;

    // Mostrar loading mientras se procesa
    Swal.fire({
      title: 'Procesando...',
      html: 'Actualizando estado de la solicitud',
      allowOutsideClick: false,
      didOpen: () => {
        Swal.showLoading();
      }
    });

    this.solicitudService.cambiarEstado(solicitud.id, { nuevoEstado }).subscribe({
      next: () => {
        Swal.fire({
          title: '¡Éxito!',
          text: `La solicitud #${solicitud.id} ahora está en estado ${nuevoEstado}.`,
          icon: 'success',
          timer: 2000,
          showConfirmButton: false
        });
        this.cargarSolicitudes();
      },
      error: (err) => {
        Swal.fire({
          title: 'Error',
          html: err?.error?.error || 'No se pudo cambiar el estado de la solicitud.',
          icon: 'error',
          confirmButtonColor: '#dc2626'
        });
        console.error(err);
      }
    });
  }

  /**
   * Configuración de icono, título y color según el nuevo estado
   */
  private configMensaje(nuevoEstado: EstadoSolicitud): {
    icono: 'question' | 'warning' | 'success' | 'info';
    titulo: string;
    color: string;
    textoBoton: string;
  } {
    switch (nuevoEstado) {
      case 'APROBADO':
        return {
          icono: 'question',
          titulo: '¿Aprobar esta solicitud?',
          color: '#3b82f6',
          textoBoton: 'Sí, aprobar'
        };
      case 'RECHAZADO':
        return {
          icono: 'warning',
          titulo: '¿Rechazar esta solicitud?',
          color: '#dc2626',
          textoBoton: 'Sí, rechazar'
        };
      case 'DESEMBOLSADO':
        return {
          icono: 'success',
          titulo: '¿Desembolsar esta solicitud?',
          color: '#10b981',
          textoBoton: 'Sí, desembolsar'
        };
      default:
        return {
          icono: 'question',
          titulo: '¿Cambiar estado?',
          color: '#64748b',
          textoBoton: 'Confirmar'
        };
    }
  }

  transicionesValidas(estado: EstadoSolicitud): EstadoSolicitud[] {
    switch (estado) {
      case 'PENDIENTE':
        return ['APROBADO', 'RECHAZADO'];
      case 'APROBADO':
        return ['DESEMBOLSADO'];
      case 'RECHAZADO':
      case 'DESEMBOLSADO':
        return [];
    }
  }

  claseEstado(estado: EstadoSolicitud): string {
    return 'estado-' + estado.toLowerCase();
  }
}
