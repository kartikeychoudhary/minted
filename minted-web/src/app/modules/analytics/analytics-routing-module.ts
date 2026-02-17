import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AnalyticsOverview } from './components/analytics-overview/analytics-overview';

const routes: Routes = [
    { path: '', component: AnalyticsOverview }
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class AnalyticsRoutingModule { }
