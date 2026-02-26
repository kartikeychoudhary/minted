import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { MessageService, ConfirmationService } from 'primeng/api';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ColDef, GridApi, GridReadyEvent, GridOptions, themeQuartz } from 'ag-grid-community';
import { FriendService } from '../../../../core/services/friend.service';
import { SplitService } from '../../../../core/services/split.service';
import { CurrencyService } from '../../../../core/services/currency.service';
import { FriendRequest, FriendResponse } from '../../../../core/models/friend.model';
import {
  SplitTransactionRequest,
  SplitTransactionResponse,
  SplitBalanceSummaryResponse,
  FriendBalanceResponse,
  SplitShareRequest,
  SplitShareResponse,
  SplitType
} from '../../../../core/models/split.model';
import { SplitFriendsCellRendererComponent } from '../cell-renderers/split-friends-cell-renderer.component';
import { SplitActionsCellRendererComponent } from '../cell-renderers/split-actions-cell-renderer.component';

interface SplitFriendEntry {
  friendId: number | null;
  friendName: string;
  avatarColor: string;
  shareAmount: number;
  sharePercentage: number | null;
  isPayer: boolean;
}

@Component({
  selector: 'app-splits-page',
  standalone: false,
  templateUrl: './splits-page.html',
  styleUrl: './splits-page.scss',
})
export class SplitsPage implements OnInit {
  loading = false;
  friends: FriendResponse[] = [];
  splits: SplitTransactionResponse[] = [];
  balanceSummary: SplitBalanceSummaryResponse = { youAreOwed: 0, youOwe: 0 };
  friendBalances: FriendBalanceResponse[] = [];

  // Friend dialog
  showFriendDialog = false;
  isEditFriend = false;
  friendForm?: FormGroup;
  selectedFriend?: FriendResponse;

  // Split dialog
  showSplitDialog = false;
  isEditSplit = false;
  splitForm?: FormGroup;
  selectedSplit?: SplitTransactionResponse;
  splitFriendEntries: SplitFriendEntry[] = [];
  selectedSplitType: SplitType = 'EQUAL';
  availableFriendsForSplit: FriendResponse[] = [];

  // Settle dialog
  showSettleDialog = false;
  settleFriend?: FriendBalanceResponse;
  settleShares: SplitShareResponse[] = [];
  settleLoading = false;

  // Avatar color options
  avatarColors = ['#6366f1', '#ec4899', '#f59e0b', '#10b981', '#3b82f6', '#8b5cf6', '#ef4444', '#14b8a6'];

  // AG Grid
  mintedTheme = themeQuartz.withParams({
    backgroundColor: 'var(--minted-bg-card)',
    foregroundColor: 'var(--minted-text-primary)',
    borderColor: 'var(--minted-border)',
    browserColorScheme: 'inherit',
    headerBackgroundColor: 'var(--minted-bg-card)',
    headerFontSize: 12,
    headerFontWeight: 600,
    headerTextColor: 'var(--minted-text-muted)',
    oddRowBackgroundColor: 'var(--minted-bg-card)',
    rowHoverColor: 'var(--minted-bg-hover)',
    selectedRowBackgroundColor: 'var(--minted-accent-subtle)',
    accentColor: 'var(--minted-accent)',
    fontFamily: '"Inter", -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif',
    fontSize: 14,
    rowHeight: 60,
    headerHeight: 48,
    spacing: 6,
    wrapperBorderRadius: 8,
    cellHorizontalPadding: 16,
    headerColumnBorder: false,
    headerColumnResizeHandleColor: 'transparent',
    columnBorder: false,
    rowBorder: { color: 'var(--minted-border-light)', width: 1, style: 'solid' },
  });

  private gridApi!: GridApi;
  columnDefs: ColDef[] = [];
  defaultColDef: ColDef = { sortable: true, filter: false, resizable: true };
  gridOptions: GridOptions = {
    pagination: true,
    paginationPageSize: 10,
    paginationPageSizeSelector: [10, 25, 50],
    domLayout: 'normal',
    animateRows: true,
    overlayNoRowsTemplate: '<span class="ag-overlay-no-rows-center">No split transactions yet. Add a split to get started.</span>',
  };
  rowData: any[] = [];

