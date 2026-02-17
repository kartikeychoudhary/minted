import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from './core/guards/auth.guard';
import { Layout } from './layout/layout';

const routes: Routes = [
  {
    path: '',
    component: Layout,
    canActivate: [AuthGuard],
    children: [
      {
        path: '',
        loadChildren: () => import('./modules/dashboard/dashboard-module').then(m => m.DashboardModule)
      },
      {
        path: 'settings',
        loadChildren: () => import('./modules/settings/settings-module').then(m => m.SettingsModule)
      },
      {
        path: 'transactions',
        loadChildren: () => import('./modules/transactions/transactions-module').then(m => m.TransactionsModule)
      }
    ]
  },
  {
    path: '',
    loadChildren: () => import('./modules/auth/auth-module').then(m => m.AuthModule)
  },
  {
    path: '**',
    redirectTo: ''
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
