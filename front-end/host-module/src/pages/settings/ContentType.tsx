import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteContentType = lazy(
  () => import('miscellaneous-module/ContentType'),
);

const ContentType = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteContentType />
    </ErrorBoundaryWrapper>
  );
};

export default ContentType;
