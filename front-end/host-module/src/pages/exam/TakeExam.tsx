import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteTakeExam = lazy(() => import('content-module/TakeExam'));

const TakeExam = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteTakeExam />
    </ErrorBoundaryWrapper>
  );
};

export default TakeExam;
