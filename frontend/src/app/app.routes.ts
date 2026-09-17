import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { LayoutComponent } from './shared/layout/layout';

export const routes: Routes = [
  // Ruta raíz → redirige a dashboard (si no hay sesión, el guard manda al login)
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
      // Aquí agregaremos clientes y solicitudes después:
      // { path: 'clientes', ... },
      // { path: 'solicitudes', ... }
    ]
  },

  // Ruta comodín → cualquier ruta desconocida va al dashboard
  {
    path: '**',
    redirectTo: 'dashboard'
  }
];
