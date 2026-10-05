import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteMSPManageLicenses = lazy(
  () => import('user-module/MSPManageLicenses'),
);

const MSPManageLicenses = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteMSPManageLicenses />
    </ErrorBoundaryWrapper>
  );
};

export default MSPManageLicenses;
