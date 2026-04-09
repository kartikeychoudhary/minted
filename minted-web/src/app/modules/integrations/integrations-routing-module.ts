import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { IntegrationsPage } from './components/integrations-page/integrations-page';
import { SplitwiseCallback } from './components/splitwise-callback/splitwise-callback';
import { AuthGuard } from '../../core/guards/auth.guard';

const routes: Routes = [
  {
    path: '',
    component: IntegrationsPage,
    canActivate: [AuthGuard],
    data: { title: 'Integrations' }
  },
  {
    path: 'splitwise/callback',
    component: SplitwiseCallback,
    // Note: Can't easily use authGuard here as it might be an isolated popup window,
    // but the backend requires the JWT. We'll handle JWT from localstorage in the popup.
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class IntegrationsRoutingModule { }
