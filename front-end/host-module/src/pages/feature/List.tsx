import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteFeatureList = lazy(() => import('content-module/FeatureList'));

const FeatureList = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteFeatureList />
    </ErrorBoundaryWrapper>
  );
};

export default FeatureList;
