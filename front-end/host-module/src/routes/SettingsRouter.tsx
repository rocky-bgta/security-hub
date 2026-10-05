import { Navigate, Route, Routes } from 'react-router-dom';

import CouponList from 'pages/settings/Coupon';
import CreditList from 'pages/settings/CreditList';
import TierList from 'pages/settings/TierList';
import VatList from 'pages/settings/VatList';
import { routes } from 'routes/AppRoutes';
import Compliance from 'pages/settings/Compliance';
import ContentType from 'pages/settings/ContentType';
import Tags from 'pages/settings/Tags';
import CountryList from 'pages/settings/CountryList';
import CategoryList from 'pages/settings/CategoryList';
import Industries from 'pages/settings/Industries';
import SubIndustries from 'pages/settings/SubIndustries';
import OrganizationSizes from 'pages/settings/OrganizationSizes';
import Language from 'pages/settings/Language';
import States from 'pages/settings/States';
import TimeZone from 'pages/settings/TimeZone';
import OrganizationTypes from 'pages/settings/OrganizationTypes';
import BillingActions from 'pages/settings/BillingActions';
import BillingNextStep from 'pages/settings/BillingNextStep';
import DepartmentList from 'pages/settings/DepartmentList';
import PolicyType from 'pages/settings/PolicyType';
import LatestNewsCategory from 'pages/settings/LatestNewsCategory';
import SupportTicketType from 'pages/settings/SupportTicketType';
import UserRanges from 'pages/settings/UserRanges';
import MSPType from 'pages/settings/MSPType';
import CreditReason from 'pages/settings/CreditReason';
import NetTerm from 'pages/settings/NetTerm';
import SuspendReason from 'pages/settings/SuspendReason';

const SettingsRouter = () => {
  return (
    <Routes>
      <Route index element={<Navigate to="manage-coupon" />} />

      <Route path={routes.manageCoupon.path} element={<CouponList />} />
      <Route path={routes.vatConfigurations.path} element={<VatList />} />
      <Route path={routes.manageCredits.path} element={<CreditList />} />
      <Route path={routes.tierConfigurations.path} element={<TierList />} />
      <Route path={routes.manageCompliance.path} element={<Compliance />} />
      <Route path={routes.contentTypes.path} element={<ContentType />} />
      <Route path={routes.tags.path} element={<Tags />} />
      <Route path={routes.country.path} element={<CountryList />} />
      <Route path={routes.category.path} element={<CategoryList />} />
      <Route path={routes.industries.path} element={<Industries />} />
      <Route path={routes.subIndustries.path} element={<SubIndustries />} />
      <Route
        path={routes.organizationSizes.path}
        element={<OrganizationSizes />}
      />
      <Route
        path={routes.organizationTypes.path}
        element={<OrganizationTypes />}
      />
      <Route path={routes.timeZones.path} element={<TimeZone />} />
      <Route path={routes.states.path} element={<States />} />
      <Route path={routes.languages.path} element={<Language />} />
      <Route path={routes.billingActions.path} element={<BillingActions />} />
      <Route path={routes.billingNextStep.path} element={<BillingNextStep />} />
      <Route path={routes.departmentList.path} element={<DepartmentList />} />
      <Route path={routes.policyType.path} element={<PolicyType />} />
      <Route
        path={routes.latestNewsCategory.path}
        element={<LatestNewsCategory />}
      />
      <Route
        path={routes.supportTicketType.path}
        element={<SupportTicketType />}
      />
      <Route path={routes.userRanges.path} element={<UserRanges />} />
      <Route path={routes.mspType.path} element={<MSPType />} />
      <Route path={routes.creditReason.path} element={<CreditReason />} />
      <Route path={routes.netTerm.path} element={<NetTerm />} />
      <Route path={routes.suspendReason.path} element={<SuspendReason />} />
    </Routes>
  );
};

export default SettingsRouter;
