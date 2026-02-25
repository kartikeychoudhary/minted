import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { SplitsPage } from './components/splits-page/splits-page';

const routes: Routes = [
  { path: '', component: SplitsPage }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class SplitsRoutingModule { }
