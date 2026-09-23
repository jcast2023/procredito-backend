import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-confirmar-reset',
  imports: [FormsModule],
  templateUrl: './confirmar-reset.html',
  styleUrl: './confirmar-reset.scss'
})
export class ConfirmarResetComponent {

  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  token = signal('');
  nuevaPassword = signal('');
  confirmarPassword = signal('');
  mostrarPassword = signal(false);
  cargando = signal(false);
  error = signal<string | null>(null);
  exito = signal(false);

  toggleMostrarPassword(): void {
    this.mostrarPassword.update(v => !v);
  }

  onSubmit(): void {
    if (!this.token() || !this.nuevaPassword()) {
      this.error.set('Complete todos los campos');
      return;
    }

    if (this.nuevaPassword().length < 6) {
      this.error.set('La contraseña debe tener al menos 6 caracteres');
      return;
    }

    if (this.nuevaPassword() !== this.confirmarPassword()) {
      this.error.set('Las contraseñas no coinciden');
      return;
    }

    this.cargando.set(true);
    this.error.set(null);

    this.authService.confirmarReset(this.token().trim(), this.nuevaPassword()).subscribe({
      next: () => {
        this.cargando.set(false);
        this.exito.set(true);
      },
      error: (err) => {
        this.cargando.set(false);
        this.error.set(
          err?.error?.error ||
          err?.error?.message ||
          'No se pudo restablecer la contraseña.'
        );
      }
    });
  }

  irAlLogin(): void {
    this.router.navigate(['/login']);
  }
}
