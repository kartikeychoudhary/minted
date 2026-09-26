import { definePreset } from '@primeng/themes';
import Aura from '@primeng/themes/aura';

/**
 * Minted PrimeNG preset.
 *
 * Every colour token points at a `--minted-*` CSS variable. Those variables are
 * redefined under `.dark-mode` (see styles.scss) and the accent variables are
 * updated at runtime by ThemeService, so both colour schemes can share a single
 * token map and PrimeNG components follow the app theme automatically.
 */
const schemeTokens = {
  primary: {
    color: 'var(--minted-accent)',
    contrastColor: '#ffffff',
    hoverColor: 'var(--minted-accent-hover)',
    activeColor: 'var(--minted-accent-hover)',
  },
  highlight: {
    background: 'var(--minted-accent-subtle)',
    focusBackground: 'var(--minted-accent-soft)',
    color: 'var(--minted-accent)',
    focusColor: 'var(--minted-accent)',
  },
  mask: {
    background: 'var(--minted-mask)',
    color: 'var(--minted-text-secondary)',
  },
  formField: {
    background: 'var(--minted-bg-input)',
    disabledBackground: 'var(--minted-bg-surface)',
    filledBackground: 'var(--minted-bg-surface)',
    filledHoverBackground: 'var(--minted-bg-surface)',
    filledFocusBackground: 'var(--minted-bg-surface)',
    borderColor: 'var(--minted-border)',
    hoverBorderColor: 'var(--minted-border-strong)',
    focusBorderColor: 'var(--minted-accent)',
    invalidBorderColor: 'var(--minted-danger)',
    color: 'var(--minted-text-primary)',
    disabledColor: 'var(--minted-text-muted)',
    placeholderColor: 'var(--minted-text-muted)',
    invalidPlaceholderColor: 'var(--minted-danger)',
    floatLabelColor: 'var(--minted-text-muted)',
    floatLabelFocusColor: 'var(--minted-accent)',
    floatLabelActiveColor: 'var(--minted-text-muted)',
    floatLabelInvalidColor: 'var(--minted-danger)',
    iconColor: 'var(--minted-text-muted)',
    shadow: 'var(--minted-shadow-xs)',
  },
  text: {
    color: 'var(--minted-text-primary)',
    hoverColor: 'var(--minted-text-primary)',
    mutedColor: 'var(--minted-text-muted)',
    hoverMutedColor: 'var(--minted-text-secondary)',
  },
  content: {
    background: 'var(--minted-bg-card)',
    hoverBackground: 'var(--minted-bg-hover)',
    borderColor: 'var(--minted-border)',
    color: 'var(--minted-text-primary)',
    hoverColor: 'var(--minted-text-primary)',
  },
  overlay: {
    select: {
      background: 'var(--minted-bg-elevated)',
      borderColor: 'var(--minted-border)',
      color: 'var(--minted-text-primary)',
    },
    popover: {
      background: 'var(--minted-bg-elevated)',
      borderColor: 'var(--minted-border)',
      color: 'var(--minted-text-primary)',
    },
    modal: {
      background: 'var(--minted-bg-card)',
      borderColor: 'var(--minted-border)',
      color: 'var(--minted-text-primary)',
    },
  },
  list: {
    option: {
      focusBackground: 'var(--minted-bg-hover)',
      selectedBackground: 'var(--minted-accent-subtle)',
      selectedFocusBackground: 'var(--minted-accent-soft)',
      color: 'var(--minted-text-primary)',
      focusColor: 'var(--minted-text-primary)',
      selectedColor: 'var(--minted-accent)',
      selectedFocusColor: 'var(--minted-accent)',
      icon: {
        color: 'var(--minted-text-muted)',
        focusColor: 'var(--minted-text-secondary)',
      },
    },
    optionGroup: {
      background: 'transparent',
      color: 'var(--minted-text-muted)',
    },
  },
  navigation: {
    item: {
      focusBackground: 'var(--minted-bg-hover)',
      activeBackground: 'var(--minted-bg-hover)',
      color: 'var(--minted-text-primary)',
      focusColor: 'var(--minted-text-primary)',
      activeColor: 'var(--minted-text-primary)',
      icon: {
        color: 'var(--minted-text-muted)',
        focusColor: 'var(--minted-text-secondary)',
        activeColor: 'var(--minted-text-secondary)',
      },
    },
    submenuLabel: {
      background: 'transparent',
      color: 'var(--minted-text-muted)',
    },
    submenuIcon: {
      color: 'var(--minted-text-muted)',
      focusColor: 'var(--minted-text-secondary)',
      activeColor: 'var(--minted-text-secondary)',
    },
  },
};

export const MintedPreset = definePreset(Aura, {
  primitive: {
    borderRadius: {
      none: '0',
      xs: '4px',
      sm: '6px',
      md: '10px',
      lg: '12px',
      xl: '16px',
    },
  },
  semantic: {
    focusRing: {
      width: '2px',
      style: 'solid',
      color: 'var(--minted-accent)',
      offset: '2px',
      shadow: 'none',
    },
    formField: {
      paddingX: '0.875rem',
      paddingY: '0.625rem',
      focusRing: {
        width: '0',
        style: 'none',
        color: 'transparent',
        offset: '0',
        shadow: '0 0 0 3px var(--minted-accent-ring)',
      },
    },
    list: {
      padding: '0.375rem',
      option: {
        padding: '0.5rem 0.75rem',
      },
    },
    overlay: {
      select: {
        shadow: 'var(--minted-shadow-lg)',
      },
      popover: {
        shadow: 'var(--minted-shadow-lg)',
      },
      modal: {
        padding: '1.5rem',
        shadow: 'var(--minted-shadow-xl)',
      },
    },
    colorScheme: {
      light: schemeTokens,
      dark: schemeTokens,
    },
  },
  components: {
    button: {
      root: {
        paddingX: '1rem',
        paddingY: '0.625rem',
        label: { fontWeight: '600' },
        sm: {
          fontSize: '0.8125rem',
          paddingX: '0.75rem',
          paddingY: '0.4375rem',
        },
      },
    },
    dialog: {
      title: { fontSize: '1.125rem', fontWeight: '700' },
    },
    tabs: {
      tab: {
        padding: '0.875rem 1rem',
        fontWeight: '600',
      },
      activeBar: { height: '2px' },
    },
  },
});
