import { Route, Routes } from 'react-router-dom';

import AttackTemplateLibrary from 'pages/vishing/AttackTemplateLibrary';
import CampaignRiskImpacts from 'pages/phishing/CampaignRiskImpacts';
import CampaignReports from 'pages/phishing/CampaignReports';
import CampaignDetailReport from 'pages/phishing/CampaignDetailReport';
import CourseStatistics from 'pages/phishing/CourseStatistics';
import RecentActivity from 'pages/phishing/RecentActivity';
import UserPhishingRiskReport from 'pages/phishing/UserPhishingRiskReport';
import CreateVishingSimulation from 'pages/vishing/CreateVishingSimulation';
import PhishingReports from 'pages/phishing/PhishingReports';
import TemplateLibrary from 'pages/phishing/TemplateLibrary';
import VishingCampaignDetails from 'pages/vishing/VishingCampaignDetails';
import VishingCampaignList from 'pages/vishing/VishingCampaignList';
import VishingDashboard from 'pages/vishing/VishingDashboard';
import VishingManagement from 'pages/vishing';
import VishingProviderConfiguration from 'pages/vishing/VishingProviderConfiguration';
import VoiceServerConfiguration from 'pages/vishing/VoiceServerConfiguration';
import { routes } from 'routes/AppRoutes';

const VishingRouter = () => {
  return (
    <Routes>
      <Route index element={<VishingManagement />} />
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
        element={<CreateVishingSimulation />}
      />
      <Route
        path={routes.vishingSimulationEdit.path}
        element={<CreateVishingSimulation />}
      />
      <Route
        path={routes.vishingTemplateLibrary.path}
        element={<TemplateLibrary />}
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
        path={routes.voiceServerConfiguration.path}
        element={<VoiceServerConfiguration />}
      />
      <Route
        path={routes.vishingCourseStatisticsDetails.path}
        element={<CourseStatistics channel="vishing" />}
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
        element={<UserPhishingRiskReport channel="vishing" />}
      />
      <Route
        path={routes.vishingRecentActivity.path}
        element={<RecentActivity channel="vishing" />}
      />
    </Routes>
  );
};

export default VishingRouter;
