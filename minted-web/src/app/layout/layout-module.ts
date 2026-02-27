import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { MessageService } from 'primeng/api';
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
  providers: [
    MessageService
  ],
  exports: [
    Layout,
    Sidebar
  ]
})
export class LayoutModule { }
