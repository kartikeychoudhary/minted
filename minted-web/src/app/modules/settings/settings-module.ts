import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MessageService, ConfirmationService } from 'primeng/api';

import { SettingsRoutingModule } from './settings-routing-module';
import { Settings } from './settings/settings';
import { AccountTypes } from './components/account-types/account-types';
import { Accounts } from './components/accounts/accounts';
import { Categories } from './components/categories/categories';
import { Budgets } from './components/budgets/budgets';
import { Profile } from './components/profile/profile';
import { SharedModule } from '../../shared/shared.module';


@NgModule({
  declarations: [
    Settings,
    AccountTypes,
    Accounts,
    Categories,
    Budgets,
    Profile
  ],
  imports: [
    CommonModule,
    SharedModule,
    SettingsRoutingModule
  ],
  providers: [
    MessageService,
    ConfirmationService
  ]
})
export class SettingsModule { }
