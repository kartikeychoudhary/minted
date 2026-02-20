import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { AdminRoutingModule } from './admin-routing.module';
import { JobsList } from './components/jobs-list/jobs-list';
import { JobDetail } from './components/job-detail/job-detail';
import { ServerSettings } from './components/server-settings/server-settings';

import { SharedModule } from '../../shared/shared.module';
import { AgGridModule } from 'ag-grid-angular';
import { MessageService, ConfirmationService } from 'primeng/api';
import { UserManagement } from './components/user-management/user-management';

@NgModule({
  declarations: [
    JobsList,
    JobDetail,
    ServerSettings,
    UserManagement
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
