import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import Swal from 'sweetalert2';
import { ClienteService } from '../../../core/services/cliente.service';
import { AuthService } from '../../../core/services/auth.service';
import { Cliente } from '../../../core/models';

@Component({
  selector: 'app-lista-clientes',
  imports: [RouterLink],
  templateUrl: './lista-clientes.html',
  styleUrl: './lista-clientes.scss'
})
export class ListaClientesComponent implements OnInit {

  private readonly clienteService = inject(ClienteService);
  private readonly authService = inject(AuthService);

  readonly clientes = signal<Cliente[]>([]);
  readonly cargando = signal(true);
  readonly error = signal<string | null>(null);

  readonly esAdmin = computed(() => this.authService.esAdmin());

  ngOnInit(): void {
    this.cargarClientes();
  }

  cargarClientes(): void {
    this.cargando.set(true);
    this.error.set(null);

    this.clienteService.listarTodos().subscribe({
      next: (data) => {
        this.clientes.set(data);
        this.cargando.set(false);
      },
      error: (err) => {
        this.error.set('No se pudo cargar la lista de clientes.');
        this.cargando.set(false);
        console.error(err);
      }
    });
  }

  async eliminar(cliente: Cliente): Promise<void> {
    // ========== PASO 1: Confirmación inicial ==========
    const resultado = await Swal.fire({
      title: '¿Eliminar cliente?',
      html: `
        <p style="color: #475569; margin: 0.5rem 0;">
          Está a punto de eliminar al cliente:
        </p>
        <p style="color: #1e293b; font-weight: 600; margin: 0.75rem 0;">
          ${cliente.nombres}<br>
          <span style="font-weight: 400; color: #64748b; font-size: 0.875rem;">
            Documento: ${cliente.documento}
          </span>
        </p>
        <p style="color: #dc2626; font-size: 0.8125rem; margin-top: 0.75rem;">
          ⚠️ Esta acción no se puede deshacer.
        </p>
      `,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Sí, eliminar',
      cancelButtonText: 'Cancelar',
      confirmButtonColor: '#dc2626',
      cancelButtonColor: '#64748b',
      reverseButtons: true,
      focusCancel: true
    });

    if (!resultado.isConfirmed) return;

    // ========== PASO 2: Loading ==========
    Swal.fire({
      title: 'Eliminando...',
      allowOutsideClick: false,
      didOpen: () => Swal.showLoading()
    });

    this.clienteService.eliminar(cliente.id).subscribe({
      next: () => {
        Swal.fire({
          title: '¡Eliminado!',
          text: `El cliente "${cliente.nombres}" fue eliminado correctamente.`,
          icon: 'success',
          timer: 2000,
          showConfirmButton: false
        });
        this.cargarClientes();
      },
      error: (err) => {
        console.error('Error al eliminar cliente:', err);

        const errorTexto: string =
          err?.error?.error ||
          err?.error?.message ||
          err?.message ||
          '';

        // Detectar error de FK (cliente con solicitudes asociadas)
        const esErrorFK =
          errorTexto.includes('ORA-02292') ||
          errorTexto.includes('restricción de integridad') ||
          errorTexto.includes('integridad') ||
          errorTexto.includes('constraint') ||
          errorTexto.includes('foreign key') ||
          errorTexto.includes('registro secundario');

        if (esErrorFK) {
          Swal.fire({
            title: 'No se puede eliminar',
            html: `
              <div style="text-align: left;">
                <p style="color: #1e293b; font-size: 1rem; margin: 0 0 0.75rem 0;">
                  El cliente <strong>"${cliente.nombres}"</strong> no puede ser eliminado
                  porque tiene <strong>solicitudes de crédito asociadas</strong>.
                </p>

                <div style="background: #eff6ff; border-left: 4px solid #3b82f6; padding: 0.75rem 1rem; border-radius: 6px; margin: 1rem 0;">
                  <p style="color: #1e3a8a; font-size: 0.875rem; margin: 0; font-weight: 600;">
                    💡 ¿Por qué no se puede eliminar?
                  </p>
                  <p style="color: #475569; font-size: 0.8125rem; margin: 0.5rem 0 0 0;">
                    Por razones de <strong>auditoría y trazabilidad</strong>, un cliente con
                    historial crediticio no puede ser eliminado. La normativa financiera
                    exige conservar los registros.
                  </p>
                </div>

                <div style="background: #f0fdf4; border-left: 4px solid #10b981; padding: 0.75rem 1rem; border-radius: 6px;">
                  <p style="color: #065f46; font-size: 0.875rem; margin: 0; font-weight: 600;">
                    ✅ ¿Qué puedes hacer?
                  </p>
                  <ul style="color: #475569; font-size: 0.8125rem; margin: 0.5rem 0 0 1rem; padding: 0;">
                    <li>Ver el historial de solicitudes del cliente</li>
                    <li>Contactar al administrador del sistema</li>
                  </ul>
                </div>
              </div>
            `,
            icon: 'info',
            confirmButtonColor: '#1e3a8a',
            confirmButtonText: 'Entendido',
            width: 500
          });
          return;
        }

        Swal.fire({
          title: 'Error al eliminar',
          html: errorTexto || 'No se pudo eliminar el cliente. Intente nuevamente.',
          icon: 'error',
          confirmButtonColor: '#dc2626',
          confirmButtonText: 'Cerrar'
        });
      }
    });
  }
}
