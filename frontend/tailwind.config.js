/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        ink: '#0A0A0A',
        panel: '#111111',
        panelSoft: '#161616',
        mist: '#F3F5F7',
        coral: '#FF6B57',
        coralSoft: '#FF9B8A',
        cyan: '#5EE7FF',
        violet: '#7C6CFF',
        gold: '#F4C75E',
        mint: '#6AE3B2',
      },
      boxShadow: {
        glow: '0 0 25px rgba(94, 231, 255, 0.25)',
        coral: '0 0 20px rgba(255, 107, 87, 0.35)',
        panel: '0 18px 50px rgba(0, 0, 0, 0.35)',
      },
      fontFamily: {
        sans: ['Inter', 'ui-sans-serif', 'system-ui', 'sans-serif'],
        display: ['Inter', 'ui-sans-serif', 'system-ui', 'sans-serif'],
      },
      keyframes: {
        float: {
          '0%, 100%': { transform: 'translateY(0px)' },
          '50%': { transform: 'translateY(-8px)' },
        },
        shimmer: {
          '0%': { backgroundPosition: '200% 0%' },
          '100%': { backgroundPosition: '-200% 0%' },
        },
        pulseGlow: {
          '0%, 100%': { boxShadow: '0 0 0 rgba(94, 231, 255, 0)', opacity: '0.9' },
          '50%': { boxShadow: '0 0 20px rgba(94, 231, 255, 0.4)', opacity: '1' },
        },
        fadeUp: {
          '0%': { opacity: '0', transform: 'translateY(18px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        },
      },
      animation: {
        float: 'float 6s ease-in-out infinite',
        shimmer: 'shimmer 2.6s linear infinite',
        pulseGlow: 'pulseGlow 2.8s ease-in-out infinite',
        fadeUp: 'fadeUp 0.4s ease-out both',
      },
      backgroundImage: {
        grid: 'linear-gradient(rgba(255,255,255,0.04) 1px, transparent 1px), linear-gradient(90deg, rgba(255,255,255,0.04) 1px, transparent 1px)',
      },
    },
  },
  plugins: [],
}