  constructor(
    private friendService: FriendService,
    private splitService: SplitService,
    private messageService: MessageService,
    private confirmationService: ConfirmationService,
    private formBuilder: FormBuilder,
    private cdr: ChangeDetectorRef,
    private route: ActivatedRoute,
    public currencyService: CurrencyService
  ) {
    this.setupGridColumns();
  }

  ngOnInit(): void {
    this.initForms();
    this.loadData();

    // Check for query params (from transactions split button)
    this.route.queryParams.subscribe(params => {
      if (params['sourceTransactionId']) {
        // Wait for data to load before opening dialog
        setTimeout(() => {
          this.splitForm?.patchValue({
            description: params['description'] || '',
            categoryName: params['categoryName'] || '',
            totalAmount: params['totalAmount'] ? parseFloat(params['totalAmount']) : null,
            transactionDate: params['transactionDate'] ? new Date(params['transactionDate']) : new Date(),
            sourceTransactionId: params['sourceTransactionId'] ? parseInt(params['sourceTransactionId']) : null
          });
          this.openNewSplitDialog();
        }, 500);
      }
    });
  }

  setupGridColumns(): void {
    this.columnDefs = [
      {
        headerName: 'Date',
        field: 'transactionDate',
        width: 130,
        cellClass: 'cell-v-center',
        valueFormatter: (params) => {
          const date = new Date(params.value);
          return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
        }
      },
      {
        headerName: 'Description',
        field: 'description',
        flex: 1,
        minWidth: 200,
        cellClass: 'cell-v-center'
      },
      {
        headerName: 'Category',
        field: 'categoryName',
        width: 150,
        cellClass: 'cell-v-center',
        cellRenderer: (params: any) => {
          return `<span class="px-2.5 py-1 inline-flex text-xs leading-5 font-semibold rounded-full bg-blue-50 text-blue-700">${params.value}</span>`;
        }
      },
      {
        headerName: 'Split With',
        field: 'shares',
        width: 160,
        sortable: false,
        cellRenderer: SplitFriendsCellRendererComponent,
        cellClass: 'cell-v-center'
      },
      {
        headerName: 'Total',
        field: 'totalAmount',
        width: 130,
        cellClass: 'cell-v-center font-bold',
        valueFormatter: (params) => this.currencyService.format(params.value)
      },
      {
        headerName: 'Your Share',
        field: 'yourShare',
        width: 130,
        cellClass: 'cell-v-center font-bold',
        valueFormatter: (params) => this.currencyService.format(params.value)
      },
      {
        headerName: '',
        field: 'actions',
        width: 120,
        sortable: false,
        cellRenderer: SplitActionsCellRendererComponent,
        cellRendererParams: {
          callbacks: {
            onEdit: (data: any) => this.openEditSplitDialog(data),
            onDelete: (data: any) => this.deleteSplit(data)
          }
        },
        cellClass: 'ag-cell-actions'
      }
    ];
  }

  onGridReady(params: GridReadyEvent): void {
    this.gridApi = params.api;
  }

  initForms(): void {
    this.friendForm = this.formBuilder.group({
      name: ['', [Validators.required, Validators.maxLength(100)]],
      email: ['', Validators.maxLength(255)],
      phone: ['', Validators.maxLength(20)],
      avatarColor: ['#6366f1']
    });

    this.splitForm = this.formBuilder.group({
      sourceTransactionId: [null],
      description: ['', [Validators.required, Validators.maxLength(500)]],
      categoryName: ['', [Validators.required, Validators.maxLength(100)]],
      totalAmount: [null, [Validators.required, Validators.min(0.01)]],
      transactionDate: [new Date(), Validators.required]
    });
  }

