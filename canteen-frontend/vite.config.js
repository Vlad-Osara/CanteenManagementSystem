import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  define: {
    global: 'window',
  },
  server: {
    port: 5173,
    allowedHosts: ['huyloi.uk', '.huyloi.uk'],
    // Use proxy to forward API requests to the backend server in development mode
    // This is useful for avoiding CORS issues when the frontend and backend are served from different origins
    // Drop proxy configuration when deploying to production
    // Use Cloudflare Tunnel Ingress to forward requests to the backend server in production mode
    // proxy: {
    //   '/api': {
    //     target: 'http://localhost:8084',
    //     changeOrigin: true,
    //     secure: false,
    //   },
    //   '/ws': {
    //     target: 'http://localhost:8084',
    //     ws: true,
    //     changeOrigin: true
    //   }
    // }
  }
})
