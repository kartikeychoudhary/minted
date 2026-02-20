import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ImportWizard } from './components/import-wizard/import-wizard';
import { ImportJobs } from './components/import-jobs/import-jobs';
import { ImportJobDetail } from './components/import-job-detail/import-job-detail';

const routes: Routes = [
  { path: '', component: ImportWizard },
  { path: 'jobs', component: ImportJobs },
  { path: 'jobs/:id', component: ImportJobDetail }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ImportRoutingModule { }
