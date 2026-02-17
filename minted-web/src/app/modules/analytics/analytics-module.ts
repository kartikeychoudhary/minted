import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SharedModule } from '../../shared/shared.module';
import { AnalyticsRoutingModule } from './analytics-routing-module';
import { AnalyticsOverview } from './components/analytics-overview/analytics-overview';
import { MessageService } from 'primeng/api';

@NgModule({
    declarations: [AnalyticsOverview],
    imports: [
        CommonModule,
        SharedModule,
        AnalyticsRoutingModule
    ],
    providers: [MessageService]
})
export class AnalyticsModule { }
