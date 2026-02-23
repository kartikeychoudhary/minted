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

  onFileSelect(event: any): void {
    const file = event.files?.[0] || event.target?.files?.[0];
    if (file) {
      if (file.type !== 'application/pdf') {
        this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Only PDF files are accepted.' });
        return;
      }
      if (file.size > 20 * 1024 * 1024) {
        this.messageService.add({ severity: 'error', summary: 'Error', detail: 'File size exceeds 20MB limit.' });
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
        this.messageService.add({ severity: 'success', summary: 'Success', detail: 'Text extracted from PDF successfully.' });
        this.router.navigate(['/statements', stmt.id]);
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.uploading = false;
        this.messageService.add({
          severity: 'error',
          summary: 'Upload Failed',
          detail: err.error?.message || 'Failed to process PDF statement.'
        });
        this.cdr.detectChanges();
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/statements']);
  }
}
