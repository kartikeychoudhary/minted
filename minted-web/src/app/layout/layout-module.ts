import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { SharedModule } from '../shared/shared.module';
import { Layout } from './layout';
import { Sidebar } from './components/sidebar/sidebar';

@NgModule({
  declarations: [
    Layout,
    Sidebar
  ],
  imports: [
    SharedModule,
    RouterModule
  ],
  exports: [
    Layout,
    Sidebar
  ]
})
export class LayoutModule { }
