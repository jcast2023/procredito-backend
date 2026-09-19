import { Component, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet, Router } from '@angular/router';
import {
  LucideLayoutDashboard,
  LucideUsers,
  LucideFileText,
  LucideMenu,
  LucideLogOut,
  LucideBanknote
} from '@lucide/angular';
import Swal from 'sweetalert2';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-layout',
  imports: [
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    LucideLayoutDashboard,
    LucideUsers,
    LucideFileText,
    LucideMenu,
    LucideLogOut,
    LucideBanknote
  ],
  templateUrl: './layout.html',
  styleUrl: './layout.scss'
})
export class LayoutComponent {

  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly usuario = this.authService.usuario;
  readonly menuAbierto = signal(true);

  toggleMenu(): void {
    this.menuAbierto.update(v => !v);
  }

  async logout(): Promise<void> {
    const u = this.usuario();

    const resultado = await Swal.fire({
      title: '¿Cerrar sesión?',
      html: `
        <p style="color: #475569; margin: 0.5rem 0;">
          ¿Está seguro que desea cerrar sesión?
        </p>
        ${u ? `
          <div style="background: #f1f5f9; border-radius: 8px; padding: 0.75rem; margin-top: 1rem;">
            <p style="color: #1e293b; font-weight: 600; margin: 0; font-size: 0.9375rem;">
              ${u.nombreCompleto}
            </p>
            <p style="color: #64748b; margin: 0.25rem 0 0 0; font-size: 0.75rem; text-transform: uppercase; letter-spacing: 0.5px;">
              ${u.rol}
            </p>
          </div>
        ` : ''}
      `,
      icon: 'question',
      showCancelButton: true,
      confirmButtonText: 'Sí, cerrar sesión',
      cancelButtonText: 'Cancelar',
      confirmButtonColor: '#dc2626',
      cancelButtonColor: '#64748b',
      reverseButtons: true,
      focusCancel: true
    });

    if (!resultado.isConfirmed) return;

    this.authService.logout();

    const Toast = Swal.mixin({
      toast: true,
      position: 'top-end',
      showConfirmButton: false,
      timer: 2000,
      timerProgressBar: true,
      didOpen: (toast) => {
        toast.addEventListener('mouseenter', Swal.stopTimer);
        toast.addEventListener('mouseleave', Swal.resumeTimer);
      }
    });

    Toast.fire({
      icon: 'success',
      title: 'Sesión cerrada correctamente'
    });

    this.router.navigate(['/login']);
  }
}