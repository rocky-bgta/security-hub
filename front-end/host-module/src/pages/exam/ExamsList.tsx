import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteExamsList = lazy(() => import('content-module/ExamsList'));

const ExamsList = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteExamsList />
    </ErrorBoundaryWrapper>
  );
};

export default ExamsList;
