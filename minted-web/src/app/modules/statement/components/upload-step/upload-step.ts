import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MessageService } from 'primeng/api';
import { StatementService } from '../../../../core/services/statement.service';
import { AccountService } from '../../../../core/services/account.service';
import { AccountResponse } from '../../../../core/models/account.model';

@Component({
  selector: 'app-upload-step',
  standalone: false,
  templateUrl: './upload-step.html',
  styleUrl: './upload-step.scss'
})
export class UploadStep implements OnInit {
  uploadForm: FormGroup;
  accounts: AccountResponse[] = [];
  accountOptions: { label: string; value: number }[] = [];
  selectedFile: File | null = null;
  uploading = false;
  showPassword = false;
  selectedFileType: 'PDF' | 'CSV' | 'TXT' = 'PDF';

  fileTypes = [
    { type: 'PDF' as const, label: 'PDF Statement', icon: 'pi pi-file-pdf', accept: '.pdf,application/pdf', description: 'Upload a PDF bank or credit card statement' },
    { type: 'CSV' as const, label: 'CSV File', icon: 'pi pi-file', accept: '.csv,text/csv', description: 'Upload a CSV file with transaction data' },
    { type: 'TXT' as const, label: 'Text File', icon: 'pi pi-file-edit', accept: '.txt,text/plain', description: 'Upload a plain text file with statement data' }
  ];

  constructor(
    private fb: FormBuilder,
    private statementService: StatementService,
    private accountService: AccountService,
    private messageService: MessageService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {
    this.uploadForm = this.fb.group({
      accountId: [null, Validators.required],
      pdfPassword: ['']
    });
  }

  ngOnInit(): void {
    this.loadAccounts();
  }

  loadAccounts(): void {
    this.accountService.getAll().subscribe({
      next: (data) => {
        this.accounts = data;
        this.accountOptions = data.map(a => ({ label: a.name, value: a.id }));
        this.cdr.detectChanges();
      }
    });
  }

  onFileTypeSelect(type: 'PDF' | 'CSV' | 'TXT'): void {
    this.selectedFileType = type;
    this.selectedFile = null;
  }

  get currentFileTypeConfig() {
    return this.fileTypes.find(f => f.type === this.selectedFileType)!;
  }

  onFileSelect(event: any): void {
    const file = event.files?.[0] || event.target?.files?.[0];
    if (file) {
      const maxSize = this.selectedFileType === 'PDF' ? 20 * 1024 * 1024 : 5 * 1024 * 1024;
      if (file.size > maxSize) {
        this.messageService.add({ severity: 'error', summary: 'Error', detail: `File size exceeds ${maxSize / (1024 * 1024)}MB limit.` });
        return;
      }
      this.selectedFile = file;
    }
  }

  removeFile(): void {
    this.selectedFile = null;
  }

  onUpload(): void {
    if (!this.selectedFile || this.uploadForm.invalid) return;

    this.uploading = true;
    const { accountId, pdfPassword } = this.uploadForm.value;

    this.statementService.upload(this.selectedFile, accountId, pdfPassword || undefined).subscribe({
      next: (stmt) => {
        this.uploading = false;
        this.messageService.add({ severity: 'success', summary: 'Success', detail: 'Text extracted successfully.' });
        this.router.navigate(['/statements', stmt.id]);
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.uploading = false;
        this.messageService.add({
          severity: 'error',
          summary: 'Upload Failed',
          detail: err.error?.message || 'Failed to process statement.'
        });
        this.cdr.detectChanges();
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/statements']);
  }
}
