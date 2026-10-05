import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteExamineeReports = lazy(
  () => import('content-module/ExamineeReports'),
);

const ExamineeReports = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteExamineeReports />
    </ErrorBoundaryWrapper>
  );
};

export default ExamineeReports;
