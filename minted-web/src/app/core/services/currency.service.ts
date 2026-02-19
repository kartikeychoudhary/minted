import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export interface CurrencyOption {
  code: string;
  label: string;
  locale: string;
}

@Injectable({ providedIn: 'root' })
export class CurrencyService {
  private readonly STORAGE_KEY = 'minted-currency';

  readonly currencies: CurrencyOption[] = [
    { code: 'USD', label: 'US Dollar ($)', locale: 'en-US' },
    { code: 'INR', label: 'Indian Rupee (₹)', locale: 'en-IN' },
    { code: 'EUR', label: 'Euro (€)', locale: 'en-EU' },
    { code: 'GBP', label: 'British Pound (£)', locale: 'en-GB' },
    { code: 'JPY', label: 'Japanese Yen (¥)', locale: 'ja-JP' },
    { code: 'CAD', label: 'Canadian Dollar (CA$)', locale: 'en-CA' },
    { code: 'AUD', label: 'Australian Dollar (A$)', locale: 'en-AU' },
    { code: 'CHF', label: 'Swiss Franc (CHF)', locale: 'de-CH' },
    { code: 'CNY', label: 'Chinese Yuan (¥)', locale: 'zh-CN' },
    { code: 'SGD', label: 'Singapore Dollar (S$)', locale: 'en-SG' },
  ];

  private currencySubject: BehaviorSubject<string>;
  currency$;

  constructor() {
    const saved = localStorage.getItem(this.STORAGE_KEY) || 'USD';
    this.currencySubject = new BehaviorSubject<string>(saved);
    this.currency$ = this.currencySubject.asObservable();
  }

  get currentCurrency(): string {
    return this.currencySubject.value;
  }

  get currentLocale(): string {
    return this.currencies.find(c => c.code === this.currencySubject.value)?.locale || 'en-US';
  }

  setCurrency(code: string): void {
    this.currencySubject.next(code);
    localStorage.setItem(this.STORAGE_KEY, code);
  }

  format(value: number): string {
    const safeValue = (value === null || value === undefined) ? 0 : value;
    return new Intl.NumberFormat(this.currentLocale, {
      style: 'currency',
      currency: this.currentCurrency
    }).format(safeValue);
  }
}
