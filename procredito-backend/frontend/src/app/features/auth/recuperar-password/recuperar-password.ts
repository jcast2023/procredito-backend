import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-recuperar-password',
  imports: [FormsModule],
  templateUrl: './recuperar-password.html',
  styleUrl: './recuperar-password.scss'
})
export class RecuperarPasswordComponent {

  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  email = signal('');
  cargando = signal(false);
  error = signal<string | null>(null);
  enviado = signal(false);

  onSubmit(): void {
    if (!this.email()) {
      this.error.set('Ingrese su correo electrónico');
      return;
    }

    this.cargando.set(true);
    this.error.set(null);

    this.authService.solicitarReset(this.email()).subscribe({
      next: () => {
        this.cargando.set(false);
        this.enviado.set(true);
      },
      error: (err) => {
        this.cargando.set(false);
        this.error.set(
          err?.error?.error ||
          err?.error?.message ||
          'Ocurrió un error al enviar el código.'
        );
      }
    });
  }

  irAlLogin(): void {
    this.router.navigate(['/login']);
  }
  irAConfirmar(): void {
  this.router.navigate(['/recuperar/confirmar']);
}


}
