import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SharedModule } from '../../shared/shared.module';
import { AgGridModule } from 'ag-grid-angular';
import { SplitsRoutingModule } from './splits-routing-module';
import { SplitsPage } from './components/splits-page/splits-page';
import { SplitFriendsCellRendererComponent } from './components/cell-renderers/split-friends-cell-renderer.component';
import { SplitActionsCellRendererComponent } from './components/cell-renderers/split-actions-cell-renderer.component';
import { MessageService, ConfirmationService } from 'primeng/api';

@NgModule({
  declarations: [
    SplitsPage,
    SplitFriendsCellRendererComponent,
    SplitActionsCellRendererComponent
  ],
  imports: [
    CommonModule,
    SharedModule,
    AgGridModule,
    SplitsRoutingModule
  ],
  providers: [
    MessageService,
    ConfirmationService
  ]
})
export class SplitsModule { }
