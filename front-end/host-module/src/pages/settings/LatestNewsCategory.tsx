import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteLatestNewsCategory = lazy(
  () => import('miscellaneous-module/LatestNewsCategory'),
);

const LatestNewsCategory = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteLatestNewsCategory />
    </ErrorBoundaryWrapper>
  );
};

export default LatestNewsCategory;
