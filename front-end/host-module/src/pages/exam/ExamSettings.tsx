import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteExamSettings = lazy(() => import('content-module/ExamSettings'));

const ExamSettings = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteExamSettings />
    </ErrorBoundaryWrapper>
  );
};

export default ExamSettings;
