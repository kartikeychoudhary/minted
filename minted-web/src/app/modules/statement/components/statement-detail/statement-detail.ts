import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { StatementService } from '../../../../core/services/statement.service';
import { CreditCardStatement } from '../../../../core/models/statement.model';
import { Subscription, interval } from 'rxjs';
import { takeWhile } from 'rxjs/operators';

@Component({
  selector: 'app-statement-detail',
  standalone: false,
  templateUrl: './statement-detail.html',
  styleUrl: './statement-detail.scss'
})
export class StatementDetail implements OnInit, OnDestroy {
  statement: CreditCardStatement | null = null;
  loading = true;
  polling = false;
  private pollSub: Subscription | null = null;

  steps = [
    { label: 'Upload', icon: 'pi pi-upload' },
    { label: 'Review Text', icon: 'pi pi-file-edit' },
    { label: 'Preview', icon: 'pi pi-list-check' },
    { label: 'Import', icon: 'pi pi-check-circle' }
  ];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private statementService: StatementService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (id) {
      this.loadStatement(id);
    }
  }

  ngOnDestroy(): void {
    this.stopPolling();
  }

  loadStatement(id: number): void {
    this.loading = true;
    this.statementService.getStatement(id).subscribe({
      next: (stmt) => {
        this.statement = stmt;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.loading = false;
        this.router.navigate(['/statements']);
        this.cdr.detectChanges();
      }
    });
  }

  onStatementUpdated(stmt: CreditCardStatement): void {
    this.statement = stmt;
    this.cdr.detectChanges();
  }

  startPolling(): void {
    if (this.polling || !this.statement) return;
    this.polling = true;
    const stmtId = this.statement.id;

    this.pollSub = interval(3000).pipe(
      takeWhile(() => this.polling)
    ).subscribe(() => {
      this.statementService.getStatement(stmtId).subscribe({
        next: (stmt) => {
          this.statement = stmt;
          if (stmt.status !== 'TEXT_EXTRACTED' && stmt.status !== 'UPLOADED' && stmt.status !== 'SENT_FOR_AI_PARSING') {
            this.stopPolling();
          }
          this.cdr.detectChanges();
        }
      });
    });
  }

  stopPolling(): void {
    this.polling = false;
    this.pollSub?.unsubscribe();
    this.pollSub = null;
  }

  getActiveStep(): number {
    return this.statement ? this.statement.currentStep - 1 : 0;
  }

  goBack(): void {
    this.router.navigate(['/statements']);
  }
}
