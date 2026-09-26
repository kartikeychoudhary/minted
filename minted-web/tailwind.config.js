/** @type {import('tailwindcss').Config} */

// Theme-aware colour helper: lets `bg-primary/10` etc. work with a CSS variable.
const cssVar = (name) => ({ opacityValue }) =>
  opacityValue === undefined || opacityValue === '1'
    ? `var(${name})`
    : `color-mix(in srgb, var(${name}) calc(${opacityValue} * 100%), transparent)`;

module.exports = {
  content: [
    "./src/**/*.{html,ts}",
  ],
  theme: {
    extend: {
      colors: {
        'primary': cssVar('--minted-accent'),
        'primary-dark': cssVar('--minted-accent-hover'),
        'minted-green': '#0f3d32',
        'minted-green-light': '#1a5446',
        'background-light': '#f8f7f6',
        'background-dark': '#201b12',
        'minted': {
          50: '#f0fdf4',
          100: '#dcfce7',
          200: '#bbf7d0',
          300: '#86efac',
          400: '#4ade80',
          500: '#22c55e',  // Primary brand color
          600: '#16a34a',
          700: '#15803d',
          800: '#166534',
          900: '#14532d',
          // Theme tokens (follow light/dark mode + accent)
          'primary': cssVar('--minted-accent'),
          'text-primary': cssVar('--minted-text-primary'),
          'text-secondary': cssVar('--minted-text-secondary'),
          'text-muted': cssVar('--minted-text-muted'),
          'border': cssVar('--minted-border'),
          'surface': cssVar('--minted-bg-surface'),
          'bg-app': cssVar('--minted-bg-page'),
          'card': cssVar('--minted-bg-card'),
        }
      },
      fontFamily: {
        'display': ['Inter', 'sans-serif']
      }
    },
  },
  plugins: [],
  // IMPORTANT: Do not let Tailwind purge PrimeNG or AG Grid classes
  safelist: [
    { pattern: /^p-/ },
    { pattern: /^ag-/ },
  ]
}