  loadData(): void {
    this.loading = true;
    Promise.all([
      this.loadFriends(),
      this.loadSplits(),
      this.loadBalanceSummary(),
      this.loadFriendBalances()
    ]).finally(() => {
      this.loading = false;
      this.cdr.detectChanges();
    });
  }

  async loadFriends(): Promise<void> {
    this.friendService.getAll().subscribe({
      next: (data) => {
        this.friends = data;
        this.cdr.detectChanges();
      },
      error: () => {
        this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Failed to load friends' });
      }
    });
  }

  async loadSplits(): Promise<void> {
    this.splitService.getAll().subscribe({
      next: (data) => {
        this.splits = data;
        this.rowData = data;
        this.cdr.detectChanges();
      },
      error: () => {
        this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Failed to load splits' });
      }
    });
  }

  async loadBalanceSummary(): Promise<void> {
    this.splitService.getBalanceSummary().subscribe({
      next: (data) => {
        this.balanceSummary = data;
        this.cdr.detectChanges();
      },
      error: () => { }
    });
  }

  async loadFriendBalances(): Promise<void> {
    this.splitService.getFriendBalances().subscribe({
      next: (data) => {
        this.friendBalances = data;
        this.cdr.detectChanges();
      },
      error: () => { }
    });
  }

  // ── Friend Dialog ──

  openNewFriendDialog(): void {
    this.isEditFriend = false;
    this.friendForm?.reset({ avatarColor: '#6366f1' });
    this.showFriendDialog = true;
  }

  openEditFriendDialog(friend: FriendResponse): void {
    this.isEditFriend = true;
    this.selectedFriend = friend;
    this.friendForm?.patchValue({
      name: friend.name,
      email: friend.email,
      phone: friend.phone,
      avatarColor: friend.avatarColor
    });
    this.showFriendDialog = true;
  }

  saveFriend(): void {
    if (this.friendForm?.invalid) {
      Object.keys(this.friendForm.controls).forEach(key => {
        this.friendForm?.get(key)?.markAsTouched();
      });
      return;
    }

    const request: FriendRequest = this.friendForm!.value;

    if (this.isEditFriend && this.selectedFriend) {
      this.friendService.update(this.selectedFriend.id, request).subscribe({
        next: () => {
          this.messageService.add({ severity: 'success', summary: 'Success', detail: 'Friend updated' });
          this.showFriendDialog = false;
          this.loadFriends();
        },
        error: (err) => {
          this.messageService.add({ severity: 'error', summary: 'Error', detail: err?.error?.message || 'Failed to update friend' });
        }
      });
    } else {
      this.friendService.create(request).subscribe({
        next: () => {
          this.messageService.add({ severity: 'success', summary: 'Success', detail: 'Friend added' });
          this.showFriendDialog = false;
          this.loadFriends();
        },
        error: (err) => {
          this.messageService.add({ severity: 'error', summary: 'Error', detail: err?.error?.message || 'Failed to add friend' });
        }
      });
    }
  }

