import federation from '@originjs/vite-plugin-federation';
import react from '@vitejs/plugin-react';
import path from 'path';
import { defineConfig, loadEnv } from 'vite';
import { federationCSS } from './federationCSS.mjs';

const trimTrailingSlash = (value: string) => value.replace(/\/+$/, '');

const getRequiredEnv = (env: Record<string, string>, key: string) => {
  const value = env[key]?.trim();
  if (!value) {
    throw new Error(`Missing required environment variable: ${key}`);
  }
  return trimTrailingSlash(value);
};

const sharedDependencies = {
  react: { singleton: true, requiredVersion: false },
  'react-dom': { singleton: true, requiredVersion: false },
  'react-router-dom': { singleton: true, requiredVersion: false },
  'react-toastify': { singleton: true, requiredVersion: false },
  recharts: { singleton: true, requiredVersion: false },
} as const;

// https://vitejs.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '');
  const remoteEntryUrl = (key: string) =>
    `${getRequiredEnv(env, key)}/assets/remoteEntry.js`;

  return defineConfig({
    base: `${trimTrailingSlash(getRequiredEnv(env, 'VITE_PUBLIC_URL'))}/`,
    plugins: [
      react(),
      federation({
        name: 'host',
        filename: 'remoteEntry.js',
        remotes: {
          'home-module': remoteEntryUrl('VITE_HOME_MODULE_URL'),
          'user-module': remoteEntryUrl('VITE_USER_MODULE_URL'),
          'content-module': remoteEntryUrl('VITE_CONTENT_MODULE_URL'),
          'miscellaneous-module': remoteEntryUrl(
            'VITE_MISCELLANEOUS_MODULE_URL',
          ),
          'billing-module': remoteEntryUrl('VITE_BILLING_MODULE_URL'),
          'account-module': remoteEntryUrl('VITE_ACCOUNT_MODULE_URL'),
          'phishing-module': remoteEntryUrl('VITE_PHISHING_MODULE_URL'),
        },
        exposes: {},
        shared: sharedDependencies,
      }),
      federationCSS(),
    ],
    // Path Alias
    resolve: {
      dedupe: ['react', 'react-dom', 'react-router-dom', 'recharts'],
      alias: {
        assets: path.resolve(__dirname, './src/assets'),
        common: path.resolve(__dirname, './src/components/common'),
        components: path.resolve(__dirname, './src/components'),
        contexts: path.resolve(__dirname, './src/contexts'),
        features: path.resolve(__dirname, './src/features'),
        hooks: path.resolve(__dirname, './src/hooks'),
        models: path.resolve(__dirname, './src/models'),
        pages: path.resolve(__dirname, './src/pages'),
        providers: path.resolve(__dirname, './src/providers'),
        routes: path.resolve(__dirname, './src/routes'),
        schemas: path.resolve(__dirname, './src/schemas'),
        services: path.resolve(__dirname, './src/services'),
        styles: path.resolve(__dirname, './src/styles'),
        utils: path.resolve(__dirname, './src/utils'),
      },
    },
    build: {
      modulePreload: false,
      target: 'esnext',
      minify: 'esbuild',
      cssCodeSplit: false,
      assetsDir: 'assets',
      rollupOptions: {
        output: {
          format: 'esm',
          entryFileNames: 'assets/[name]-[hash].js',
          chunkFileNames: 'assets/[name]-[hash].js',
          assetFileNames: 'assets/[name]-[hash][extname]',
        },
      },
    },
    server: {
      proxy: {
        '/gateway': {
          target: 'http://localhost:7030',
          changeOrigin: true,
        },
      },
      hmr: {
        overlay: false, // Disable full-page reloads
      },
    },
  });
});
