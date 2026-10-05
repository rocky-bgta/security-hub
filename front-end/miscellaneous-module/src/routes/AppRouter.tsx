import { Route, Routes } from 'react-router-dom';

import BaseLayout from 'components/layouts/BaseLayout';
import LatestNews from 'pages/aspire-admin/LatestNews';
import PollSurvey from 'pages/aspire-admin/PollSurvey';
import TicketLibrary from 'pages/aspire-admin/TicketLibrary';
import BillingActions from 'pages/BillingActions';
import BillingNextStep from 'pages/BillingNextStep';
import Category from 'pages/Category';
import Compliance from 'pages/Compliance';
import ContentType from 'pages/ContentType';
import Country from 'pages/Country';
import CreditReason from 'pages/CreditReason';
import DepartmentList from 'pages/DepartmentList';
import Industries from 'pages/Industries';
import SubIndustries from 'pages/SubIndustries';
import ManageResources from 'pages/knowledge-hub/ManageResources';
import ResourceAnalytics from 'pages/knowledge-hub/ResourceAnalytics';
import ResourceCategories from 'pages/knowledge-hub/ResourceCategories';
import KnowledgeHub from 'pages/KnowledgeHub';
import Languages from 'pages/Languages';
import LatestNewsCategory from 'pages/LatestNewsCategory';
import Leaderboard from 'pages/Leaderboard';
import Login from 'pages/Login';
import Menu from 'pages/Menu';
import MFASetup from 'pages/MFASetup';
import MFAVerify from 'pages/MFAVerify';
import MSPType from 'pages/MSPType';
import NetTerm from 'pages/NetTerm';
import NotFound from 'pages/NotFound';
import OrganizationSizes from 'pages/OrganizationSizes';
import OrganizationTypes from 'pages/OrganizationTypes';
import Permissions from 'pages/Permissions';
import AssignedPolicy from 'pages/policy/AssignedPolicy';
import PolicyList from 'pages/policy/PolicyList';
import RequestedPolicy from 'pages/policy/RequestedPolicy';
import RequestNewPolicy from 'pages/policy/RequestNewPolicy';
import PolicyType from 'pages/PolicyType';
import AccessReport from 'pages/report/AccessReport';
import CertificateReport from 'pages/report/CertificateReport';
import ContentReport from 'pages/report/ContentReport';
import LicenseReport from 'pages/report/LicenseReport';
import PerformanceReport from 'pages/report/PerformanceReport';
import PhishingContentReport from 'pages/report/PhishingContentReport';
import ProductReport from 'pages/report/ProductReport';
import UserActivityReport from 'pages/report/UserActivityReport';
import BillingPaymentReport from 'pages/report/BillingPaymentReport';
import SupportTicketReport from 'pages/report/SupportTicketReport';
import UserReport from 'pages/report/UserReport';
import UserRiskReport from 'pages/report/UserRiskReport';
import Roles from 'pages/Roles';
import States from 'pages/States';
import SuspendReason from 'pages/SuspendReason';
import Tags from 'pages/Tags';
import PendingTickets from 'pages/support-ticket/PendingTickets';
import ResolveTicket from 'pages/support-ticket/ResolvedTicket';
import SupportTickets from 'pages/support-ticket/SupportTickets';
import SupportTicketType from 'pages/SupportTicketType';
import TimeZone from 'pages/TimeZone';
import UserRanges from 'pages/UserRanges';
import { routes } from 'routes/Routes';

