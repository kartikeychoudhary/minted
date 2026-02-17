import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { Login } from './components/login/login';
import { ChangePassword } from './components/change-password/change-password';

const routes: Routes = [
  {
    path: 'login',
    component: Login
  },
  {
    path: 'change-password',
    component: ChangePassword
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AuthRoutingModule { }
