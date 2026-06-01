import type { Config } from 'tailwindcss';

const config: Config = {
  content: [
    './src/pages/**/*.{js,ts,jsx,tsx,mdx}',
    './src/components/**/*.{js,ts,jsx,tsx,mdx}',
    './src/app/**/*.{js,ts,jsx,tsx,mdx}',
  ],
  theme: {
    extend: {
      colors: {
        primary: {
          DEFAULT: '#1677ff',
          50: '#e6f4ff',
          100: '#bae0ff',
          200: '#91caff',
          300: '#69b1ff',
          400: '#4096ff',
          500: '#1677ff',
          600: '#0958d9',
          700: '#003eb3',
          800: '#002c8c',
          900: '#001d66',
        },
        secondary: {
          DEFAULT: '#722ed1',
          50: '#f9f0ff',
          100: '#efdbff',
          200: '#d3adf7',
          300: '#b37feb',
          400: '#9254de',
          500: '#722ed1',
          600: '#531dab',
          700: '#391085',
          800: '#22075e',
          900: '#120338',
        },
        success: '#52c41a',
        warning: '#faad14',
        error: '#ff4d4f',
      },
    },
  },
  plugins: [],
  corePlugins: {
    preflight: false,
  },
};
export default config;
