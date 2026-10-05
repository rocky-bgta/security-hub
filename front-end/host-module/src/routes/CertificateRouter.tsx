import { Route, Routes } from 'react-router-dom';

import CertificateManagement from 'pages/certificate';
import CertificateAnalytics from 'pages/certificate/CertificateAnalytics';
import CertificateHistory from 'pages/certificate/CertificateHistory';
import CertificateIssued from 'pages/certificate/CertificateIssued';
import CertificateTemplates from 'pages/certificate/CertificateTemplates';
import { routes } from 'routes/AppRoutes';

const CertificateRouter = () => {
  return (
    <Routes>
      <Route index element={<CertificateManagement />} />

      <Route
        path={routes.certificateTemplate.path}
        element={<CertificateTemplates />}
      />
      <Route
        path={routes.certificateIssued.path}
        element={<CertificateIssued />}
      />
      <Route
        path={routes.certificateAnalytics.path}
        element={<CertificateAnalytics />}
      />

      <Route
        path={routes.certificateHistory.path}
        element={<CertificateHistory />}
      />
    </Routes>
  );
};

export default CertificateRouter;
