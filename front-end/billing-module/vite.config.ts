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
} as const;

// https://vitejs.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '');

  return defineConfig({
    base: `${trimTrailingSlash(getRequiredEnv(env, 'VITE_PUBLIC_URL'))}/`,
    plugins: [
      react(),
      federation({
        name: 'billing-module',
        filename: 'remoteEntry.js',
        remotes: {
          'home-module': `${getRequiredEnv(env, 'VITE_HOME_MODULE_URL')}/assets/remoteEntry.js`,
        },
        exposes: {
          './PendingPayment': './src/pages/payment/PendingPayment.tsx',
          './PaymentReport': './src/pages/payment/PaymentReport.tsx',
          './PaymentFailed': './src/pages/payment/PaymentFailed.tsx',
          './PaymentSuccess': './src/pages/payment/PaymentSuccess.tsx',

          './ClientBillingReport':
            './src/pages/payment/ClientBillingReport.tsx',

          './MSPPendingPayment': './src/pages/payment/MSPPendingPayment.tsx',
          './MSPPaymentHistory': './src/pages/payment/MSPPaymentHistory.tsx',
          './MSPPaymentReportDetails':
            './src/pages/payment/MSPPaymentReportDetails.tsx',
          './MSPManageLicense': './src/pages/payment/MSPManageLicense.tsx',

          './ClientPendingPayment':
            './src/pages/payment/ClientPendingPayment.tsx',
          './PaymentHistory': './src/pages/payment/PaymentHistory.tsx',
          './ClientPaymentHistory':
            './src/pages/payment/ClientPaymentHistory.tsx',
          './ClientManageLicense':
            './src/pages/payment/ClientManageLicense.tsx',

          './BillingHistory': './src/pages/payment/AspireBillingHistory.tsx',
          './BillingAnalytics':
            './src/pages/payment/AspireBillingAnalytics.tsx',

          './CouponList': './src/pages/CouponList.tsx',
          './CreditList': './src/pages/credit/CreditList.tsx',
          './CreditDetails': './src/pages/credit/CreditDetails.tsx',
          './CreditUseHistory': './src/pages/credit/CreditUseHistory.tsx',
          './VatList': './src/pages/vat/VatList.tsx',
          './TierList': './src/pages/settings/TierList.tsx',
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
