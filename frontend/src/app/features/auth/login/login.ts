import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { LoginRequest } from '../../../core/models';


@Component({
  selector: 'app-login',
  imports: [FormsModule, RouterModule],
  templateUrl: './login.html',
  styleUrl: './login.scss'
})
export class LoginComponent {

  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  // Estado del formulario
  username = signal('');
  password = signal('');
  cargando = signal(false);
  error = signal<string | null>(null);

  // NUEVO: mostrar/ocultar contraseña
  mostrarPassword = signal(false);

  toggleMostrarPassword(): void {
    this.mostrarPassword.update(v => !v);
  }

  onSubmit(): void {
    if (!this.username() || !this.password()) {
      this.error.set('Por favor complete todos los campos');
      return;
    }

    this.cargando.set(true);
    this.error.set(null);

    const request: LoginRequest = {
      username: this.username(),
      password: this.password()
    };

    this.authService.login(request).subscribe({
      next: () => {
        this.cargando.set(false);
        const returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/dashboard';
        this.router.navigateByUrl(returnUrl);
      },
      error: (err) => {
        this.cargando.set(false);
        this.error.set(
          err?.error?.error ||
          err?.error?.message ||
          'Credenciales inválidas. Verifique su usuario y contraseña.'
        );
      }
    });
  }
}
