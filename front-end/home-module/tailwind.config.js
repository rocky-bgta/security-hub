/** @type {import('tailwindcss').Config} */

export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  prefix: 'home-',
  theme: {
    extend: {
      colors: {
        'stormy-gray': '#8798AD',
        'vibrant-red': '#ED4337',
        'cloudy-white': '#FFFFFFBF',
        'steel-gray': '#7C7C7C',
        'deep-ocean': '#192129',
        'ash-gray': '#9A9A9A',
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
      boxShadow: {
        'soft-shadow': '0px 0px 10px 0px rgba(0, 0, 0, 0.1)',
      },
      spacing: {
        140: '560px',
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
  // eslint-disable-next-line @typescript-eslint/no-require-imports, no-undef
  plugins: [require('tailwindcss-animate')],
};
