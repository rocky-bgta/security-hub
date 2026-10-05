import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemotePerformanceReport = lazy(
  () => import('content-module/PerformanceReport'),
);

const PerformanceReport = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemotePerformanceReport />
    </ErrorBoundaryWrapper>
  );
};

export default PerformanceReport;
