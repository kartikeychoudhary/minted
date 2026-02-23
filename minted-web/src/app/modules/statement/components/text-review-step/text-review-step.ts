import { Component, Input, Output, EventEmitter, ChangeDetectorRef } from '@angular/core';
import { MessageService } from 'primeng/api';
import { StatementService } from '../../../../core/services/statement.service';
import { CreditCardStatement } from '../../../../core/models/statement.model';

@Component({
  selector: 'app-text-review-step',
  standalone: false,
  templateUrl: './text-review-step.html',
  styleUrl: './text-review-step.scss'
})
export class TextReviewStep {
  @Input() statement!: CreditCardStatement;
  @Output() statementUpdated = new EventEmitter<CreditCardStatement>();
  @Output() startPolling = new EventEmitter<void>();

  parsing = false;

  constructor(
    private statementService: StatementService,
    private messageService: MessageService,
    private cdr: ChangeDetectorRef
  ) {}

  get charCount(): number {
    return this.statement?.extractedText?.length || 0;
  }

  triggerParse(): void {
    this.parsing = true;
    this.statementService.triggerParse(this.statement.id).subscribe({
      next: (stmt) => {
        this.messageService.add({
          severity: 'info',
          summary: 'AI Parsing Started',
          detail: 'AI is parsing your statement. This may take 10-30 seconds.'
        });
        this.startPolling.emit();
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.parsing = false;
        this.messageService.add({
          severity: 'error',
          summary: 'Parse Failed',
          detail: err.error?.message || 'Failed to start AI parsing.'
        });
        this.cdr.detectChanges();
      }
    });
  }
}
