import { platformBrowser } from '@angular/platform-browser';
import { AppModule } from './app/app-module';
import { ModuleRegistry, AllCommunityModule } from 'ag-grid-community';

// Register AG Grid modules globally
ModuleRegistry.registerModules([AllCommunityModule]);

platformBrowser().bootstrapModule(AppModule, {

})
  .catch(err => console.error(err));
