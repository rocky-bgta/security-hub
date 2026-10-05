import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteMSPLicenseHistory = lazy(
  () => import('content-module/MSPLicenseHistory'),
);

const MSPLicenseHistory = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteMSPLicenseHistory />
    </ErrorBoundaryWrapper>
  );
};

export default MSPLicenseHistory;
