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
import { LlmConfigComponent } from './components/llm-config/llm-config';
import { MerchantMappingsComponent } from './components/merchant-mappings/merchant-mappings';
import { SharedModule } from '../../shared/shared.module';
import { AgGridModule } from 'ag-grid-angular';


@NgModule({
  declarations: [
    Settings,
    AccountTypes,
    Accounts,
    Categories,
    Budgets,
    Profile,
    LlmConfigComponent,
    MerchantMappingsComponent
  ],
  imports: [
    CommonModule,
    SharedModule,
    AgGridModule,
    SettingsRoutingModule
  ],
  providers: [
    MessageService,
    ConfirmationService
  ]
})
export class SettingsModule { }
