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
        name: 'home-module',
        filename: 'remoteEntry.js',
        remotes: {
          'content-module': remoteEntryUrl('VITE_CONTENT_MODULE_URL'),
          'phishing-module': remoteEntryUrl('VITE_PHISHING_MODULE_URL'),
        },
        exposes: {
          './ROLE': './src/utils/Role.ts',
          './security': './src/utils/Security.ts',

          './AuthProvider': './src/providers/AuthProvider.tsx',
          './APIClientProvider': './src/providers/APIClientProvider.tsx',
          './StoreProvider': './src/providers/StoreProvider.tsx',

          './useAuth': './src/hooks/UseAuth.ts',
          './useAPI': './src/hooks/UseAPI.ts',
          './useStore': './src/hooks/UseStore.ts',
          './useUploader': './src/hooks/UseUploader.ts',

          './TextEditor': './src/components/common/TextEditor.tsx',
          './HtmlEditor': './src/components/html-editor/HtmlEditor.tsx',
          './HtmlEditorTypes': './src/components/html-editor/types.ts',

          './AuthLayout': './src/components/layouts/AuthLayout.tsx',
          './AspireAdminLayout':
            './src/components/layouts/AspireAdminLayout.tsx',
          './ClientAdminLayout':
            './src/components/layouts/ClientAdminLayout.tsx',
          './SelfClientAdminLayout':
            './src/components/layouts/SelfClientAdminLayout.tsx',
          './ClientUserLayout': './src/components/layouts/ClientUserLayout.tsx',
          './ClientUserAccountLayout':
            './src/components/layouts/ClientUserAccountLayout.tsx',
          './MspLayout': './src/components/layouts/MspLayout.tsx',

          './Dashboard': './src/pages/dashboard/Dashboard.tsx',
          './Login': './src/pages/auth/Login.tsx',
          './MFASetup': './src/pages/auth/TwoFactorAuthSetup.tsx',
          './MFAVerify': './src/pages/auth/TwoFactorAuthVerify.tsx',
          './MFASettings': './src/pages/account/MFASettings.tsx',
          './ResetPassword': './src/pages/auth/ResetPassword.tsx',
          './SetPassword': './src/pages/auth/SetPassword.tsx',
          './RequestResetPassword': './src/pages/auth/RequestResetPassword.tsx',
        },
        shared: sharedDependencies,
      }),
      federationCSS(),
    ],
    // Path Alias
    resolve: {
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
