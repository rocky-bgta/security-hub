import { Route, Routes } from 'react-router-dom';

import CampaignRiskImpacts from 'pages/phishing/CampaignRiskImpacts';
import CampaignReports from 'pages/phishing/CampaignReports';
import CampaignDetailReport from 'pages/phishing/CampaignDetailReport';
import CourseStatistics from 'pages/phishing/CourseStatistics';
import RecentActivity from 'pages/phishing/RecentActivity';
import UserPhishingRiskReport from 'pages/phishing/UserPhishingRiskReport';
import CreateSmishingSimulation from 'pages/smishing/CreateSmishingSimulation';
import SmsCampaignDetails from 'pages/smishing/SmsCampaignDetails';
import SmsCampaignList from 'pages/smishing/SmsCampaignList';
import SmsServerConfigurationList from 'pages/smishing/SmsServerConfigurationList';
import SmsTemplateCreate from 'pages/smishing/SmsTemplateCreate';
import SmsTemplateLibrary from 'pages/smishing/SmsTemplateLibrary';
import LandingPageCreate from 'pages/smishing/LandingPageCreate';
import LandingPageLibrary from 'pages/smishing/LandingPageLibrary';
import SmishingDashboard from 'pages/smishing/SmishingDashboard';
import SmishingManagement from 'pages/smishing';
import SmishingReports from 'pages/smishing/SmishingReports';
import { routes } from 'routes/AppRoutes';

const SmishingRouter = () => {
  return (
    <Routes>
      <Route index element={<SmishingManagement />} />
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
        element={<CreateSmishingSimulation />}
      />
      <Route
        path={routes.smishingSimulationEdit.path}
        element={<CreateSmishingSimulation />}
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
      <Route path={routes.smishingReports.path} element={<SmishingReports />} />
      <Route
        path={routes.smishingCampaignRiskImpacts.path}
        element={<CampaignRiskImpacts channel="smishing" />}
      />
      <Route
        path={routes.smishingCourseStatisticsDetails.path}
        element={<CourseStatistics channel="smishing" />}
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
        element={<UserPhishingRiskReport channel="smishing" />}
      />
      <Route
        path={routes.smishingRecentActivity.path}
        element={<RecentActivity channel="smishing" />}
      />
    </Routes>
  );
};

export default SmishingRouter;
