import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { updatePreset } from '@primeng/themes';

export interface AccentPreset {
  name: string;
  label: string;
  color: string;
  palette: string;
}

@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly DARK_MODE_KEY = 'minted-dark-mode';
  private readonly ACCENT_KEY = 'minted-accent';

  private isDarkModeSubject = new BehaviorSubject<boolean>(false);
  isDarkMode$ = this.isDarkModeSubject.asObservable();

  private accentColorSubject = new BehaviorSubject<string>('#c48821');
  accentColor$ = this.accentColorSubject.asObservable();

  readonly accentPresets: AccentPreset[] = [
    { name: 'amber', label: 'Amber', color: '#c48821', palette: 'amber' },
    { name: 'emerald', label: 'Emerald', color: '#10b981', palette: 'emerald' },
    { name: 'blue', label: 'Blue', color: '#3b82f6', palette: 'blue' },
    { name: 'violet', label: 'Violet', color: '#8b5cf6', palette: 'violet' },
    { name: 'rose', label: 'Rose', color: '#f43f5e', palette: 'rose' },
    { name: 'teal', label: 'Teal', color: '#14b8a6', palette: 'teal' },
  ];

  init(): void {
    const savedDarkMode = localStorage.getItem(this.DARK_MODE_KEY) === 'true';
    const savedAccent = localStorage.getItem(this.ACCENT_KEY) || '#c48821';

    if (savedDarkMode) {
      this.applyDarkMode(true);
    }
    this.applyAccentColor(savedAccent);
  }

  toggleDarkMode(): void {
    const newValue = !this.isDarkModeSubject.value;
    this.applyDarkMode(newValue);
    localStorage.setItem(this.DARK_MODE_KEY, String(newValue));
  }

  setAccentColor(color: string): void {
    this.applyAccentColor(color);
    localStorage.setItem(this.ACCENT_KEY, color);
  }

  get currentAccentColor(): string {
    return this.accentColorSubject.value;
  }

  private applyDarkMode(isDark: boolean): void {
    const html = document.documentElement;
    if (isDark) {
      html.classList.add('dark-mode');
    } else {
      html.classList.remove('dark-mode');
    }
    this.isDarkModeSubject.next(isDark);
  }

  private applyAccentColor(color: string): void {
    document.documentElement.style.setProperty('--minted-accent', color);
    this.accentColorSubject.next(color);

    const preset = this.accentPresets.find((p) => p.color === color);
    if (preset) {
      const p = preset.palette;
      try {
        updatePreset({
          semantic: {
            primary: {
              50: `{${p}.50}`,
              100: `{${p}.100}`,
              200: `{${p}.200}`,
              300: `{${p}.300}`,
              400: `{${p}.400}`,
              500: `{${p}.500}`,
              600: `{${p}.600}`,
              700: `{${p}.700}`,
              800: `{${p}.800}`,
              900: `{${p}.900}`,
              950: `{${p}.950}`,
            },
          },
        });
      } catch (e) {
        console.warn('Failed to update PrimeNG preset:', e);
      }
    }
  }
}
