import { Navigate, Route, Routes } from 'react-router-dom';

import DomainManagement from 'pages/domain/DomainManagement';
import EmailTemplateCreate from 'pages/email-template/EmailTemplateCreate';
import EmailTemplateLibrary from 'pages/email-template/EmailTemplateLibrary';
import SmsTemplateLibrary from 'pages/sms-template/SmsTemplateLibrary';
import SmsTemplateCreate from 'pages/sms-template/SmsTemplateCreate';
import LandingPageCreate from 'pages/landing-page/LandingPageCreate';
import LandingPageLibrary from 'pages/landing-page/LandingPageLibrary';
import EmailSenderProfileList from 'pages/sender-profile/EmailSenderProfileList';
import SmsServerConfigurationList from 'pages/sms-server-configuration/SmsServerConfigurationList';
import CampaignDetails from 'pages/campaign/CampaignDetails';
import EmailCampaignList from 'pages/campaign/EmailCampaignList';
import CreateCampaign from 'pages/campaign/CreateCampaign';
import BreachDashboard from 'pages/breach/BreachDashboard';
import BreachManagement from 'pages/breach/BreachManagement';
import RecipientBreaches from 'pages/breach/RecipientBreaches';
import CampaignDetailReport from 'pages/dashboard/CampaignDetailReport';
import CampaignReports from 'pages/dashboard/CampaignReports';
import PhishingDashboard from 'pages/dashboard/PhishingDashboard';
import UserRiskReport from 'pages/dashboard/UserRiskReport';
import PhishingReports from 'pages/reports/PhishingReports';
import SmishingReports from 'pages/reports/SmishingReports';
import RecentActivity from 'pages/reports/RecentActivity';
import AIProviderConfiguration from 'pages/ai-provider/AIProviderConfiguration';
import ProviderConfiguration from 'pages/provider-credential/ProviderConfiguration';
import VoiceServerConfiguration from 'pages/voice-server-configuration/VoiceServerConfiguration';
import DeepfakeProviderConfiguration from 'pages/provider-credential/DeepfakeProviderConfiguration';
import VishingProviderConfiguration from 'pages/provider-credential/VishingProviderConfiguration';
import BaseLayout from 'components/layouts/BaseLayout';
import Login from 'pages/Login';
import MFASetup from 'pages/MFASetup';
import MFAVerify from 'pages/MFAVerify';
import NotFound from 'pages/NotFound';
import { routes } from 'routes/Routes';
import Configurations from 'pages/configuration/Configurations';
import BreachMonitorDashboard from 'pages/breach-monitor/Dashboard';
import EmailBreaches from 'pages/breach-monitor/EmailBreaches';
import IPBreaches from 'pages/breach-monitor/IPBreaches';
import CompanyImpersonation from 'pages/breach-monitor/CompanyImpersonation';
import BreachVulnerabilities from 'pages/breach-monitor/BreachVulnerabilities';
import IntelBreaches from 'pages/breach-monitor/IntelBreaches';
import DeepfakeGenerator from 'pages/deepfake/CreateDeepFake';
import ContentLibrary from 'pages/deepfake/ContentLibrary';
import CampaignRiskImpacts from 'pages/dashboard/CampaignRiskImpacts';
import CourseStatisticsDetails from 'pages/course-statistics/CourseStatisticsDetails';
import CreateSmsCampaign from 'pages/sms-campaign/CreateSmsCampaign';
import SmsCampaignList from 'pages/sms-campaign/SmsCampaignList';
import SmsCampaignDetails from 'pages/sms-campaign/SmsCampaignDetails';
import SmishingDashboard from 'pages/sms-campaign/SmishingDashboard';
import VishingPage from 'pages/vishing-simulation/CreateVishingSimulation';
import VishingCampaignList from 'pages/vishing-simulation/VishingCampaignList';
import VishingCampaignDetails from 'pages/vishing-simulation/VishingCampaignDetails';
import VishingDashboard from 'pages/vishing-simulation/VishingDashboard';
import AttackTemplateLibrary from 'pages/vishing-simulation/AttackTemplateLibrary';

