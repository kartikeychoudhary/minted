import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from './core/guards/auth.guard';
import { adminGuard } from './core/guards/admin.guard';
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
      },
      {
        path: 'recurring',
        loadChildren: () => import('./modules/recurring/recurring-module').then(m => m.RecurringModule)
      },
      {
        path: 'analytics',
        loadChildren: () => import('./modules/analytics/analytics-module').then(m => m.AnalyticsModule)
      },
      {
        path: 'admin',
        loadChildren: () => import('./modules/admin/admin-module').then(m => m.AdminModule),
        canActivate: [adminGuard]
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
