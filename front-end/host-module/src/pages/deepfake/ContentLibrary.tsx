import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteContentLibrary = lazy(
  () => import('phishing-module/ContentLibrary'),
);

const ContentLibrary = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteContentLibrary />
    </ErrorBoundaryWrapper>
  );
};

export default ContentLibrary;
