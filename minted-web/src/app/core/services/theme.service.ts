import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { updatePreset } from '@primeng/themes';

export interface AccentPreset {
  name: string;
  label: string;
  color: string;
  palette: string;
  hover: string;
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
    { name: 'amber', label: 'Amber', color: '#c48821', palette: 'amber', hover: '#a87315' },
    { name: 'emerald', label: 'Emerald', color: '#10b981', palette: 'emerald', hover: '#059669' },
    { name: 'blue', label: 'Blue', color: '#3b82f6', palette: 'blue', hover: '#2563eb' },
    { name: 'violet', label: 'Violet', color: '#8b5cf6', palette: 'violet', hover: '#7c3aed' },
    { name: 'rose', label: 'Rose', color: '#f43f5e', palette: 'rose', hover: '#e11d48' },
    { name: 'teal', label: 'Teal', color: '#14b8a6', palette: 'teal', hover: '#0d9488' },
  ];

  init(): void {
    const savedDarkMode = localStorage.getItem(this.DARK_MODE_KEY) === 'true';
    const savedAccent = localStorage.getItem(this.ACCENT_KEY) || '#c48821';

    this.applyDarkMode(savedDarkMode);
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
    const root = document.documentElement;
    const preset = this.accentPresets.find((p) => p.color === color);
    const hoverColor = preset ? preset.hover : this.darkenColor(color, 15);

    // Set all accent-related CSS variables
    root.style.setProperty('--minted-accent', color);
    root.style.setProperty('--minted-accent-hover', hoverColor);
    root.style.setProperty('--minted-accent-subtle', this.hexToRgba(color, 0.10));

    this.accentColorSubject.next(color);

    // Update PrimeNG Aura theme primary palette
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

  /** Darken a hex color by a percentage */
  private darkenColor(hex: string, percent: number): string {
    const num = parseInt(hex.replace('#', ''), 16);
    const r = Math.max(0, (num >> 16) - Math.round(2.55 * percent));
    const g = Math.max(0, ((num >> 8) & 0x00ff) - Math.round(2.55 * percent));
    const b = Math.max(0, (num & 0x0000ff) - Math.round(2.55 * percent));
    return `#${(0x1000000 + (r << 16) + (g << 8) + b).toString(16).slice(1)}`;
  }

  /** Convert hex to rgba string */
  private hexToRgba(hex: string, alpha: number): string {
    const num = parseInt(hex.replace('#', ''), 16);
    const r = (num >> 16) & 255;
    const g = (num >> 8) & 255;
    const b = num & 255;
    return `rgba(${r}, ${g}, ${b}, ${alpha})`;
  }
}
