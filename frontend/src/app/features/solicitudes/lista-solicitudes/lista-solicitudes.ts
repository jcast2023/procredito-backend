import { Component, inject, signal, OnInit, computed } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DecimalPipe } from '@angular/common';
import {
  LucideInfo,
  LucideCheck,
  LucideX,
  LucideBanknote,
  LucideShieldAlert,
  LucideArrowUp,
  LucideArrowDown,
  LucideDownload
} from '@lucide/angular';
import Swal from 'sweetalert2';
import { SolicitudService } from '../../../core/services/solicitud.service';
import { AuthService } from '../../../core/services/auth.service';
import { Solicitud, EstadoSolicitud } from '../../../core/models';

type ColumnaOrden = 'id' | 'clienteNombres' | 'montoSolicitado' | 'tasaInteres' | 'plazoMeses' | 'cuotaMensual' | 'estado';

@Component({
  selector: 'app-lista-solicitudes',
  imports: [
    RouterLink, DecimalPipe,
    LucideInfo, LucideCheck, LucideX, LucideBanknote,
    LucideShieldAlert, LucideArrowUp, LucideArrowDown, LucideDownload
  ],
  templateUrl: './lista-solicitudes.html',
  styleUrl: './lista-solicitudes.scss'
})
export class ListaSolicitudesComponent implements OnInit {

  private readonly solicitudService = inject(SolicitudService);
  private readonly authService = inject(AuthService);

  private readonly UMBRAL_ADMIN = 15000;
  private readonly POR_PAGINA = 10;

  readonly solicitudes = signal<Solicitud[]>([]);
  readonly cargando = signal(true);
  readonly error = signal<string | null>(null);
  readonly filtroEstado = signal<EstadoSolicitud | 'TODAS'>('TODAS');
  readonly busqueda = signal('');
  readonly columnaOrden = signal<ColumnaOrden>('id');
  readonly ordenAsc = signal(true);
  readonly paginaActual = signal(1);

  readonly esAdmin = computed(() => this.authService.esAdmin());

  readonly estados: (EstadoSolicitud | 'TODAS')[] = [
    'TODAS', 'PENDIENTE', 'APROBADO', 'RECHAZADO', 'DESEMBOLSADO'
  ];

  // ============ FILTRADO + BÚSQUEDA + ORDEN (computed) ============
  readonly solicitudesFiltradas = computed(() => {
    const filtro = this.filtroEstado();
    const texto = this.busqueda().trim().toLowerCase();

    let lista = this.solicitudes();
    if (filtro !== 'TODAS') {
      lista = lista.filter(s => s.estado === filtro);
    }
    if (texto.length > 0) {
      lista = lista.filter(s => this.coincideConBusqueda(s, texto));
    }

    // Orden por columna
    const col = this.columnaOrden();
    const asc = this.ordenAsc();
    const dir = asc ? 1 : -1;

    lista = [...lista].sort((a, b) => {
      let va: string | number;
      let vb: string | number;
      switch (col) {
        case 'id': va = a.id; vb = b.id; break;
        case 'clienteNombres': va = a.clienteNombres.toLowerCase(); vb = b.clienteNombres.toLowerCase(); break;
        case 'montoSolicitado': va = a.montoSolicitado; vb = b.montoSolicitado; break;
        case 'tasaInteres': va = a.tasaInteres; vb = b.tasaInteres; break;
        case 'plazoMeses': va = a.plazoMeses; vb = b.plazoMeses; break;
        case 'cuotaMensual': va = a.cuotaMensual; vb = b.cuotaMensual; break;
        case 'estado': va = a.estado; vb = b.estado; break;
        default: va = a.id; vb = b.id;
      }
      if (va < vb) return -1 * dir;
      if (va > vb) return 1 * dir;
      return 0;
    });

    return lista;
  });

  // ============ PAGINACIÓN ============
  readonly totalPaginas = computed(() =>
    Math.max(1, Math.ceil(this.solicitudesFiltradas().length / this.POR_PAGINA))
  );

  readonly solicitudesPagina = computed(() => {
    const inicio = (this.paginaActual() - 1) * this.POR_PAGINA;
    return this.solicitudesFiltradas().slice(inicio, inicio + this.POR_PAGINA);
  });

