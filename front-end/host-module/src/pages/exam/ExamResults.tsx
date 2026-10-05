import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';
import { ContentManagementRoutes } from 'routes/ContentManagementRoutes';

const RemoteExamResults = lazy(() => import('content-module/ExamResults'));

const ExamResults = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteExamResults hostPath={ContentManagementRoutes} />
    </ErrorBoundaryWrapper>
  );
};

export default ExamResults;
