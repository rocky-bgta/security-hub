import { Route, Routes } from 'react-router-dom';

import PackageManagement from 'pages/package';
import AddPackage from 'pages/package/AddPackage';
import AssignedPackages from 'pages/package/AssignedPackages';
import AvailablePackages from 'pages/package/AvailablePackages';
import PackageLicenseHistory from 'pages/package/PackageLicenseHistory';
import PerformanceReport from 'pages/package/PerformanceReport';
import { routes } from 'routes/AppRoutes';
import SubPackages from 'pages/package/SubPackages';

const PackageRouter = () => {
  return (
    <Routes>
      <Route index element={<PackageManagement />} />

      <Route
        path={routes.assignedPackages.path}
        element={<AssignedPackages />}
      />
      <Route
        path={routes.availablePackages.path}
        element={<AvailablePackages />}
      />
      <Route path={routes.subPackages.path} element={<SubPackages />} />
      <Route
        path={routes.packageLicenseHistory.path}
        element={<PackageLicenseHistory />}
      />
      <Route
        path={routes.packageReports.path}
        element={<PerformanceReport />}
      />

      <Route path={routes.addPackage.path} element={<AddPackage />} />
    </Routes>
  );
};

export default PackageRouter;