  readonly hayResultados = computed(() => this.solicitudesFiltradas().length > 0);

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
        this.paginaActual.set(1);
      },
      error: (err) => {
        this.error.set('No se pudo cargar la lista de solicitudes.');
        this.cargando.set(false);
        console.error(err);
      }
    });
  }

  // Búsqueda normalizada: "17,000" o "17.000" también encuentra "17000.00"
  private coincideConBusqueda(s: Solicitud, texto: string): boolean {
    const textoNormalizado = this.normalizarNumero(texto);
    return (
      s.clienteNombres.toLowerCase().includes(texto) ||
      s.clienteDocumento.toLowerCase().includes(texto) ||
      String(s.id).includes(texto) ||
      s.montoSolicitado.toFixed(2).includes(texto) ||
      (textoNormalizado.length > 0 && s.montoSolicitado.toFixed(2).replace('.', '').includes(textoNormalizado))
    );
  }

  // Quita comas y puntos para comparar montos sin formato
  private normalizarNumero(texto: string): string {
    return texto.replace(/[.,]/g, '');
  }

  limpiarBusqueda(): void {
    this.busqueda.set('');
  }

  cambiarFiltro(estado: EstadoSolicitud | 'TODAS'): void {
    this.filtroEstado.set(estado);
    this.paginaActual.set(1);
  }

  // ============ ORDEN POR COLUMNA ============
  ordenarPor(columna: ColumnaOrden): void {
    if (this.columnaOrden() === columna) {
      this.ordenAsc.set(!this.ordenAsc());
    } else {
      this.columnaOrden.set(columna);
      this.ordenAsc.set(true);
    }
    this.paginaActual.set(1);
  }

  esColumnaActiva(columna: ColumnaOrden): boolean {
    return this.columnaOrden() === columna;
  }

  // ============ PAGINACIÓN ============
  cambiarPagina(pagina: number): void {
    if (pagina >= 1 && pagina <= this.totalPaginas()) {
      this.paginaActual.set(pagina);
    }
  }

  // ============ EXPORTAR CSV ============
  exportarCsv(): void {
    const filas = this.solicitudesFiltradas().map(s => [
      s.id,
      s.clienteNombres,
      s.clienteDocumento,
      s.montoSolicitado.toFixed(2),
      s.tasaInteres.toFixed(2),
      s.plazoMeses,
      s.cuotaMensual.toFixed(2),
      s.estado
    ]);

    const encabezados = ['ID', 'Cliente', 'Documento', 'Monto', 'Tasa %', 'Plazo (meses)', 'Cuota', 'Estado'];
    const csv = [encabezados, ...filas]
      .map(f => f.map(c => `"${String(c).replace(/"/g, '""')}"`).join(','))
      .join('\r\n');

    // BOM para que Excel abra bien los acentos
    const blob = new Blob(['\uFEFF' + csv], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `solicitudes_${new Date().toISOString().slice(0, 10)}.csv`;
    a.click();
    URL.revokeObjectURL(url);
  }

  // ============ CAMBIO DE ESTADO (sin cambios) ============
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
        const mensaje = err?.error?.mensaje
          || err?.error?.error
          || 'No se pudo cambiar el estado de la solicitud.';
        Swal.fire({
          title: 'Acción no permitida',
          html: mensaje,
          icon: 'error',
          confirmButtonColor: '#dc2626'
        });
        console.error(err);
        this.cargarSolicitudes();
      }
    });
  }

  private configMensaje(nuevoEstado: EstadoSolicitud): {
    icono: 'question' | 'warning' | 'success' | 'info';
    titulo: string;
    color: string;
    textoBoton: string;
  } {
    switch (nuevoEstado) {
      case 'APROBADO':
        return { icono: 'question', titulo: '¿Aprobar esta solicitud?', color: '#3b82f6', textoBoton: 'Sí, aprobar' };
      case 'RECHAZADO':
        return { icono: 'warning', titulo: '¿Rechazar esta solicitud?', color: '#dc2626', textoBoton: 'Sí, rechazar' };
      case 'DESEMBOLSADO':
        return { icono: 'success', titulo: '¿Desembolsar esta solicitud?', color: '#10b981', textoBoton: 'Sí, desembolsar' };
      default:
        return { icono: 'question', titulo: '¿Cambiar estado?', color: '#64748b', textoBoton: 'Confirmar' };
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

  // Transiciones visibles según rol + monto (regla del umbral S/ 15,000)
  transicionesVisibles(sol: Solicitud): EstadoSolicitud[] {
    const base = this.transicionesValidas(sol.estado);
    if (this.esAdmin()) return base;

    const montoAlto = sol.montoSolicitado >= this.UMBRAL_ADMIN || sol.requiereAdmin === true;
    if (montoAlto) return [];
    return base;
  }

  puedeActuar(sol: Solicitud, nuevoEstado: EstadoSolicitud): boolean {
    if (this.esAdmin()) return true;
    const montoAlto = sol.montoSolicitado >= this.UMBRAL_ADMIN || sol.requiereAdmin === true;
    return !montoAlto;
  }

  claseEstado(estado: EstadoSolicitud): string {
    return 'estado-' + estado.toLowerCase();
  }

  // Helper para el badge "Requiere Admin"
  requiereAdmin(sol: Solicitud): boolean {
    return sol.montoSolicitado >= this.UMBRAL_ADMIN || sol.requiereAdmin === true;
  }
}
