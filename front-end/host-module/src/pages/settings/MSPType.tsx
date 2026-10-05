import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteMSPType = lazy(() => import('miscellaneous-module/MSPType'));

const MSPType = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteMSPType />
    </ErrorBoundaryWrapper>
  );
};

export default MSPType;
