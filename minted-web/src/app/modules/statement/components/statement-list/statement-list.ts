import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';
import { StatementService } from '../../../../core/services/statement.service';
import { CreditCardStatement } from '../../../../core/models/statement.model';

@Component({
  selector: 'app-statement-list',
  standalone: false,
  templateUrl: './statement-list.html',
  styleUrl: './statement-list.scss'
})
export class StatementList implements OnInit {
  statements: CreditCardStatement[] = [];
  loading = true;

  constructor(
    private statementService: StatementService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadStatements();
  }

  loadStatements(): void {
    this.loading = true;
    this.statementService.getStatements().subscribe({
      next: (data) => {
        this.statements = data;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  newStatement(): void {
    this.router.navigate(['/statements/new']);
  }

  viewStatement(stmt: CreditCardStatement): void {
    this.router.navigate(['/statements', stmt.id]);
  }

  getStatusSeverity(status: string): 'success' | 'info' | 'warn' | 'danger' | 'secondary' {
    switch (status) {
      case 'COMPLETED': return 'success';
      case 'TEXT_EXTRACTED':
      case 'LLM_PARSED': return 'info';
      case 'UPLOADED':
      case 'CONFIRMING': return 'warn';
      case 'FAILED': return 'danger';
      default: return 'secondary';
    }
  }

  getStatusLabel(status: string): string {
    switch (status) {
      case 'UPLOADED': return 'Uploaded';
      case 'TEXT_EXTRACTED': return 'Text Extracted';
      case 'LLM_PARSED': return 'AI Parsed';
      case 'CONFIRMING': return 'Confirming';
      case 'COMPLETED': return 'Completed';
      case 'FAILED': return 'Failed';
      default: return status;
    }
  }

  getStepLabel(step: number): string {
    switch (step) {
      case 1: return 'Upload';
      case 2: return 'Review Text';
      case 3: return 'Preview Transactions';
      case 4: return 'Imported';
      default: return `Step ${step}`;
    }
  }

  formatFileSize(bytes: number): string {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
  }

  goBack(): void {
    this.router.navigate(['/']);
  }
}
