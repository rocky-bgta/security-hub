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

  return defineConfig({
    base: `${trimTrailingSlash(getRequiredEnv(env, 'VITE_PUBLIC_URL'))}/`,
    plugins: [
      react(),
      federation({
        name: 'user-module',
        filename: 'remoteEntry.js',
        remotes: {
          'home-module': `${getRequiredEnv(env, 'VITE_HOME_MODULE_URL')}/assets/remoteEntry.js`,
        },
        exposes: {
          './Users': './src/pages/AllUsers.tsx',
          './SystemUsers': './src/pages/SystemUsers.tsx',

          './ActivityLogs': './src/pages/ActivityLogs.tsx',
          './Onboarding': './src/pages/aspire-admin/Onboarding.tsx',
          './BulkImport': './src/pages/BulkImport.tsx',
          './SyncUsers': './src/pages/aspire-admin/SyncUsers.tsx',
          './Reports': './src/pages/aspire-admin/Reports.tsx',
          './Analytics': './src/pages/aspire-admin/Analytics.tsx',
          './SuspendUser': './src/pages/aspire-admin/SuspendUser.tsx',
          './ClientUserList': './src/pages/aspire-admin/ClientUsersList.tsx',

          './MSPList': './src/pages/msp-admin/List.tsx',
          './MSPEdit': './src/pages/msp-admin/Edit.tsx',
          './MSPProfile': './src/pages/msp-admin/Profile.tsx',
          './MSPManageLicenses': './src/pages/msp-admin/ManageLicenses.tsx',
          './MSPSuspend': './src/pages/msp-admin/Suspend.tsx',
          './MSPAnalytics': './src/pages/msp-admin/Analytics.tsx',
          './MSPReports': './src/pages/msp-admin/Reports.tsx',
          './MSPActivityLogs': './src/pages/msp-admin/ActivityLogs.tsx',
          './MspAdminOnBoarding': './src/pages/msp-admin/OnBoarding.tsx',

          // msp and client admin onboarding pages shared by both modules
          './OnBoarding': './src/pages/OnBoarding.tsx',

          './ClientSelfOnBoarding':
            './src/pages/client-admin/SelfOnBoarding.tsx',
          './ClientBuyProduct': './src/pages/client-admin/BuyProduct.tsx',
          './ClientEdit': './src/pages/client-admin/Edit.tsx',
          './ClientList': './src/pages/client-admin/List.tsx',
          './ClientAnalytics': './src/pages/client-admin/Analytics.tsx',
          './ClientReports': './src/pages/client-admin/Reports.tsx',
          './ClientSuspend': './src/pages/client-admin/ClientSuspend.tsx',
          './ClientActivityLogs':
            './src/pages/client-admin/ClientActivityLogs.tsx',
          './ClientUserActivityLogs':
            './src/pages/client-admin/ClientUserActivityLogs.tsx',
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
      hmr: {
        overlay: false, // Disable full-page reloads
      },
    },
  });
});
