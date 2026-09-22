import { Component, inject, signal, OnInit, computed } from '@angular/core';
import { RouterLink } from '@angular/router';
import {
  LucidePlus,
  LucidePencil,
  LucideUserX,
  LucideUserCheck,
  LucideShield,
  LucideUser
} from '@lucide/angular';
import Swal from 'sweetalert2';
import { UsuarioService } from '../../../core/services/usuario.service';
import { Usuario } from '../../../core/models';

@Component({
  selector: 'app-lista-usuarios',
  imports: [
    RouterLink,
    LucidePlus,
    LucidePencil,
    LucideUserX,
    LucideUserCheck,
    LucideShield,
    LucideUser
  ],
  templateUrl: './lista-usuarios.html',
  styleUrl: './lista-usuarios.scss'
})
export class ListaUsuariosComponent implements OnInit {

  private readonly usuarioService = inject(UsuarioService);

  readonly usuarios = signal<Usuario[]>([]);
  readonly cargando = signal(true);
  readonly error = signal<string | null>(null);

  readonly totalActivos = computed(() =>
    this.usuarios().filter(u => u.activo).length
  );

  readonly totalInactivos = computed(() =>
    this.usuarios().filter(u => !u.activo).length
  );

  ngOnInit(): void {
    this.cargarUsuarios();
  }

  cargarUsuarios(): void {
    this.cargando.set(true);
    this.error.set(null);

    this.usuarioService.listarTodos().subscribe({
      next: (data) => {
        this.usuarios.set(data);
        this.cargando.set(false);
      },
      error: (err) => {
        this.error.set('No se pudo cargar la lista de usuarios.');
        this.cargando.set(false);
        console.error(err);
      }
    });
  }

  async cambiarActivo(usuario: Usuario): Promise<void> {
    const nuevoEstado = !usuario.activo;
    const accion = nuevoEstado ? 'activar' : 'desactivar';

    const resultado = await Swal.fire({
      title: `¿${accion.charAt(0).toUpperCase() + accion.slice(1)} usuario?`,
      html: `
        <p style="color: #475569; margin: 0.5rem 0;">
          Está a punto de <strong>${accion}</strong> al usuario:
        </p>
        <div style="background: #f1f5f9; border-radius: 8px; padding: 0.75rem; margin-top: 1rem;">
          <p style="color: #1e293b; font-weight: 600; margin: 0;">
            ${usuario.nombreCompleto}
          </p>
          <p style="color: #64748b; margin: 0.25rem 0 0 0; font-size: 0.8125rem;">
            @${usuario.username} · ${usuario.rol}
          </p>
        </div>
        ${!nuevoEstado ? `
          <p style="color: #dc2626; font-size: 0.8125rem; margin-top: 0.75rem;">
            ⚠️ El usuario no podrá iniciar sesión mientras esté inactivo.
          </p>
        ` : ''}
      `,
      icon: nuevoEstado ? 'question' : 'warning',
      showCancelButton: true,
      confirmButtonText: `Sí, ${accion}`,
      cancelButtonText: 'Cancelar',
      confirmButtonColor: nuevoEstado ? '#10b981' : '#dc2626',
      cancelButtonColor: '#64748b',
      reverseButtons: true,
      focusCancel: true
    });

    if (!resultado.isConfirmed) return;

    Swal.fire({
      title: 'Procesando...',
      allowOutsideClick: false,
      didOpen: () => Swal.showLoading()
    });

    this.usuarioService.cambiarActivo(usuario.id, nuevoEstado).subscribe({
      next: () => {
        Swal.fire({
          title: '¡Listo!',
          text: `El usuario "${usuario.nombreCompleto}" fue ${accion}do correctamente.`,
          icon: 'success',
          timer: 2000,
          showConfirmButton: false
        });
        this.cargarUsuarios();
      },
      error: (err) => {
        Swal.fire({
          title: 'Error',
          html: err?.error?.error || `No se pudo ${accion} el usuario.`,
          icon: 'error',
          confirmButtonColor: '#dc2626'
        });
        console.error(err);
      }
    });
  }
}
