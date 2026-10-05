import tailwindcssAnimate from 'tailwindcss-animate';

/** @type {import('tailwindcss').Config} */
export default {
  darkMode: ['class'],
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        'vibrant-red': '#ED4337',
        'steel-gray': '#7C7C7C',
        graphite: '#686868',
        dark: '#192129',

        background: '#0f1419',
        foreground: '#ffffff',

        primary: {
          DEFAULT: '#00d4aa',
          foreground: '#000000',
        },
        secondary: {
          DEFAULT: '#1a2332',
          foreground: '#ffffff',
        },
        destructive: {
          DEFAULT: '#d32f2f',
          foreground: '#f8fafc',
        },
        muted: {
          DEFAULT: '#1a2332',
          foreground: '#94a3b8',
        },
        card: {
          DEFAULT: '#00000000',
          background: '#1a2332',
          foreground: '#ffffff',
          border: '#6f7781',
        },
      },
      keyframes: {
        'accordion-down': {
          from: {
            height: '0',
          },
          to: {
            height: 'var(--radix-accordion-content-height)',
          },
        },
        'accordion-up': {
          from: {
            height: 'var(--radix-accordion-content-height)',
          },
          to: {
            height: '0',
          },
        },
      },
      animation: {
        'accordion-down': 'accordion-down 0.2s ease-out',
        'accordion-up': 'accordion-up 0.2s ease-out',
      },
      fontFamily: {
        outfit: ['Outfit', 'sans-serif'],
      },
      borderRadius: {
        lg: 'var(--radius)',
        md: 'calc(var(--radius) - 2px)',
        sm: 'calc(var(--radius) - 4px)',
      },
    },
  },
  plugins: [tailwindcssAnimate],
};
