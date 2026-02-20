import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { JobsList } from './components/jobs-list/jobs-list';
import { JobDetail } from './components/job-detail/job-detail';
import { ServerSettings } from './components/server-settings/server-settings';
import { UserManagement } from './components/user-management/user-management';

const routes: Routes = [
    { path: 'users', component: UserManagement },
    { path: 'jobs', component: JobsList },
    { path: 'jobs/:id', component: JobDetail },
    { path: 'settings', component: ServerSettings },
    { path: '', redirectTo: 'users', pathMatch: 'full' }
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class AdminRoutingModule { }
