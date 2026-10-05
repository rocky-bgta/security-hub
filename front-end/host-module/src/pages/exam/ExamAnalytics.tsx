import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteExamAnalytics = lazy(() => import('content-module/ExamAnalytics'));

const ExamAnalytics = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteExamAnalytics />
    </ErrorBoundaryWrapper>
  );
};

export default ExamAnalytics;
