import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { RecurringList } from './components/recurring-list/recurring-list';

const routes: Routes = [
    { path: '', component: RecurringList }
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class RecurringRoutingModule { }
