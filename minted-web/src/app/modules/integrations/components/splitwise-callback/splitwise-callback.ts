import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-splitwise-callback',
  standalone: false,
  template: `
    <div class="h-screen w-screen flex flex-col items-center justify-center bg-minted-bg-app text-minted-text-primary p-6 text-center">
      <div *ngIf="processing" class="flex flex-col items-center animate-fade-in">
        <i class="pi pi-spin pi-spinner text-4xl text-[#18b193] mb-4"></i>
        <h2 class="text-xl font-bold mb-2">Connecting to Splitwise...</h2>
        <p class="text-minted-text-secondary">Please wait while we finalize the connection. This window will close automatically.</p>
      </div>

      <div *ngIf="error" class="flex flex-col items-center animate-fade-in max-w-md">
        <div class="w-16 h-16 rounded-full bg-red-100 flex items-center justify-center mb-4 text-red-500">
          <i class="pi pi-times text-2xl"></i>
        </div>
        <h2 class="text-xl font-bold mb-2">Authorization Failed</h2>
        <p class="text-minted-text-secondary mb-6">{{ error }}</p>
        <button class="px-6 py-2 bg-[#18b193] text-white rounded-lg font-bold hover:bg-[#159a80] transition-colors" (click)="closeWindow()">
          Close Window
        </button>
      </div>
    </div>
  `
})
export class SplitwiseCallback implements OnInit {
  processing = true;
  error: string | null = null;

  constructor(private route: ActivatedRoute) {}

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      const code = params['code'];
      const error = params['error'];

      if (error) {
        this.processing = false;
        this.error = 'Splitwise returned an error: ' + error;
        return;
      }

      if (code) {
        // Send the code back to the parent window
        if (window.opener) {
          window.opener.postMessage({ type: 'SPLITWISE_AUTH_CODE', code }, '*');
        } else {
          this.processing = false;
          this.error = 'This page should be opened as a popup. Could not communicate with the parent Minted tab.';
        }
      } else {
        this.processing = false;
        this.error = 'Invalid response from Splitwise. Missing authorization code.';
      }
    });
  }

  closeWindow() {
    window.close();
  }
}
