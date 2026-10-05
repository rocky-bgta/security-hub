import { Route, Routes } from 'react-router-dom';

import ReportManagement from 'pages/report';
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
import { routes } from 'routes/AppRoutes';

const ReportRouter = () => {
  return (
    <Routes>
      <Route index element={<ReportManagement />} />

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
    </Routes>
  );
};

export default ReportRouter;