const AppRouter = () => {
  return (
    <Routes>
      <Route path="/" element={<BaseLayout />}>
        <Route path={routes.roleList.path} element={<Roles />} />
        <Route path={routes.permissionList.path} element={<Permissions />} />
        <Route path={routes.menuList.path} element={<Menu />} />

        <Route path={routes.manageCompliance.path} element={<Compliance />} />
        <Route path={routes.contentTypes.path} element={<ContentType />} />
        <Route path={routes.tags.path} element={<Tags />} />
        <Route path={routes.country.path} element={<Country />} />
        <Route path={routes.category.path} element={<Category />} />
        <Route
          path={routes.organizationSizes.path}
          element={<OrganizationSizes />}
        />
        <Route
          path={routes.organizationTypes.path}
          element={<OrganizationTypes />}
        />
        <Route path={routes.policyType.path} element={<PolicyType />} />
        <Route
          path={routes.LatestNewsCategory.path}
          element={<LatestNewsCategory />}
        />
        <Route path={routes.industries.path} element={<Industries />} />
        <Route path={routes.subIndustries.path} element={<SubIndustries />} />
        <Route path={routes.languages.path} element={<Languages />} />
        <Route path={routes.states.path} element={<States />} />
        <Route path={routes.timeZones.path} element={<TimeZone />} />

        <Route path={routes.userReport.path} element={<UserReport />} />
        <Route
          path={routes.supportTicketReport.path}
          element={<SupportTicketReport />}
        />
        <Route
          path={routes.billingPaymentReport.path}
          element={<BillingPaymentReport />}
        />
        <Route path={routes.accessReport.path} element={<AccessReport />} />
        <Route path={routes.productReport.path} element={<ProductReport />} />
        <Route path={routes.contentReport.path} element={<ContentReport />} />
        <Route path={routes.licenseReport.path} element={<LicenseReport />} />
        <Route path={routes.userRiskReport.path} element={<UserRiskReport />} />
        <Route
          path={routes.certificateReport.path}
          element={<CertificateReport />}
        />
        <Route
          path={routes.userActivityReport.path}
          element={<UserActivityReport />}
        />
        <Route
          path={routes.phishingContentReport.path}
          element={<PhishingContentReport />}
        />
        <Route
          path={routes.performanceReport.path}
          element={<PerformanceReport />}
        />

        <Route path={routes.policyList.path} element={<PolicyList />} />
        <Route
          path={routes.requestPolicy.path}
          element={<RequestNewPolicy />}
        />
        <Route
          path={routes.assignedPolicies.path}
          element={<AssignedPolicy />}
        />
        <Route
          path={routes.requestedPolicies.path}
          element={<RequestedPolicy />}
        />

        <Route path={routes.supportTickets.path} element={<SupportTickets />} />
        <Route path={routes.knowledgeHub.path} element={<KnowledgeHub />} />
        <Route
          path={routes.manageResources.path}
          element={<ManageResources />}
        />
        <Route
          path={routes.resourceAnalytics.path}
          element={<ResourceAnalytics />}
        />
        <Route
          path={routes.resourceCategories.path}
          element={<ResourceCategories />}
        />
        <Route path={routes.leaderboard.path} element={<Leaderboard />} />

        <Route path={routes.ticketLibrary.path} element={<TicketLibrary />} />
        <Route path={routes.pendingTickets.path} element={<PendingTickets />} />
        <Route path={routes.resolveTicket.path} element={<ResolveTicket />} />
        <Route path={routes.pollSurvey.path} element={<PollSurvey />} />
        <Route path={routes.latestNews.path} element={<LatestNews />} />

        <Route path={routes.billingActions.path} element={<BillingActions />} />
        <Route
          path={routes.billingNextStep.path}
          element={<BillingNextStep />}
        />
        <Route path={routes.departmentList.path} element={<DepartmentList />} />
        <Route
          path={routes.supportTicketType.path}
          element={<SupportTicketType />}
        />
        <Route path={routes.userRanges.path} element={<UserRanges />} />
        <Route path={routes.mspType.path} element={<MSPType />} />
        <Route path={routes.creditReason.path} element={<CreditReason />} />
        <Route path={routes.netTerm.path} element={<NetTerm />} />
        <Route
          path={routes.suspendReason.path}
          element={<SuspendReason />}
        />
      </Route>

      <Route path={routes.login.path} element={<Login />} />
      <Route path={routes.twoFactorAuthSetup.path} element={<MFASetup />} />
      <Route path={routes.twoFactorAuthVerify.path} element={<MFAVerify />} />
      <Route path="*" element={<NotFound />} />
    </Routes>
  );
};

export default AppRouter;
