import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MessageService, ConfirmationService } from 'primeng/api';
import { SharedModule } from '../../shared/shared.module';
import { TransactionsRoutingModule } from './transactions-routing-module';
import { TransactionsList } from './components/transactions-list/transactions-list';
import { AgGridModule } from 'ag-grid-angular';
import { CategoryCellRendererComponent } from './components/cell-renderers/category-cell-renderer.component';
import { ActionsCellRendererComponent } from './components/cell-renderers/actions-cell-renderer.component';


@NgModule({
  declarations: [
    TransactionsList,
    CategoryCellRendererComponent,
    ActionsCellRendererComponent
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
