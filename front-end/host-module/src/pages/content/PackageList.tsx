import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';
import { ContentManagementRoutes } from 'routes/ContentManagementRoutes';

const RemotePackageList = lazy(() => import('content-module/PackageList'));

const PackageList = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemotePackageList hostPath={ContentManagementRoutes} />
    </ErrorBoundaryWrapper>
  );
};

export default PackageList;
