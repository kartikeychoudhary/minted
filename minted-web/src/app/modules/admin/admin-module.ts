import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { AdminRoutingModule } from './admin-routing.module';
import { JobsList } from './components/jobs-list/jobs-list';
import { JobDetail } from './components/job-detail/job-detail';
import { ServerSettings } from './components/server-settings/server-settings';
import { UserManagement } from './components/user-management/user-management';
import { TagCellRendererComponent } from './components/cell-renderers/tag-cell-renderer.component';
import { UserActionsCellRendererComponent } from './components/cell-renderers/user-actions-cell-renderer.component';

import { SharedModule } from '../../shared/shared.module';
import { AgGridModule } from 'ag-grid-angular';
import { MessageService, ConfirmationService } from 'primeng/api';

@NgModule({
  declarations: [
    JobsList,
    JobDetail,
    ServerSettings,
    UserManagement,
    TagCellRendererComponent,
    UserActionsCellRendererComponent
  ],
  imports: [
    CommonModule,
    SharedModule,
    AgGridModule,
    AdminRoutingModule
  ],
  providers: [
    MessageService,
    ConfirmationService
  ]
})
export class AdminModule { }
