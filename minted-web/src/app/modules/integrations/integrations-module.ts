import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { IntegrationsRoutingModule } from './integrations-routing-module';

import { IntegrationsPage } from './components/integrations-page/integrations-page';
import { SplitwiseCallback } from './components/splitwise-callback/splitwise-callback';

// PrimeNG
import { ButtonModule } from 'primeng/button';
import { ToastModule } from 'primeng/toast';
import { DialogModule } from 'primeng/dialog';
import { SelectModule } from 'primeng/select';
import { TableModule } from 'primeng/table';
import { ConfirmDialogModule } from 'primeng/confirmdialog';

@NgModule({
  declarations: [
    IntegrationsPage,
    SplitwiseCallback
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    IntegrationsRoutingModule,
    ButtonModule,
    ToastModule,
    DialogModule,
    SelectModule,
    TableModule,
    ConfirmDialogModule
  ]
})
export class IntegrationsModule { }
