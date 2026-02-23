import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { StatementRoutingModule } from './statement-routing-module';
import { StatementList } from './components/statement-list/statement-list';
import { UploadStep } from './components/upload-step/upload-step';
import { StatementDetail } from './components/statement-detail/statement-detail';
import { TextReviewStep } from './components/text-review-step/text-review-step';
import { ParsePreviewStep } from './components/parse-preview-step/parse-preview-step';
import { ConfirmStep } from './components/confirm-step/confirm-step';

import { SharedModule } from '../../shared/shared.module';
import { AgGridModule } from 'ag-grid-angular';
import { MessageService, ConfirmationService } from 'primeng/api';

@NgModule({
  declarations: [
    StatementList,
    UploadStep,
    StatementDetail,
    TextReviewStep,
    ParsePreviewStep,
    ConfirmStep
  ],
  imports: [
    CommonModule,
    SharedModule,
    AgGridModule,
    StatementRoutingModule
  ],
  providers: [
    MessageService,
    ConfirmationService
  ]
})
export class StatementModule { }