const AppRouter = () => {
  return (
    <Routes>
      <Route path="/" element={<BaseLayout />}>
        <Route
          path="/"
          element={<Navigate to={routes.phishingDashboard.path} replace />}
        />

        {/* ----------------------------------------------------------------- */}
        {/* Phishing                                                          */}
        {/* ----------------------------------------------------------------- */}
        <Route
          path={routes.phishingDashboard.path}
          element={<PhishingDashboard />}
        />
        <Route
          path={routes.phishingDomainManagement.path}
          element={<DomainManagement />}
        />
        <Route
          path={routes.phishingCampaings.path}
          element={<EmailCampaignList />}
        />
        <Route
          path={routes.phishingCampaignCreate.path}
          element={<CreateCampaign />}
        />
        <Route
          path={routes.phishingCampaignDetails.path}
          element={<CampaignDetails />}
        />
        <Route
          path={routes.phishingCampaignEdit.path}
          element={<CreateCampaign />}
        />
        <Route
          path={routes.phishingTemplateLibrary.path}
          element={<EmailTemplateLibrary />}
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
          element={<EmailSenderProfileList />}
        />
        <Route
          path={routes.phishingReports.path}
          element={<PhishingReports />}
        />
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
          element={<UserRiskReport />}
        />
        <Route
          path={routes.phishingRecentActivity.path}
          element={<RecentActivity />}
        />
        <Route
          path={routes.phishingCourseStatisticsDetails.path}
          element={<CourseStatisticsDetails />}
        />

        {/* ----------------------------------------------------------------- */}
        {/* SMS Simulation                                                    */}
        {/* ----------------------------------------------------------------- */}
        <Route
          path={routes.smishingDashboard.path}
          element={<SmishingDashboard />}
        />
        <Route
          path={routes.smishingCampaigns.path}
          element={<SmsCampaignList />}
        />
        <Route
          path={routes.smishingCampaignDetails.path}
          element={<SmsCampaignDetails />}
        />
        <Route
          path={routes.smishingSimulationCreate.path}
          element={<CreateSmsCampaign />}
        />
        <Route
          path={routes.smishingSimulationEdit.path}
          element={<CreateSmsCampaign />}
        />
        <Route
          path={routes.smsTemplateLibrary.path}
          element={<SmsTemplateLibrary />}
        />
        <Route
          path={routes.smsTemplateLibraryCreate.path}
          element={<SmsTemplateCreate />}
        />
        <Route
          path={routes.smsTemplateLibraryEdit.path}
          element={<SmsTemplateCreate />}
        />
        <Route
          path={routes.smsServerConfigurations.path}
          element={<SmsServerConfigurationList />}
        />
        <Route
          path={routes.smishingLandingPages.path}
          element={<LandingPageLibrary />}
        />
        <Route
          path={routes.smishingLandingPageCreate.path}
          element={<LandingPageCreate />}
        />
        <Route
          path={routes.smishingLandingPageEdit.path}
          element={<LandingPageCreate />}
        />
        <Route
          path={routes.smishingReports.path}
          element={<SmishingReports />}
        />
        <Route
          path={routes.smishingCampaignRiskImpacts.path}
          element={<CampaignRiskImpacts channel="smishing" />}
        />
        <Route
          path={routes.smishingCourseStatisticsDetails.path}
          element={<CourseStatisticsDetails channel="smishing" />}
        />
        <Route
          path={routes.smishingCampaignReports.path}
          element={<CampaignReports channel="smishing" />}
        />
        <Route
          path={routes.smishingCampaignDetailReport.path}
          element={<CampaignDetailReport channel="smishing" />}
        />
        <Route
          path={routes.smishingUserRiskReport.path}
          element={<UserRiskReport channel="smishing" />}
        />
        <Route
          path={routes.smishingRecentActivity.path}
          element={<RecentActivity channel="smishing" />}
        />

        {/* ----------------------------------------------------------------- */}
        {/* AI Vishing                                                        */}
        {/* ----------------------------------------------------------------- */}
        <Route
          path={routes.vishingDashboard.path}
          element={<VishingDashboard />}
        />
        <Route
          path={routes.vishingCampaigns.path}
          element={<VishingCampaignList />}
        />
        <Route
          path={routes.vishingCampaignRiskImpacts.path}
          element={<CampaignRiskImpacts channel="vishing" />}
        />
        <Route
          path={routes.vishingCampaignDetails.path}
          element={<VishingCampaignDetails />}
        />
        <Route
          path={routes.vishingSimulationCreate.path}
          element={<VishingPage />}
        />
        <Route
          path={routes.vishingSimulationEdit.path}
          element={<VishingPage />}
        />
        <Route
          path={routes.vishingTemplateLibrary.path}
          element={<EmailTemplateLibrary />}
        />
        <Route
          path={routes.vishingAttackTemplates.path}
          element={<AttackTemplateLibrary />}
        />
        <Route
          path={routes.vishingReports.path}
          element={<PhishingReports channel="vishing" />}
        />
        <Route
          path={routes.vishingProviderConfiguration.path}
          element={<VishingProviderConfiguration />}
        />
        <Route
          path={routes.vishingCourseStatisticsDetails.path}
          element={<CourseStatisticsDetails channel="vishing" />}
        />
        <Route
          path={routes.vishingCampaignReports.path}
          element={<CampaignReports channel="vishing" />}
        />
        <Route
          path={routes.vishingCampaignDetailReport.path}
          element={<CampaignDetailReport channel="vishing" />}
        />
        <Route
          path={routes.vishingUserRiskReport.path}
          element={<UserRiskReport channel="vishing" />}
        />
        <Route
          path={routes.vishingRecentActivity.path}
          element={<RecentActivity channel="vishing" />}
        />
        {/* voiceServerConfiguration: page not present in module yet */}

        {/* ----------------------------------------------------------------- */}
        {/* Deepfake                                                          */}
        {/* ----------------------------------------------------------------- */}
        <Route
          path={routes.deepfakeDashboard.path}
          element={<PhishingDashboard />}
        />
        <Route
          path={routes.deepfakeCreate.path}
          element={<DeepfakeGenerator />}
        />
        <Route
          path={routes.deepfakeEdit.path}
          element={<DeepfakeGenerator />}
        />
        <Route
          path={routes.deepFakeContentLibrary.path}
          element={<ContentLibrary />}
        />
        <Route
          path={routes.deepfakeProviderConfiguration.path}
          element={<DeepfakeProviderConfiguration />}
        />

        {/* ----------------------------------------------------------------- */}
        {/* Administration                                                    */}
        {/* ----------------------------------------------------------------- */}
        <Route
          path={routes.aiProviderConfiguration.path}
          element={<AIProviderConfiguration />}
        />
        <Route
          path={routes.providerConfiguration.path}
          element={<ProviderConfiguration />}
        />
        <Route path={routes.configurations.path} element={<Configurations />} />

        {/* ----------------------------------------------------------------- */}
        {/* Breach Detection                                                  */}
        {/* ----------------------------------------------------------------- */}
        <Route
          path={routes.breachDashboard.path}
          element={<BreachDashboard />}
        />
        <Route
          path={routes.breachManagement.path}
          element={<BreachManagement />}
        />
        <Route
          path={routes.recipientBreaches.path}
          element={<RecipientBreaches />}
        />
        <Route path={routes.breachDetails.path} element={<BreachDashboard />} />

        <Route
          path={routes.phishingDashboard.path}
          element={<PhishingDashboard />}
        />
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
          element={<UserRiskReport />}
        />

        <Route
          path={routes.phishingReports.path}
          element={<PhishingReports />}
        />
        <Route
          path={routes.phishingRecentActivity.path}
          element={<RecentActivity />}
        />

        <Route
          path={routes.aiProviderConfiguration.path}
          element={<AIProviderConfiguration />}
        />
        <Route
          path={routes.providerConfiguration.path}
          element={<ProviderConfiguration />}
        />
        <Route
          path={routes.voiceServerConfiguration.path}
          element={<VoiceServerConfiguration />}
        />

        <Route path={routes.configurations.path} element={<Configurations />} />

        {/* ----------------------------------------------------------------- */}
        {/* Breach Monitor                                                    */}
        {/* ----------------------------------------------------------------- */}
        <Route
          path={routes.breachMonitorDashboard.path}
          element={<BreachMonitorDashboard />}
        />
        <Route
          path={routes.breachMonitorEmails.path}
          element={<EmailBreaches />}
        />
        <Route path={routes.breachMonitorIps.path} element={<IPBreaches />} />
        <Route
          path={routes.breachMonitorThreatIntel.path}
          element={<IntelBreaches />}
        />
        <Route
          path={routes.breachMonitorVulnerabilities.path}
          element={<BreachVulnerabilities />}
        />
        <Route
          path={routes.breachMonitorCompanyImpersonation.path}
          element={<CompanyImpersonation />}
        />
      </Route>
      <Route path={routes.login.path} element={<Login />} />
      <Route path={routes.twoFactorAuthSetup.path} element={<MFASetup />} />
      <Route path={routes.twoFactorAuthVerify.path} element={<MFAVerify />} />

      <Route path={routes.notFound.path} element={<NotFound />} />
    </Routes>
  );
};

export default AppRouter;
