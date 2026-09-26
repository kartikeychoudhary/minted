import { NgModule, provideBrowserGlobalErrorListeners } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { providePrimeNG } from 'primeng/config';
import { MintedPreset } from './core/theme/minted-preset';

import { AppRoutingModule } from './app-routing-module';
import { App } from './app';
import { CoreModule } from './core';
import { LayoutModule } from './layout/layout-module';

@NgModule({
  declarations: [
    App
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    CoreModule.forRoot(),
    LayoutModule,
    AppRoutingModule
  ],
  providers: [
    provideBrowserGlobalErrorListeners(),
    providePrimeNG({
      theme: {
        preset: MintedPreset,
        options: {
          darkModeSelector: '.dark-mode'
        }
      }
    })
  ],
  bootstrap: [App]
})
export class AppModule { }
