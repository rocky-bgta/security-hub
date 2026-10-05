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
        name: 'miscellaneous-module',
        filename: 'remoteEntry.js',
        remotes: {
          'home-module': `${getRequiredEnv(env, 'VITE_HOME_MODULE_URL')}/assets/remoteEntry.js`,
        },
        exposes: {
          './Roles': './src/pages/Roles.tsx',
          './Permissions': './src/pages/Permissions.tsx',
          './Menu': './src/pages/Menu.tsx',

          './CategoryList': './src/pages/Category.tsx',
          './Compliance': './src/pages/Compliance.tsx',
          './ContentType': './src/pages/ContentType.tsx',
          './Tags': './src/pages/Tags.tsx',
          './CountryList': './src/pages/Country.tsx',
          './Industries': './src/pages/Industries.tsx',
          './SubIndustries': './src/pages/SubIndustries.tsx',
          './OrganizationSizes': './src/pages/OrganizationSizes.tsx',
          './OrganizationTypes': './src/pages/OrganizationTypes.tsx',
          './TimeZone': './src/pages/TimeZone.tsx',
          './States': './src/pages/States.tsx',
          './Languages': './src/pages/Languages.tsx',
          './DepartmentList': './src/pages/DepartmentList.tsx',

          './AccessReport': './src/pages/report/AccessReport.tsx',
          './ContentReport': './src/pages/report/ContentReport.tsx',
          './ProductReport': './src/pages/report/ProductReport.tsx',
          './UserReport': './src/pages/report/UserReport.tsx',
          './SupportTicketReport':
            './src/pages/report/SupportTicketReport.tsx',
          './BillingPaymentReport':
            './src/pages/report/BillingPaymentReport.tsx',

          './CertificateReport': './src/pages/report/CertificateReport.tsx',
          './LicenseReport': './src/pages/report/LicenseReport.tsx',
          './PerformanceReport': './src/pages/report/PerformanceReport.tsx',
          './PhishingContentReport':
            './src/pages/report/PhishingContentReport.tsx',
          './UserActivityReport': './src/pages/report/UserActivityReport.tsx',
          './UserRiskReport': './src/pages/report/UserRiskReport.tsx',

          './PolicyList': './src/pages/policy/PolicyList.tsx',
          './RequestPolicy': './src/pages/policy/RequestNewPolicy.tsx',
          './AssignedPolicy': './src/pages/policy/AssignedPolicy.tsx',
          './RequestedPolicy': './src/pages/policy/RequestedPolicy.tsx',

          './SupportTickets': './src/pages/support-ticket/SupportTickets.tsx',
          './PendingTickets': './src/pages/support-ticket/PendingTickets.tsx',
          './ResolvedTickets': './src/pages/support-ticket/ResolvedTicket.tsx',
          './ManageResources': './src/pages/knowledge-hub/ManageResources.tsx',
          './ResourceAnalytics':
            './src/pages/knowledge-hub/ResourceAnalytics.tsx',
          './ResourceCategories':
            './src/pages/knowledge-hub/ResourceCategories.tsx',
          './KnowledgeHub': './src/pages/KnowledgeHub.tsx',
          './Leaderboard': './src/pages/Leaderboard.tsx',
          './PollSurvey': './src/pages/aspire-admin/PollSurvey.tsx',
          './LatestNews': './src/pages/aspire-admin/LatestNews.tsx',
          './BillingActions': './src/pages/BillingActions.tsx',
          './BillingNextStep': './src/pages/BillingNextStep.tsx',
          './PolicyType': './src/pages/PolicyType.tsx',
          './LatestNewsCategory': './src/pages/LatestNewsCategory.tsx',
          './SupportTicketType': './src/pages/SupportTicketType.tsx',
          './UserRanges': './src/pages/UserRanges.tsx',
          './MSPType': './src/pages/MSPType.tsx',
          './CreditReason': './src/pages/CreditReason.tsx',
          './NetTerm': './src/pages/NetTerm.tsx',
          './SuspendReason': './src/pages/SuspendReason.tsx',
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
