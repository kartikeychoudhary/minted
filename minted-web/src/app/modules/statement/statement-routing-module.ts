import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { StatementList } from './components/statement-list/statement-list';
import { UploadStep } from './components/upload-step/upload-step';
import { StatementDetail } from './components/statement-detail/statement-detail';

const routes: Routes = [
  { path: '', component: StatementList },
  { path: 'new', component: UploadStep },
  { path: ':id', component: StatementDetail }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class StatementRoutingModule { }
