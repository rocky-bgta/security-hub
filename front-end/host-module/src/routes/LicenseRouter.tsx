import { Route, Routes } from 'react-router-dom';

import LicenseManagement from 'pages/license';
import AssignedPackages from 'pages/package/AssignedPackages';
import PackageLicenseHistory from 'pages/package/PackageLicenseHistory';
import { routes } from 'routes/AppRoutes';

const LicenseRouter = () => {
  return (
    <Routes>
      <Route index element={<LicenseManagement />} />
      <Route
        path={routes.licenseOverview.path}
        element={<AssignedPackages />}
      />
      <Route
        path={routes.licenseAssignment.path}
        element={<PackageLicenseHistory />}
      />
    </Routes>
  );
};

export default LicenseRouter;
