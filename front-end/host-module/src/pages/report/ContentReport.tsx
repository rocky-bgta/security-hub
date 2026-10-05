import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteContentReport = lazy(
  () => import('miscellaneous-module/ContentReport'),
);

const ContentReport = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteContentReport />
    </ErrorBoundaryWrapper>
  );
};

export default ContentReport;
