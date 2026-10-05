import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteResourceAnalytics = lazy(
  () => import('miscellaneous-module/ResourceAnalytics'),
);

const ResourceAnalytics = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteResourceAnalytics />
    </ErrorBoundaryWrapper>
  );
};

export default ResourceAnalytics;
