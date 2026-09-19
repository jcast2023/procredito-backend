import { Component, inject, signal, OnInit } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { Router, ActivatedRoute, RouterLink } from '@angular/router';
import Swal from 'sweetalert2';
import { ClienteService } from '../../../core/services/cliente.service';
import { ClienteRequest } from '../../../core/models';

@Component({
  selector: 'app-form-cliente',
  imports: [FormsModule, RouterLink],
  templateUrl: './form-cliente.html',
  styleUrl: './form-cliente.scss'
})
export class FormClienteComponent implements OnInit {

  private readonly clienteService = inject(ClienteService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  // Estado del formulario
  readonly modoEdicion = signal(false);
  readonly clienteId = signal<number | null>(null);
  readonly cargando = signal(false);
  readonly guardando = signal(false);

  // Campos del formulario
  documento = signal('');
  nombres = signal('');
  telefono = signal('');
  direccion = signal('');
  tipoNegocio = signal('');

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');

    if (id) {
      // Modo edición
      this.modoEdicion.set(true);
      this.clienteId.set(Number(id));
      this.cargarCliente(Number(id));
    }
  }

  cargarCliente(id: number): void {
    this.cargando.set(true);

    this.clienteService.obtenerPorId(id).subscribe({
      next: (cliente) => {
        this.documento.set(cliente.documento);
        this.nombres.set(cliente.nombres);
        this.telefono.set(cliente.telefono || '');
        this.direccion.set(cliente.direccion || '');
        this.tipoNegocio.set(cliente.tipoNegocio || '');
        this.cargando.set(false);
      },
      error: (err) => {
        this.cargando.set(false);
        Swal.fire({
          title: 'Error',
          text: 'No se pudo cargar los datos del cliente.',
          icon: 'error',
          confirmButtonColor: '#dc2626'
        }).then(() => {
          this.router.navigate(['/clientes']);
        });
        console.error(err);
      }
    });
  }

  guardar(formCliente: NgForm): void {
    // Marcar todos los campos como tocados para mostrar los errores
    formCliente.control?.markAllAsTouched();

    const doc = this.documento().trim();
    const nom = this.nombres().trim();
    const tel = this.telefono().trim();
    const dir = this.direccion().trim();
    const tipo = this.tipoNegocio().trim();

    // Validación de documento: exactamente 8 dígitos
    if (!/^\d{8}$/.test(doc)) {
      Swal.fire({
        title: 'Documento inválido',
        text: 'El DNI debe tener exactamente 8 dígitos numéricos.',
        icon: 'warning',
        confirmButtonColor: '#1e3a8a'
      });
      return;
    }

    // Validación de nombres
    if (!nom) {
      Swal.fire({
        title: 'Campos incompletos',
        text: 'Los nombres son obligatorios.',
        icon: 'warning',
        confirmButtonColor: '#1e3a8a'
      });
      return;
    }

    // Validación de teléfono: exactamente 9 dígitos
    if (!/^\d{9}$/.test(tel)) {
      Swal.fire({
        title: 'Teléfono inválido',
        text: 'El teléfono debe tener exactamente 9 dígitos numéricos.',
        icon: 'warning',
        confirmButtonColor: '#1e3a8a'
      });
      return;
    }

    // Validación de tipo de negocio y dirección: obligatorios
    if (!tipo || !dir) {
      Swal.fire({
        title: 'Campos incompletos',
        text: 'El tipo de negocio y la dirección son obligatorios.',
        icon: 'warning',
        confirmButtonColor: '#1e3a8a'
      });
      return;
    }

    // Verificar DNI duplicado (solo en creación, o si cambió en edición)
    this.clienteService.existeDni(doc, this.modoEdicion() ? this.clienteId() : null).subscribe({
      next: (existe) => {
        if (existe) {
          Swal.fire({
            title: 'DNI ya registrado',
            text: `Ya existe un cliente con el DNI "${doc}".`,
            icon: 'warning',
            confirmButtonColor: '#1e3a8a'
          });
          return;
        }
        this.enviar(doc, nom, tel, dir, tipo);
      },
      error: () => {
        // Si falla la verificación, continuar (el backend validará)
        this.enviar(doc, nom, tel, dir, tipo);
      }
    });
  }

  private enviar(doc: string, nom: string, tel: string, dir: string, tipo: string): void {
    this.guardando.set(true);

    const request: ClienteRequest = {
      documento: doc,
      nombres: nom,
      telefono: tel,
      direccion: dir,
      tipoNegocio: tipo
    };

    const esEdicion = this.modoEdicion();

    const operacion = esEdicion
      ? this.clienteService.actualizar(this.clienteId()!, request)
      : this.clienteService.crear(request);

    operacion.subscribe({
      next: () => {
        this.guardando.set(false);
        Swal.fire({
          title: esEdicion ? '¡Actualizado!' : '¡Creado!',
          text: `El cliente "${request.nombres}" fue ${esEdicion ? 'actualizado' : 'creado'} correctamente.`,
          icon: 'success',
          timer: 2000,
          showConfirmButton: false
        }).then(() => {
          this.router.navigate(['/clientes']);
        });
      },
      error: (err) => {
        this.guardando.set(false);

        const errorTexto: string =
          err?.error?.error ||
          err?.error?.message ||
          'No se pudo guardar el cliente. Verifique los datos.';

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
    this.router.navigate(['/clientes']);
  }
}
