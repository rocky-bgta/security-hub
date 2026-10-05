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
        name: 'content-module',
        filename: 'remoteEntry.js',
        remotes: {
          'home-module': `${getRequiredEnv(env, 'VITE_HOME_MODULE_URL')}/assets/remoteEntry.js`,
        },
        exposes: {
          './ClientUserDashboard': './src/pages/dashboard/Dashboard.tsx',

          './CourseHeader': './src/features/layout/CourseHeader.tsx',

          './ProductList': './src/pages/product/ProductList.tsx',
          './FeatureList': './src/pages/FeatureList.tsx',

          './PackageList': './src/pages/package/List.tsx',
          './CourseList': './src/pages/course/List.tsx',
          './CourseCards': './src/pages/course/Cards.tsx',
          './CourseChapters': './src/features/chapter/CourseWiseChapters.tsx',

          './ProductAnalytics': './src/pages/product/ProductAnalytics.tsx',
          './AssignedPackages': './src/pages/package/AssignedPackages.tsx',
          './AvailablePackages': './src/pages/package/AvailablePackages.tsx',
          './SubPackages': './src/pages/package/SubPackages.tsx',
          './PackageLicense': './src/pages/package/PackageLicense.tsx',
          './PerformanceReport': './src/pages/package/PerformanceReport.tsx',
          './CertificateHistory':
            './src/pages/certificate/CertificateHistory.tsx',
          './CertificateTemplates':
            './src/pages/certificate/CertificateTemplates.tsx',
          './CertificateIssued':
            './src/pages/certificate/CertificateIssued.tsx',
          './CertificateAnalytics':
            './src/pages/certificate/CertificateAnalytics.tsx',
          './UserActivityLogs': './src/pages/UserActivityLogs.tsx',
          './ClientLicenseHistory':
            './src/pages/package/ClientLicenseHistory.tsx',
          './MSPLicenseHistory':
            './src/pages/package/aspire-admin/MSPLicenseHistory.tsx',

          './Campaigns': './src/pages/Campaigns.tsx',
          './BookMarks': './src/pages/BookMarks.tsx',
          './Certificates': './src/pages/certificate/CertificateList.tsx',
          './CourseDetails': './src/pages/course/ViewDetails.tsx',
          './CourseComplete': './src/pages/course/CourseComplete.tsx',
          './ContentDetails': './src/pages/content/ContentView.tsx',
          './UserProfile': './src/pages/account/MyProfile.tsx',
          './ProfileSettings': './src/pages/account/ProfileSettings.tsx',
          './ChangePassword': './src/pages/account/ChangePassword.tsx',

          './TakeExam': './src/pages/TakeExam.tsx',
          './ExamResults': './src/pages/ExamResults.tsx',
          './PhishingList': './src/pages/PhishingList.tsx',

          './ExamAnalytics': './src/pages/exam/Analytics.tsx',
          './ExamsList': './src/pages/exam/ExamsList.tsx',
          './ExamLibrary': './src/pages/exam/ExamLibrary.tsx',
          './ExamineeReports': './src/pages/exam/ExamineeReports.tsx',
          './ExamSettings': './src/pages/exam/ExamSettings.tsx',
          './UserNotification': './src/pages/UserNotification.tsx',
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
