import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteMSPManageLicense = lazy(
  () => import('billing-module/MSPManageLicense'),
);

const MSPManageLicense = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteMSPManageLicense />
    </ErrorBoundaryWrapper>
  );
};

export default MSPManageLicense;
