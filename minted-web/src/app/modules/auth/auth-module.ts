import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { AuthRoutingModule } from './auth-routing-module';
import { Login } from './components/login/login';
import { SharedModule } from '../../shared/shared.module';
import { ChangePassword } from './components/change-password/change-password';

@NgModule({
  declarations: [
    Login,
    ChangePassword
  ],
  imports: [
    CommonModule,
    SharedModule,
    AuthRoutingModule
  ]
})
export class AuthModule { }
