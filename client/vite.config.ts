import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'
import { visualizer } from 'rollup-plugin-visualizer'

// https://vite.dev/config/
export default defineConfig(({ mode }) => ({
  plugins: [
    vue(),
    // 번들 분석 (npm run build:analyze)
    mode === 'analyze' && visualizer({
      open: true,
      filename: 'dist/stats.html',
      gzipSize: true,
      brotliSize: true,
    }),
  ].filter(Boolean),
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/ws': {
        target: 'http://localhost:8081',
        changeOrigin: true,
        ws: true
      }
    }
  },
  build: {
    // 청크 사이즈 경고 임계값 (KB)
    chunkSizeWarningLimit: 500,
    rollupOptions: {
      output: {
        // 수동 청크 분리
        manualChunks: {
          // Vue 관련
          'vue-vendor': ['vue', 'vue-router', 'pinia'],
          // UI 라이브러리
          'ui-vendor': ['lucide-vue-next'],
          // 유틸리티
          'utils': ['axios', 'sockjs-client', '@stomp/stompjs'],
        }
      }
    }
  }
}))

