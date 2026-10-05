import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteExamLibrary = lazy(() => import('content-module/ExamLibrary'));

const ExamLibrary = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteExamLibrary />
    </ErrorBoundaryWrapper>
  );
};

export default ExamLibrary;
