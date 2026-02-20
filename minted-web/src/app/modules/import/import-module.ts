import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { ImportRoutingModule } from './import-routing-module';
import { ImportWizard } from './components/import-wizard/import-wizard';
import { ImportJobs } from './components/import-jobs/import-jobs';
import { ImportJobDetail } from './components/import-job-detail/import-job-detail';
import { StatusCellRendererComponent } from './components/cell-renderers/status-cell-renderer';

import { SharedModule } from '../../shared/shared.module';
import { AgGridModule } from 'ag-grid-angular';
import { MessageService, ConfirmationService } from 'primeng/api';

@NgModule({
  declarations: [
    ImportWizard,
    ImportJobs,
    ImportJobDetail,
    StatusCellRendererComponent
  ],
  imports: [
    CommonModule,
    SharedModule,
    AgGridModule,
    ImportRoutingModule
  ],
  providers: [
    MessageService,
    ConfirmationService
  ]
})
export class ImportModule { }
