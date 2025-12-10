import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
    open: true,
    proxy: {
      '/nominatim': {
        target: 'https://nominatim.openstreetmap.org',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/nominatim/, ''),
        headers: {
          'User-Agent': 'MapRouteExplorer/3.0.0',
        },
      },
      '/n8n': {
        target: process.env.N8N_PROXY_TARGET || 'https://yocomsn8n.duckdns.org',
        changeOrigin: true,
        secure: true,
        rewrite: (path) => path.replace(/^\/n8n/, '/webhook'),
        headers: {
          'User-Agent': 'MapRouteExplorer/3.0.0',
        },
      },
    },
  },
})
