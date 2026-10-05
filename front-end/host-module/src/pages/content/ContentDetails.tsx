import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteContentDetails = lazy(
  () => import('content-module/ContentDetails'),
);

const ContentDetails = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteContentDetails />
    </ErrorBoundaryWrapper>
  );
};

export default ContentDetails;
