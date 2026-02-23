import { NgModule } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { NotificationsRoutingModule } from './notifications-routing-module';
import { NotificationsList } from './components/notifications-list/notifications-list';
import { ConfirmationService, MessageService } from 'primeng/api';

@NgModule({
  declarations: [
    NotificationsList
  ],
  imports: [
    SharedModule,
    NotificationsRoutingModule
  ],
  providers: [
    MessageService,
    ConfirmationService
  ]
})
export class NotificationsModule { }