  deleteFriendConfirm(friend: FriendResponse): void {
    this.confirmationService.confirm({
      key: 'splits',
      message: `Remove ${friend.name} from your friends?`,
      header: 'Confirm Remove',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.friendService.delete(friend.id).subscribe({
          next: () => {
            this.messageService.add({ severity: 'success', summary: 'Success', detail: 'Friend removed' });
            this.loadFriends();
            this.loadFriendBalances();
          },
          error: () => {
            this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Failed to remove friend' });
          }
        });
      }
    });
  }

  // ── Split Dialog ──

  openNewSplitDialog(): void {
    this.isEditSplit = false;
    this.selectedSplitType = 'EQUAL';
    this.splitFriendEntries = [
      { friendId: null, friendName: 'Me', avatarColor: '#22c55e', shareAmount: 0, sharePercentage: null, isPayer: true }
    ];
    this.updateAvailableFriends();
    if (!this.splitForm?.get('description')?.value) {
      this.splitForm?.reset({ transactionDate: new Date() });
    }
    this.showSplitDialog = true;
  }

  openEditSplitDialog(split: SplitTransactionResponse): void {
    this.isEditSplit = true;
    this.selectedSplit = split;
    this.selectedSplitType = split.splitType as SplitType;

    this.splitForm?.patchValue({
      sourceTransactionId: split.sourceTransactionId,
      description: split.description,
      categoryName: split.categoryName,
      totalAmount: split.totalAmount,
      transactionDate: new Date(split.transactionDate)
    });

    this.splitFriendEntries = split.shares.map(s => ({
      friendId: s.friendId,
      friendName: s.friendName,
      avatarColor: s.friendAvatarColor || '#6366f1',
      shareAmount: s.shareAmount,
      sharePercentage: s.sharePercentage,
      isPayer: s.isPayer
    }));

    this.updateAvailableFriends();
    this.showSplitDialog = true;
  }

  addFriendToSplit(friend: FriendResponse): void {
    if (this.splitFriendEntries.some(e => e.friendId === friend.id)) return;
    this.splitFriendEntries.push({
      friendId: friend.id,
      friendName: friend.name,
      avatarColor: friend.avatarColor,
      shareAmount: 0,
      sharePercentage: null,
      isPayer: false
    });
    this.updateAvailableFriends();
    this.recalculateShares();
  }

  removeFriendFromSplit(index: number): void {
    if (this.splitFriendEntries[index].friendId === null) return; // Can't remove "Me"
    this.splitFriendEntries.splice(index, 1);
    this.updateAvailableFriends();
    this.recalculateShares();
  }

  updateAvailableFriends(): void {
    const usedIds = new Set(this.splitFriendEntries.filter(e => e.friendId !== null).map(e => e.friendId));
    this.availableFriendsForSplit = this.friends.filter(f => !usedIds.has(f.id));
  }

  onSplitTypeChange(type: SplitType): void {
    this.selectedSplitType = type;
    this.recalculateShares();
  }

  recalculateShares(): void {
    const totalAmount = this.splitForm?.get('totalAmount')?.value || 0;
    if (this.selectedSplitType === 'EQUAL' && this.splitFriendEntries.length > 0) {
      const count = this.splitFriendEntries.length;
      const equalAmount = Math.floor((totalAmount / count) * 100) / 100;
      const remainder = Math.round((totalAmount - equalAmount * count) * 100) / 100;
      this.splitFriendEntries.forEach((entry, i) => {
        entry.shareAmount = i === 0 ? equalAmount + remainder : equalAmount;
      });
    }
    this.cdr.detectChanges();
  }

  onTotalAmountChange(): void {
    this.recalculateShares();
  }

  getSplitTotal(): number {
    return this.splitFriendEntries.reduce((sum, e) => sum + (e.shareAmount || 0), 0);
  }

  saveSplit(): void {
    if (this.splitForm?.invalid) {
      Object.keys(this.splitForm.controls).forEach(key => {
        this.splitForm?.get(key)?.markAsTouched();
      });
      return;
    }

    if (this.splitFriendEntries.length < 2) {
      this.messageService.add({ severity: 'warn', summary: 'Warning', detail: 'Add at least one friend to split with' });
      return;
    }

    const formValue = this.splitForm!.value;
    const shares: SplitShareRequest[] = this.splitFriendEntries.map(entry => ({
      friendId: entry.friendId,
      shareAmount: entry.shareAmount,
      sharePercentage: entry.sharePercentage || undefined,
      isPayer: entry.isPayer
    }));

    const request: SplitTransactionRequest = {
      sourceTransactionId: formValue.sourceTransactionId,
      description: formValue.description,
      categoryName: formValue.categoryName,
      totalAmount: formValue.totalAmount,
      splitType: this.selectedSplitType,
      transactionDate: this.formatDate(formValue.transactionDate),
      shares
    };

    if (this.isEditSplit && this.selectedSplit) {
      this.splitService.update(this.selectedSplit.id, request).subscribe({
        next: () => {
          this.messageService.add({ severity: 'success', summary: 'Success', detail: 'Split updated' });
          this.showSplitDialog = false;
          this.loadData();
        },
        error: (err) => {
          this.messageService.add({ severity: 'error', summary: 'Error', detail: err?.error?.message || 'Failed to update split' });
        }
      });
    } else {
      this.splitService.create(request).subscribe({
        next: () => {
          this.messageService.add({ severity: 'success', summary: 'Success', detail: 'Split created' });
          this.showSplitDialog = false;
          this.loadData();
        },
        error: (err) => {
          this.messageService.add({ severity: 'error', summary: 'Error', detail: err?.error?.message || 'Failed to create split' });
        }
      });
    }
  }

  deleteSplit(split: SplitTransactionResponse): void {
    this.confirmationService.confirm({
      key: 'splits',
      message: `Delete split "${split.description}"?`,
      header: 'Confirm Delete',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.splitService.delete(split.id).subscribe({
          next: () => {
            this.messageService.add({ severity: 'success', summary: 'Success', detail: 'Split deleted' });
            this.loadData();
          },
          error: () => {
            this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Failed to delete split' });
          }
        });
      }
    });
  }

  // ── Settle Dialog ──

  openSettleDialog(fb: FriendBalanceResponse): void {
    this.settleFriend = fb;
    this.settleLoading = true;
    this.showSettleDialog = true;
    this.splitService.getSharesByFriend(fb.friendId).subscribe({
      next: (data) => {
        this.settleShares = data.filter(s => !s.isSettled);
        this.settleLoading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.settleLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  confirmSettle(): void {
    if (!this.settleFriend) return;
    this.splitService.settle({ friendId: this.settleFriend.friendId }).subscribe({
      next: () => {
        this.messageService.add({ severity: 'success', summary: 'Success', detail: `Settled with ${this.settleFriend!.friendName}` });
        this.showSettleDialog = false;
        this.loadData();
      },
      error: () => {
        this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Settlement failed' });
      }
    });
  }

  exportFriendCSV(fb: FriendBalanceResponse): void {
    this.splitService.getSharesByFriend(fb.friendId).subscribe({
      next: (shares) => {
        this.splitService.exportFriendShares(shares, fb.friendName);
        this.messageService.add({ severity: 'success', summary: 'Success', detail: 'CSV exported' });
      },
      error: () => {
        this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Export failed' });
      }
    });
  }

  // ── Helpers ──

  getFriend(friendId: number): FriendResponse | undefined {
    return this.friends.find(f => f.id === friendId);
  }

  getInitials(name: string): string {
    if (!name) return '?';
    const parts = name.trim().split(' ');
    if (parts.length >= 2) {
      return (parts[0][0] + parts[1][0]).toUpperCase();
    }
    return name.substring(0, 2).toUpperCase();
  }

  formatDate(date: Date | string): string {
    if (typeof date === 'string') return date;
    return date.toISOString().split('T')[0];
  }

  onFriendAvatarSelected(file: File): void {
    if (!this.selectedFriend) return;
    this.friendService.uploadAvatar(this.selectedFriend.id, file).subscribe({
      next: (updated) => {
        // Update in-place so the ring preview refreshes
        if (this.selectedFriend) {
          this.selectedFriend.avatarBase64 = updated.avatarBase64;
        }
        // Also refresh the friends list
        this.loadFriends();
        this.messageService.add({ severity: 'success', summary: 'Avatar Updated', detail: 'Friend avatar saved.' });
      },
      error: () => {
        this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Failed to upload friend avatar.' });
      }
    });
  }

  onFriendAvatarRemoved(): void {
    if (!this.selectedFriend) return;
    this.friendService.deleteAvatar(this.selectedFriend.id).subscribe({
      next: (updated) => {
        if (this.selectedFriend) {
          this.selectedFriend.avatarBase64 = null;
        }
        this.loadFriends();
        this.messageService.add({ severity: 'info', summary: 'Avatar Removed', detail: 'Friend avatar removed.' });
      },
      error: () => {
        this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Failed to remove friend avatar.' });
      }
    });
  }
}
