import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteTierList = lazy(() => import('billing-module/TierList'));

const TierList = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteTierList />
    </ErrorBoundaryWrapper>
  );
};

export default TierList;
