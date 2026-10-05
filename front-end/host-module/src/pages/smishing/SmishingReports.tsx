import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteSmishingReports = lazy(
  () => import('phishing-module/SmishingReports'),
);

const SmishingReports = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteSmishingReports />
    </ErrorBoundaryWrapper>
  );
};

export default SmishingReports;
