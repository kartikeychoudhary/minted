import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { RecurringRoutingModule } from './recurring-routing-module';
import { RecurringList } from './components/recurring-list/recurring-list';
import { SharedModule } from '../../shared/shared.module';

@NgModule({
    declarations: [
        RecurringList
    ],
    imports: [
        CommonModule,
        SharedModule,
        RecurringRoutingModule
    ]
})
export class RecurringModule { }
