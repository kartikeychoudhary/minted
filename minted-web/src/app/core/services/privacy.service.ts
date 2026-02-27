import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class PrivacyService {
  private readonly STORAGE_KEY = 'minted-privacy-mode';

  private isPrivacyModeSubject = new BehaviorSubject<boolean>(false);
  isPrivacyMode$ = this.isPrivacyModeSubject.asObservable();

  init(): void {
    const saved = localStorage.getItem(this.STORAGE_KEY) === 'true';
    this.applyPrivacyMode(saved);
  }

  togglePrivacyMode(): void {
    const newValue = !this.isPrivacyModeSubject.value;
    this.applyPrivacyMode(newValue);
    localStorage.setItem(this.STORAGE_KEY, String(newValue));
  }

  get isPrivacyMode(): boolean {
    return this.isPrivacyModeSubject.value;
  }

  private applyPrivacyMode(enabled: boolean): void {
    const html = document.documentElement;
    if (enabled) {
      html.classList.add('privacy-mode');
    } else {
      html.classList.remove('privacy-mode');
    }
    this.isPrivacyModeSubject.next(enabled);
  }
}
