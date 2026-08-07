import type { Config } from 'tailwindcss';

/**
 * Tailwind consumes CSS variables set in globals.css (mapped from
 * design/tokens/*.json). Semantic names — no hex codes in components.
 */
const config: Config = {
  content: ['./src/**/*.{ts,tsx}'],
  darkMode: 'class',
  theme: {
    container: { center: true, padding: '2rem', screens: { '2xl': '1400px' } },
    extend: {
      colors: {
        canvas: 'var(--color-bg-canvas)',
        surface: 'var(--color-bg-surface)',
        raised: 'var(--color-bg-raised)',
        sunken: 'var(--color-bg-sunken)',
        brand: {
          DEFAULT: 'var(--color-brand)',
          subtle: 'var(--color-brand-subtle)',
          fg: 'var(--color-text-brand)',
        },
        fg: {
          DEFAULT: 'var(--color-text-primary)',
          muted: 'var(--color-text-secondary)',
          subtle: 'var(--color-text-tertiary)',
          inverse: 'var(--color-text-inverse)',
        },
        border: {
          DEFAULT: 'var(--color-border-default)',
          subtle: 'var(--color-border-subtle)',
          strong: 'var(--color-border-strong)',
        },
        success: { DEFAULT: 'var(--color-status-success)', bg: 'var(--color-status-success-bg)' },
        danger:  { DEFAULT: 'var(--color-status-danger)',  bg: 'var(--color-status-danger-bg)' },
        warning: { DEFAULT: 'var(--color-status-warning)', bg: 'var(--color-status-warning-bg)' },
        info:    { DEFAULT: 'var(--color-status-info)',    bg: 'var(--color-status-info-bg)' },
      },
      fontFamily: {
        sans: ['var(--font-sans)', 'Inter', 'system-ui', 'sans-serif'],
        mono: ['ui-monospace', 'SFMono-Regular', 'monospace'],
      },
      borderRadius: {
        DEFAULT: 'var(--radius-md)',
        sm: 'var(--radius-sm)',
        md: 'var(--radius-md)',
        lg: 'var(--radius-lg)',
        xl: 'var(--radius-xl)',
      },
      boxShadow: {
        sm: '0 1px 2px 0 rgb(0 0 0 / 0.05)',
        md: '0 4px 8px 0 rgb(0 0 0 / 0.10)',
        lg: '0 10px 20px 0 rgb(0 0 0 / 0.12)',
      },
    },
  },
  plugins: [],
};

export default config;
