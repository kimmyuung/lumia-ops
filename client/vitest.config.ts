import { fileURLToPath } from 'node:url'
import { mergeConfig, defineConfig, configDefaults } from 'vitest/config'
import viteConfig from './vite.config'

export default mergeConfig(
    viteConfig,
    defineConfig({
        test: {
            environment: 'jsdom',
            exclude: [...configDefaults.exclude, 'e2e/**'],
            root: fileURLToPath(new URL('./', import.meta.url)),
            globals: true,
            coverage: {
                provider: 'v8',
                reporter: ['text', 'json', 'html'],
                reportsDirectory: './coverage',
                exclude: [
                    'node_modules/**',
                    'dist/**',
                    '**/*.d.ts',
                    '**/*.config.*',
                    '**/index.ts',
                    'src/main.ts'
                ],
                thresholds: {
                    lines: 60,
                    functions: 60,
                    branches: 60,
                    statements: 60
                }
            }
        }
    })
)
