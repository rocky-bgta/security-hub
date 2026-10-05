import { Route, Routes } from 'react-router-dom';

import AIProviderConfiguration from 'pages/phishing/AIProviderConfiguration';
import BreachDashboard from 'pages/phishing/BreachDashboard';
import BreachManagement from 'pages/phishing/BreachManagement';
import CampaignCreate from 'pages/phishing/CampaignCreate';
import CampaignDetailReport from 'pages/phishing/CampaignDetailReport';
import CampaignDetails from 'pages/phishing/CampaignDetails';
import CampaignList from 'pages/phishing/CampaignList';
import CampaignReports from 'pages/phishing/CampaignReports';
import CampaignRiskImpacts from 'pages/phishing/CampaignRiskImpacts';
import Configurations from 'pages/phishing/Configurations';
import CourseStatistics from 'pages/phishing/CourseStatistics';
import DomainManagement from 'pages/phishing/DomainManagement';
import EmailTemplateCreate from 'pages/phishing/EmailTemplateCreate';
import LandingPageCreate from 'pages/phishing/LandingPageCreate';
import LandingPageLibrary from 'pages/phishing/LandingPageLibrary';
import PhishingDashboard from 'pages/phishing/PhishingDashboard';
import PhishingManagement from 'pages/phishing';
import PhishingReports from 'pages/phishing/PhishingReports';
import ProviderConfigurationRoute from 'pages/phishing/ProviderConfigurationRoute';
import RecentActivity from 'pages/phishing/RecentActivity';
import RecipientBreaches from 'pages/phishing/RecipientBreaches';
import SenderProfileManagement from 'pages/phishing/SenderProfileManagement';
import TemplateLibrary from 'pages/phishing/TemplateLibrary';
import UserPhishingRiskReport from 'pages/phishing/UserPhishingRiskReport';
import VoiceServerConfiguration from 'pages/vishing/VoiceServerConfiguration';
import { routes } from 'routes/AppRoutes';

const PhishingRouter = () => {
  return (
    <Routes>
      <Route index element={<PhishingManagement />} />

      <Route
        path={routes.phishingDashboard.path}
        element={<PhishingDashboard />}
      />
      <Route
        path={routes.phishingDomainManagement.path}
        element={<DomainManagement />}
      />
      <Route path={routes.phishingCampaings.path} element={<CampaignList />} />
      <Route
        path={routes.phishingCampaignCreate.path}
        element={<CampaignCreate />}
      />
      <Route
        path={routes.phishingCampaignEdit.path}
        element={<CampaignCreate />}
      />
      <Route
        path={routes.phishingCampaignDetails.path}
        element={<CampaignDetails />}
      />
      <Route
        path={routes.phishingTemplateLibrary.path}
        element={<TemplateLibrary />}
      />
      <Route
        path={routes.phishingTemplateLibraryCreate.path}
        element={<EmailTemplateCreate />}
      />
      <Route
        path={routes.phishingTemplateLibraryEdit.path}
        element={<EmailTemplateCreate />}
      />
      <Route
        path={routes.phishingLandingPages.path}
        element={<LandingPageLibrary />}
      />
      <Route
        path={routes.phishingLandingPageCreate.path}
        element={<LandingPageCreate />}
      />
      <Route
        path={routes.phishingLandingPageEdit.path}
        element={<LandingPageCreate />}
      />
      <Route
        path={routes.phishingSenderProfiles.path}
        element={<SenderProfileManagement />}
      />
      <Route path={routes.phishingReports.path} element={<PhishingReports />} />
      <Route
        path={routes.phishingCampaignRiskImpacts.path}
        element={<CampaignRiskImpacts />}
      />
      <Route
        path={routes.phishingCampaignReports.path}
        element={<CampaignReports />}
      />
      <Route
        path={routes.phishingCampaignDetailReport.path}
        element={<CampaignDetailReport />}
      />
      <Route
        path={routes.phishingUserRiskReport.path}
        element={<UserPhishingRiskReport />}
      />
      <Route
        path={routes.phishingRecentActivity.path}
        element={<RecentActivity />}
      />
      <Route
        path={routes.phishingCourseStatisticsDetails.path}
        element={<CourseStatistics />}
      />

      <Route
        path={routes.aiProviderConfiguration.path}
        element={<AIProviderConfiguration />}
      />
      <Route
        path={routes.providerConfiguration.path}
        element={<ProviderConfigurationRoute />}
      />
      <Route
        path={routes.voiceServerConfiguration.path}
        element={<VoiceServerConfiguration />}
      />
      <Route path={routes.configurations.path} element={<Configurations />} />

      <Route path={routes.breachDashboard.path} element={<BreachDashboard />} />
      <Route
        path={routes.breachManagement.path}
        element={<BreachManagement />}
      />
      <Route
        path={routes.recipientBreaches.path}
        element={<RecipientBreaches />}
      />
    </Routes>
  );
};

export default PhishingRouter;
