import { Component, OnInit, OnDestroy, HostListener, ChangeDetectorRef } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { MessageService, ConfirmationService } from 'primeng/api';
import { SplitwiseService } from '../../../../core/services/splitwise.service';
import { FriendService } from '../../../../core/services/friend.service';
import { 
  IntegrationStatusResponse, 
  SplitwiseFriend, 
  FriendLinkResponse 
} from '../../../../core/models/splitwise.model';
import { FriendResponse } from '../../../../core/models/friend.model';

@Component({
  selector: 'app-integrations-page',
  standalone: false,
  templateUrl: './integrations-page.html',
  providers: [MessageService, ConfirmationService]
})
export class IntegrationsPage implements OnInit, OnDestroy {
  splitwiseStatus: IntegrationStatusResponse | null = null;
  loadingStatus = true;

  // Splitwise Settings Dialog
  showSplitwiseSettings = false;
  splitwiseFriends: SplitwiseFriend[] = [];
  mintedFriends: FriendResponse[] = [];
  linkedFriends: FriendLinkResponse[] = [];
  loadingFriends = false;

  // For the dropdowns
  availableSplitwiseFriends: any[] = [];
  
  // Message listener
  private popupListener: any;
  private authWindow: Window | null = null;

  constructor(
    private titleService: Title,
    private messageService: MessageService,
    private confirmationService: ConfirmationService,
    private splitwiseService: SplitwiseService,
    private friendService: FriendService,
    private cdr: ChangeDetectorRef
  ) {
    this.titleService.setTitle('Minted - Integrations');
  }

  ngOnInit() {
    this.loadStatus();
    
    // Listen for messages from the OAuth popup
    this.popupListener = this.handleMessage.bind(this);
    window.addEventListener('message', this.popupListener, false);
  }

  ngOnDestroy() {
    if (this.popupListener) {
      window.removeEventListener('message', this.popupListener);
    }
  }

  loadStatus() {
    this.loadingStatus = true;
    this.splitwiseService.getStatus().subscribe({
      next: (res) => {
        this.splitwiseStatus = res as IntegrationStatusResponse;
        this.loadingStatus = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.loadingStatus = false;
        this.cdr.detectChanges();
      }
    });
  }

  connectSplitwise() {
    this.splitwiseService.getAuthUrl().subscribe({
      next: (res) => {
        const url = (res as any).authorizationUrl;
        // Open popup
        const width = 500;
        const height = 600;
        const left = (window.innerWidth - width) / 2;
        const top = (window.innerHeight - height) / 2;
        
        this.authWindow = window.open(
          url,
          'Splitwise Auth',
          `width=${width},height=${height},top=${top},left=${left}`
        );
      },
      error: (err) => {
        this.messageService.add({ severity: 'error', summary: 'Error', detail: err?.error?.message || 'Failed to start authorization flow.' });
      }
    });
  }

  disconnectSplitwise(event: Event) {
    event.stopPropagation();
    this.confirmationService.confirm({
      message: 'Are you sure you want to disconnect your Splitwise account? You will not be able to push any splits until you reconnect.',
      header: 'Disconnect Splitwise',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.splitwiseService.disconnect().subscribe({
          next: () => {
            this.messageService.add({ severity: 'success', summary: 'Disconnected', detail: 'Splitwise account unlinked successfully.' });
            this.loadStatus();
          },
          error: (err) => {
            this.messageService.add({ severity: 'error', summary: 'Error', detail: err?.error?.message || 'Failed to disconnect.' });
          }
        });
      }
    });
  }

  openSettings() {
    if (!this.splitwiseStatus?.connected) return;
    this.showSplitwiseSettings = true;
    this.loadFriendsData();
  }

  loadFriendsData() {
    this.loadingFriends = true;
    
    // 1. Get Minted friends
    this.friendService.getAll().subscribe(friends => {
      this.mintedFriends = friends;
      
      // 2. Get Linked friends
      this.splitwiseService.getLinkedFriends().subscribe(links => {
        this.linkedFriends = links;
        
        // 3. Get Splitwise friends
        this.splitwiseService.getFriends().subscribe(swFriends => {
          this.splitwiseFriends = swFriends;
          this.buildDropdownOptions();
          this.loadingFriends = false;
        });
      });
    });
  }

  buildDropdownOptions() {
    this.availableSplitwiseFriends = this.splitwiseFriends.map(f => ({
      label: f.displayName,
      value: f.id,
      email: f.email
    }));
  }

  getLinkedSplitwiseFriendId(friendId: number): number | null {
    const link = this.linkedFriends.find(l => l.friendId === friendId);
    return link ? link.splitwiseFriendId : null;
  }

  getLinkedSplitwiseInfo(friendId: number): FriendLinkResponse | undefined {
      return this.linkedFriends.find(l => l.friendId === friendId);
  }

  onFriendLinkChange(friendId: number, event: any) {
    const swId = event.value;
    if (swId) {
      this.splitwiseService.linkFriend(friendId, swId).subscribe({
        next: () => {
          this.messageService.add({ severity: 'success', summary: 'Linked', detail: 'Friend linked successfully.' });
          this.loadFriendsData(); // Refresh to update the UI
        },
        error: (err) => {
          this.messageService.add({ severity: 'error', summary: 'Error', detail: err?.error?.message || 'Failed to link friend.' });
          this.loadFriendsData(); // Revert UI
        }
      });
    }
  }

  unlinkFriend(friendId: number) {
    this.splitwiseService.unlinkFriend(friendId).subscribe({
      next: () => {
        this.messageService.add({ severity: 'success', summary: 'Unlinked', detail: 'Friend unlinked successfully.' });
        this.loadFriendsData();
      },
      error: (err) => {
        this.messageService.add({ severity: 'error', summary: 'Error', detail: err?.error?.message || 'Failed to unlink friend.' });
      }
    });
  }

  @HostListener('window:message', ['$event'])
  handleMessage(event: MessageEvent) {
    // Basic security check (ideally match exact origin, but environment can vary)
    if (!event.data || event.data.type !== 'SPLITWISE_AUTH_CODE') return;

    const code = event.data.code;
    
    if (this.authWindow) {
      this.authWindow.close();
      this.authWindow = null;
    }

    if (code) {
      this.splitwiseService.handleCallback(code).subscribe({
        next: (res) => {
          this.messageService.add({ 
            severity: 'success', 
            summary: 'Connected', 
            detail: res.message || 'Successfully connected to Splitwise!' 
          });
          this.loadStatus();
          this.cdr.detectChanges();
        },
        error: (err) => {
          this.messageService.add({ 
            severity: 'error', 
            summary: 'Connection Failed', 
            detail: err?.error?.message || 'Failed to complete Splitwise authorization.' 
          });
          this.cdr.detectChanges();
        }
      });
    }
  }
}
