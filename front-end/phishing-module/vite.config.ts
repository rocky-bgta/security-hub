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
  // Must match host `shared` so charts use the host React instance (avoids duplicate React / null dispatcher).
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
        name: 'phishing-module',
        filename: 'remoteEntry.js',
        remotes: {
          'home-module': `${getRequiredEnv(env, 'VITE_HOME_MODULE_URL')}/assets/remoteEntry.js`,
        },
        exposes: {
          // Domain Management
          './DomainManagement': './src/pages/domain/DomainManagement.tsx',

          // Template Library (Email & SMS)
          './TemplateLibrary':
            './src/pages/email-template/TemplateLibrary.tsx',
          './SmsTemplateLibrary':
            './src/pages/sms-template/SmsTemplateLibrary.tsx',
          './SmsTemplateCreate':
            './src/pages/sms-template/SmsTemplateCreate.tsx',
          './EmailTemplateCreate':
            './src/pages/email-template/EmailTemplateCreate.tsx',

          // Landing Page Management (Task-04, Task-05)
          './LandingPageLibrary':
            './src/pages/landing-page/LandingPageLibrary.tsx',
          './LandingPageCreate':
            './src/pages/landing-page/LandingPageCreate.tsx',

          // Sender Profile Management (Task-06)
          './SenderProfileManagement':
            './src/pages/sender-profile/SenderProfileManagement.tsx',

          // Campaign Management (Task-07)",
          './CreateCampaign': './src/pages/campaign/CreateCampaign.tsx',
          './CampaignList': './src/pages/campaign/CampaignList.tsx',
          './SmsCampaignList': './src/pages/sms-campaign/SmsCampaignList.tsx',
          './SmsCampaignDetails':
            './src/pages/sms-campaign/SmsCampaignDetails.tsx',
          './VishingCampaignList':
            './src/pages/vishing-simulation/VishingCampaignList.tsx',
          './VishingCampaignDetails':
            './src/pages/vishing-simulation/VishingCampaignDetails.tsx',
          './CampaignDetails': './src/pages/campaign/CampaignDetails.tsx',
          './CampaignRiskImpacts':
            './src/pages/dashboard/CampaignRiskImpacts.tsx',
          './CampaignReports': './src/pages/dashboard/CampaignReports.tsx',
          './CampaignDetailReport':
            './src/pages/dashboard/CampaignDetailReport.tsx',
          './UserPhishingRiskReport':
            './src/pages/dashboard/UserRiskReport.tsx',

          // Breach Detection (Task-08)
          './BreachDashboard': './src/pages/breach/BreachDashboard.tsx',
          './RecipientBreaches': './src/pages/breach/RecipientBreaches.tsx',
          './BreachManagement': './src/pages/breach/BreachManagement.tsx',

          // Dashboard & Analytics (Task-09)
          './PhishingDashboard': './src/pages/dashboard/PhishingDashboard.tsx',
          './VishingDashboard':
            './src/pages/vishing-simulation/VishingDashboard.tsx',
          './PhishingReports': './src/pages/reports/PhishingReports.tsx',
          './SmishingReports': './src/pages/reports/SmishingReports.tsx',
          './RecentActivity': './src/pages/reports/RecentActivity.tsx',

          './CreateDeepFake': './src/pages/deepfake/CreateDeepFake.tsx',
          './ContentLibrary': './src/pages/deepfake/ContentLibrary.tsx',

          './CourseStatistics':
            './src/pages/course-statistics/CourseStatisticsDetails.tsx',

          // Vishing Simulation
          './CreateVishingSimulation':
            './src/pages/vishing-simulation/CreateVishingSimulation.tsx',
          './AttackTemplateLibrary':
            './src/pages/vishing-simulation/AttackTemplateLibrary.tsx',

          // Smishing Simulation
          './SmishingDashboard':
            './src/pages/sms-campaign/SmishingDashboard.tsx',
          './CreateSmsCampaign':
            './src/pages/sms-campaign/CreateSmsCampaign.tsx',
          './SmsServerConfigurationList':
            './src/pages/sms-server-configuration/SmsServerConfigurationList.tsx',

          // Configuration
          './Configurations': './src/pages/configuration/Configurations.tsx',

          // AI provider configuration
          './AIProviderConfiguration':
            './src/pages/ai-provider/AIProviderConfiguration.tsx',

          // Deepfake provider credentials
          './ProviderConfiguration':
            './src/pages/provider-credential/ProviderConfiguration.tsx',
          './DeepfakeProviderConfiguration':
            './src/pages/provider-credential/DeepfakeProviderConfiguration.tsx',
          './VishingProviderConfiguration':
            './src/pages/provider-credential/VishingProviderConfiguration.tsx',

          // Voice server configurations
          './VoiceServerConfiguration':
            './src/pages/voice-server-configuration/VoiceServerConfiguration.tsx',

          // Breach Monitor
          './BreachMonitorDashboard':
            './src/pages/breach-monitor/Dashboard.tsx',
          './EmailBreaches': './src/pages/breach-monitor/EmailBreaches.tsx',
          './IPBreaches': './src/pages/breach-monitor/IPBreaches.tsx',
          './IntelBreaches': './src/pages/breach-monitor/IntelBreaches.tsx',
          './BreachVulnerabilities':
            './src/pages/breach-monitor/BreachVulnerabilities.tsx',
          './CompanyImpersonation':
            './src/pages/breach-monitor/CompanyImpersonation.tsx',
        },
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
        features: path.resolve(__dirname, './src/features'),
        hooks: path.resolve(__dirname, './src/hooks'),
        models: path.resolve(__dirname, './src/models'),
        pages: path.resolve(__dirname, './src/pages'),
        providers: path.resolve(__dirname, './src/providers'),
        routes: path.resolve(__dirname, './src/routes'),
        schemas: path.resolve(__dirname, './src/schemas'),
        styles: path.resolve(__dirname, './src/styles'),
        utils: path.resolve(__dirname, './src/utils'),
      },
    },
    build: {
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
      port: 5170,
      strictPort: true,
      host: true,
      hmr: {
        overlay: false,
      },
    },
  });
});
