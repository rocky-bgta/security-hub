/** @type {import('tailwindcss').Config} */

export default {
  prefix: 'content-',
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        'soft-blue-gray': '#B0BAC9',
        'vibrant-red': '#ED4337',
        'light-blue': '#42B9EA',
        'stormy-gray': '#8798AD',
        success: '#46C089',
        'dark-blue': '#253340',
        'cloudy-white': '#FFFFFFBF',
        'ash-gray': '#9A9A9A',
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
      backgroundImage: {
        'primary-gradient': 'linear-gradient(180deg, #084c94, #163560 101.93%)',
      },
      fontFamily: {
        outfit: ['Outfit', 'sans-serif'],
      },
    },
  },
  plugins: [require('tailwind-scrollbar-hide')],
};
