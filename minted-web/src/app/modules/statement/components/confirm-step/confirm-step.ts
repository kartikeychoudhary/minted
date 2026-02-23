import { Component, Input } from '@angular/core';
import { Router } from '@angular/router';
import { CreditCardStatement } from '../../../../core/models/statement.model';

@Component({
  selector: 'app-confirm-step',
  standalone: false,
  templateUrl: './confirm-step.html',
  styleUrl: './confirm-step.scss'
})
export class ConfirmStep {
  @Input() statement!: CreditCardStatement;

  constructor(private router: Router) {}

  goToTransactions(): void {
    this.router.navigate(['/transactions']);
  }

  goToStatements(): void {
    this.router.navigate(['/statements']);
  }
}
