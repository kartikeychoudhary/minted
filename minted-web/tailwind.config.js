/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{html,ts}",
  ],
  theme: {
    extend: {
      colors: {
        'primary': '#c48821',
        'primary-dark': '#9d6d1a',
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
