import { Navigate, Route, Routes } from 'react-router-dom';

import BaseLayout from 'components/layout/BaseLayout';
import Login from 'pages/auth/Login';
import Dashboard from 'pages/Dashboard';
import NotFound from 'pages/NotFound';
import AccountRouter from 'routes/AccountRouter';
import { routes } from 'routes/AppRoutes';
import BillingRouter from 'routes/BillingRouter';
import BrandingRouter from 'routes/BrandingRouter';
import CertificateRouter from 'routes/CertificateRouter';
import ClientRouter from 'routes/ClientRouter';
import ContentRouter from 'routes/ContentRouter';
import DepartmentRouter from 'routes/DepartmentRouter';
import ExamRouter from 'routes/ExamRouter';
import FeatureRouter from 'routes/FeatureRouter';
import KnowledgeHubRouter from 'routes/KnowledgeHubRouter';
import LeaderboardRouter from 'routes/LeaderboardRouter';
import MenuRouter from 'routes/MenuRouter';
import MSPRouter from 'routes/MSPRouter';
import NewsRouter from 'routes/NewsRouter';
import NotificationRouter from 'routes/NotificationRouter';
import LicenseRouter from 'routes/LicenseRouter';
import PackageRouter from 'routes/PackageRouter';
import PolicyRouter from 'routes/PolicyRouter';
import ProductRouter from 'routes/ProductRouter';
import ReportRouter from 'routes/ReportRouter';
import RoleRouter from 'routes/RoleRouter';
import SettingsRouter from 'routes/SettingsRouter';
import SupportRouter from 'routes/SupportRouter';
import SurveyRouter from 'routes/SurveyRouter';
import UserRouter from 'routes/UserRouter';
import BreachMonitorRouter from 'routes/BreachMonitorRouter';
import AuthLayout from 'components/layout/AuthLayout';
import SelfClientAdminLayout from 'components/layout/SelfClientAdminLayout';
import ResetPassword from 'pages/auth/ResetPassword';
import MFASetup from 'pages/auth/MFASetup';
import MFAVerify from 'pages/auth/MFAVerify';
import RequestResetPassword from 'pages/auth/RequestResetPassword';
import SetPassword from 'pages/auth/SetPassword';
import ClientBuyProduct from 'pages/client/BuyProduct';
import ClientLicenseHistory from 'pages/client/ClientLicenseHistory';
import ClientSelfOnBoarding from 'pages/client/SelfOnboard';
import BookMarks from 'pages/content/BookMarks';
import Campaigns from 'pages/content/Campaigns';
import Certificates from 'pages/content/Certificates';
import ClientUserChangePassword from 'pages/content/ClientUserChangePassword';
import ClientUserProfile from 'pages/content/ClientUserProfile';
import ClientUserProfileSettings from 'pages/content/ClientUserProfileSettings';
import ContentDetails from 'pages/content/ContentDetails';
import CourseChapters from 'pages/content/CourseChapters';
import CourseComplete from 'pages/content/CourseComplete';
import CourseDetails from 'pages/content/CourseDetails';
import CourseList from 'pages/content/CourseList';
import PackageList from 'pages/content/PackageList';
import PhishingList from 'pages/content/PhishingList';
import ExamResults from 'pages/exam/ExamResults';
import KnowledgeHub from 'pages/knowledge/KnowledgeHub';
import MSPLicenseHistory from 'pages/msp/MSPLicenseHistory';
import ActivityLogs from 'pages/user/ActivityLogs';
import UserNotifications from 'pages/notification/UserNotifications';
import ClientUserPolicyList from 'pages/policy/ClientUserPolicyList';
import SupportTickets from 'pages/support/SupportTickets';
import UserActivityLogs from 'pages/user/UserActivityLogs';
import PhishingRouter from './PhishingRouter';
import TakeExam from 'pages/exam/TakeExam';
import SmishingRouter from './SmishingRouter';
import VishingRouter from './VishingRouter';
import DeepfakeRouter from './DeepfakeRouter';

const authChildPath = (path: string) => path.replace(/^\/auth\/?/, '');

