import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MessageService, ConfirmationService } from 'primeng/api';
import { SharedModule } from '../../shared/shared.module';
import { TransactionsRoutingModule } from './transactions-routing-module';
import { TransactionsList } from './components/transactions-list/transactions-list';


@NgModule({
  declarations: [
    TransactionsList
  ],
  imports: [
    CommonModule,
    SharedModule,
    TransactionsRoutingModule
  ],
  providers: [
    MessageService,
    ConfirmationService
  ]
})
export class TransactionsModule { }
