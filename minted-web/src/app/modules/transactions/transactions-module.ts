import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MessageService, ConfirmationService } from 'primeng/api';
import { SharedModule } from '../../shared/shared.module';
import { TransactionsRoutingModule } from './transactions-routing-module';
import { TransactionsList } from './components/transactions-list/transactions-list';
import { AgGridModule } from 'ag-grid-angular';


@NgModule({
  declarations: [
    TransactionsList
  ],
  imports: [
    CommonModule,
    SharedModule,
    AgGridModule,
    TransactionsRoutingModule
  ],
  providers: [
    MessageService,
    ConfirmationService
  ]
})
export class TransactionsModule { }
