import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// The dev server runs on http://localhost:5173 and forwards (proxies) every
// /api request and the /ws WebSocket to the Spring Boot backend on port 8080.
// The browser therefore only ever talks to one address.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    strictPort: true,
    proxy: {
      '/api': { target: 'http://localhost:8080', changeOrigin: true },
      '/ws': { target: 'ws://localhost:8080', ws: true },
    },
  },
});