const AppRouter = () => {
  return (
    <Routes>
      <Route path="/" element={<BaseLayout />}>
        <Route path={routes.dashboard.path} element={<Dashboard />} />
        <Route
          path={routes.accountManagement.path}
          element={<AccountRouter />}
        />
        <Route
          path={routes.billingManagement.path}
          element={<BillingRouter />}
        />
        <Route
          path={routes.brandingManagement.path}
          element={<BrandingRouter />}
        />
        <Route
          path={routes.certificateManagement.path}
          element={<CertificateRouter />}
        />
        <Route path={routes.clientManagement.path} element={<ClientRouter />} />
        <Route
          path={routes.contentManagement.path}
          element={<ContentRouter />}
        />
        <Route
          path={routes.departmentManagement.path}
          element={<DepartmentRouter />}
        />
        <Route path={routes.examManagement.path} element={<ExamRouter />} />
        <Route
          path={routes.featureManagement.path}
          element={<FeatureRouter />}
        />
        <Route
          path={routes.knowledgeHubManagement.path}
          element={<KnowledgeHubRouter />}
        />
        <Route
          path={routes.leaderboardManagement.path}
          element={<LeaderboardRouter />}
        />
        <Route path={routes.menuManagement.path} element={<MenuRouter />} />
        <Route path={routes.mspManagement.path} element={<MSPRouter />} />
        <Route path={routes.newsManagement.path} element={<NewsRouter />} />
        <Route
          path={routes.notificationManagement.path}
          element={<NotificationRouter />}
        />
        <Route
          path={routes.packageManagement.path}
          element={<PackageRouter />}
        />
        <Route
          path={routes.licenseManagement.path}
          element={<LicenseRouter />}
        />
        <Route path={routes.policyManagement.path} element={<PolicyRouter />} />
        <Route
          path={routes.phishingManagement.path}
          element={<PhishingRouter />}
        />
        <Route
          path={routes.smishingManagement.path}
          element={<SmishingRouter />}
        />
        <Route
          path={routes.vishingManagement.path}
          element={<VishingRouter />}
        />
        <Route
          path={routes.deepfakeManagement.path}
          element={<DeepfakeRouter />}
        />
        <Route
          path={routes.administrationManagement.path}
          element={<PhishingRouter />}
        />
        <Route
          path={routes.breachMonitor.path}
          element={<BreachMonitorRouter />}
        />
        <Route
          path={routes.productManagement.path}
          element={<ProductRouter />}
        />
        <Route path={routes.reportManagement.path} element={<ReportRouter />} />
        <Route path={routes.roleManagement.path} element={<RoleRouter />} />
        <Route path={routes.settings.path} element={<SettingsRouter />} />
        <Route
          path={routes.supportManagement.path}
          element={<SupportRouter />}
        />
        <Route path={routes.surveyManagement.path} element={<SurveyRouter />} />
        <Route path={routes.userManagement.path} element={<UserRouter />} />

        {/* End User */}
        <Route path={routes.account.path} element={<Navigate to="profile" />} />
        <Route
          path={routes.accountUserSecurity.path}
          element={<ClientUserChangePassword />}
        />
        <Route
          path={routes.accountProfile.path}
          element={<ClientUserProfile />}
        />
        <Route
          path={routes.accountProfileSettings.path}
          element={<ClientUserProfileSettings />}
        />
        <Route path={routes.bookmarks.path} element={<BookMarks />} />
        <Route path={routes.campaigns.path} element={<Campaigns />} />
        <Route path={routes.certificates.path} element={<Certificates />} />
        <Route path={routes.contentDetails.path} element={<ContentDetails />} />
        <Route path={routes.courseChapters.path} element={<CourseChapters />} />
        <Route path={routes.courseComplete.path} element={<CourseComplete />} />
        <Route path={routes.courseDetails.path} element={<CourseDetails />} />
        <Route path={routes.courseList.path} element={<CourseList />} />
        <Route
          path={routes.policyList.path}
          element={<ClientUserPolicyList />}
        />

        <Route path={routes.knowledgeHub.path} element={<KnowledgeHub />} />
        <Route path={routes.packageList.path} element={<PackageList />} />
        <Route path={routes.phishingList.path} element={<PhishingList />} />
        <Route
          path={routes.notification.path}
          element={<UserNotifications />}
        />
        <Route path={routes.supportTickets.path} element={<SupportTickets />} />
        <Route path={routes.activityLogs.path} element={<ActivityLogs />} />

        <Route
          path={routes.clientLicenseHistory.path}
          element={<ClientLicenseHistory />}
        />
        <Route
          path={routes.mspLicenseHistory.path}
          element={<MSPLicenseHistory />}
        />
        <Route path={routes.activityLogs.path} element={<UserActivityLogs />} />
        <Route
          path={routes.clientLicenseHistory.path}
          element={<ClientLicenseHistory />}
        />
        <Route
          path={routes.mspLicenseHistory.path}
          element={<MSPLicenseHistory />}
        />

        <Route path={routes.buyProduct.path} element={<ClientBuyProduct />} />

        <Route path={routes.exam.path} element={<TakeExam />} />
        <Route path={routes.examResult.path} element={<ExamResults />} />
      </Route>

      <Route path="/auth" element={<AuthLayout />}>
        <Route path={authChildPath(routes.login.path)} element={<Login />} />
        <Route
          path={authChildPath(routes.twoFactorAuthSetup.path)}
          element={<MFASetup />}
        />
        <Route
          path={authChildPath(routes.twoFactorAuthVerify.path)}
          element={<MFAVerify />}
        />
        <Route
          path={authChildPath(routes.resetPassword.path)}
          element={<ResetPassword />}
        />
        <Route
          path={authChildPath(routes.setPassword.path)}
          element={<SetPassword />}
        />
        <Route
          path={authChildPath(routes.requestResetPassword.path)}
          element={<RequestResetPassword />}
        />
      </Route>
      <Route
        path={routes.selfOnboard.path}
        element={
          <SelfClientAdminLayout>
            <ClientSelfOnBoarding />
          </SelfClientAdminLayout>
        }
      />

      <Route path="*" element={<NotFound />} />
    </Routes>
  );
};

export default AppRouter;
