import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { AuthRoutingModule } from './auth-routing-module';
import { Login } from './components/login/login';
import { SharedModule } from '../../shared/shared.module';
import { ChangePassword } from './components/change-password/change-password';
import { Signup } from './components/signup/signup';
import { MessageService } from 'primeng/api';

@NgModule({
  declarations: [
    Login,
    ChangePassword,
    Signup
  ],
  imports: [
    CommonModule,
    SharedModule,
    AuthRoutingModule
  ],
  providers: [
    MessageService
  ]
})
export class AuthModule { }
