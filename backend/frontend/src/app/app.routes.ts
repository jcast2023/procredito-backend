import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { adminGuard } from './core/guards/admin.guard';
import { LayoutComponent } from './shared/layout/layout';

export const routes: Routes = [
  // Ruta raíz → redirige a dashboard
  {
    path: '',
    redirectTo: 'dashboard',
    pathMatch: 'full'
  },

  // LOGIN (público, sin layout)
  {
    path: 'login',
    loadComponent: () =>
      import('./features/auth/login/login').then(m => m.LoginComponent)
  },

  // Rutas protegidas (con layout + authGuard)
  {
    path: '',
    component: LayoutComponent,
    canActivate: [authGuard],
    children: [
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./features/dashboard/dashboard').then(m => m.DashboardComponent)
      },

      // ===== CLIENTES =====
      {
        path: 'clientes',
        loadComponent: () =>
          import('./features/clientes/lista-clientes/lista-clientes')
            .then(m => m.ListaClientesComponent)
      },
      {
        path: 'clientes/nuevo',
        loadComponent: () =>
          import('./features/clientes/form-cliente/form-cliente')
            .then(m => m.FormClienteComponent)
      },
      {
        path: 'clientes/:id/editar',
        loadComponent: () =>
          import('./features/clientes/form-cliente/form-cliente')
            .then(m => m.FormClienteComponent)
      },

      // ===== SOLICITUDES =====
      {
        path: 'solicitudes',
        loadComponent: () =>
          import('./features/solicitudes/lista-solicitudes/lista-solicitudes')
            .then(m => m.ListaSolicitudesComponent)
      },
      {
        path: 'solicitudes/nueva',
        loadComponent: () =>
          import('./features/solicitudes/form-solicitud/form-solicitud')
            .then(m => m.FormSolicitudComponent)
      },

      // ===== USUARIOS (solo ADMIN) =====
      {
        path: 'usuarios',
        canActivate: [adminGuard],
        loadComponent: () =>
          import('./features/usuarios/lista-usuarios/lista-usuarios')
            .then(m => m.ListaUsuariosComponent)
      },
      {
        path: 'usuarios/nuevo',
        canActivate: [adminGuard],
        loadComponent: () =>
          import('./features/usuarios/form-usuario/form-usuario')
            .then(m => m.FormUsuarioComponent)
      },
      {
        path: 'usuarios/:id/editar',
        canActivate: [adminGuard],
        loadComponent: () =>
          import('./features/usuarios/form-usuario/form-usuario')
            .then(m => m.FormUsuarioComponent)
      }
    ]
  },

  // Rutas públicas de reset password
  {
    path: 'recuperar',
    loadComponent: () =>
      import('./features/auth/recuperar-password/recuperar-password')
        .then(m => m.RecuperarPasswordComponent)
  },
  {
    path: 'recuperar/confirmar',
    loadComponent: () =>
      import('./features/auth/confirmar-reset/confirmar-reset')
        .then(m => m.ConfirmarResetComponent)
  },

  // Ruta comodín
  {
    path: '**',
    redirectTo: 'dashboard'
  }
];
