import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemotePackageLicenseHistory = lazy(
  () => import('content-module/PackageLicense'),
);

const PackageLicenseHistory = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemotePackageLicenseHistory />
    </ErrorBoundaryWrapper>
  );
};

export default PackageLicenseHistory;
