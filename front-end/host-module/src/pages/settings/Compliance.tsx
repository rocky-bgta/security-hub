import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteCompliance = lazy(() => import('miscellaneous-module/Compliance'));

const Compliance = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteCompliance />
    </ErrorBoundaryWrapper>
  );
};

export default Compliance;
