import { Component, inject, signal, OnInit } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { Router, ActivatedRoute, RouterLink } from '@angular/router';
import Swal from 'sweetalert2';
import { UsuarioService } from '../../../core/services/usuario.service';
import { UsuarioRequest } from '../../../core/models';
import { Rol } from '../../../core/models/auth-response.model';

@Component({
  selector: 'app-form-usuario',
  imports: [FormsModule, RouterLink],
  templateUrl: './form-usuario.html',
  styleUrl: './form-usuario.scss'
})
export class FormUsuarioComponent implements OnInit {

  private readonly usuarioService = inject(UsuarioService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  // Estado del formulario
  readonly modoEdicion = signal(false);
  readonly usuarioId = signal<number | null>(null);
  readonly cargando = signal(false);
  readonly guardando = signal(false);

  // Campos del formulario
  username = signal('');
  password = signal('');
  nombreCompleto = signal('');
  email = signal('');
  rol = signal<Rol>('ANALISTA');

  readonly roles: Rol[] = ['ADMIN', 'ANALISTA'];

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');

    if (id) {
      this.modoEdicion.set(true);
      this.usuarioId.set(Number(id));
      this.cargarUsuario(Number(id));
    }
  }

  cargarUsuario(id: number): void {
    this.cargando.set(true);

    this.usuarioService.obtenerPorId(id).subscribe({
      next: (usuario) => {
        this.username.set(usuario.username);
        this.nombreCompleto.set(usuario.nombreCompleto);
        this.email.set(usuario.email || '');
        this.rol.set(usuario.rol);
        this.cargando.set(false);
      },
      error: (err) => {
        this.cargando.set(false);
        Swal.fire({
          title: 'Error',
          text: 'No se pudo cargar los datos del usuario.',
          icon: 'error',
          confirmButtonColor: '#dc2626'
        }).then(() => {
          this.router.navigate(['/usuarios']);
        });
        console.error(err);
      }
    });
  }

  guardar(formUsuario: NgForm): void {
    formUsuario.control?.markAllAsTouched();

    const user = this.username().trim();
    const pass = this.password().trim();
    const nom = this.nombreCompleto().trim();
    const mail = this.email().trim();

    // Validación de username
    if (!user || user.length < 4) {
      Swal.fire({
        title: 'Usuario inválido',
        text: 'El username debe tener al menos 4 caracteres.',
        icon: 'warning',
        confirmButtonColor: '#1e3a8a'
      });
      return;
    }

    // Validación de password (solo al crear)
    if (!this.modoEdicion() && (!pass || pass.length < 6)) {
      Swal.fire({
        title: 'Contraseña inválida',
        text: 'La contraseña es obligatoria y debe tener al menos 6 caracteres.',
        icon: 'warning',
        confirmButtonColor: '#1e3a8a'
      });
      return;
    }

    // Validación de nombre
    if (!nom) {
      Swal.fire({
        title: 'Campos incompletos',
        text: 'El nombre completo es obligatorio.',
        icon: 'warning',
        confirmButtonColor: '#1e3a8a'
      });
      return;
    }

    // Validación de email
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!mail || !emailRegex.test(mail)) {
      Swal.fire({
        title: 'Email inválido',
        text: 'Ingrese un correo electrónico válido.',
        icon: 'warning',
        confirmButtonColor: '#1e3a8a'
      });
      return;
    }

    this.enviar(user, pass, nom, mail);
  }

  private enviar(user: string, pass: string, nom: string, mail: string): void {
    this.guardando.set(true);

    const request: UsuarioRequest = {
      username: user,
      nombreCompleto: nom,
      email: mail,
      rol: this.rol()
    };

    // Solo enviar password si tiene valor
    if (pass) {
      request.password = pass;
    }

    const esEdicion = this.modoEdicion();

    const operacion = esEdicion
      ? this.usuarioService.actualizar(this.usuarioId()!, request)
      : this.usuarioService.crear(request);

    operacion.subscribe({
      next: () => {
        this.guardando.set(false);
        Swal.fire({
          title: esEdicion ? '¡Actualizado!' : '¡Creado!',
          text: `El usuario "${request.nombreCompleto}" fue ${esEdicion ? 'actualizado' : 'creado'} correctamente.`,
          icon: 'success',
          timer: 2000,
          showConfirmButton: false
        }).then(() => {
          this.router.navigate(['/usuarios']);
        });
      },
      error: (err) => {
        this.guardando.set(false);

        const errorTexto: string =
          err?.error?.error ||
          err?.error?.message ||
          'No se pudo guardar el usuario. Verifique los datos.';

        Swal.fire({
          title: 'Error al guardar',
          html: errorTexto,
          icon: 'error',
          confirmButtonColor: '#dc2626'
        });
        console.error(err);
      }
    });
  }

  cancelar(): void {
    this.router.navigate(['/usuarios']);
  }
}
