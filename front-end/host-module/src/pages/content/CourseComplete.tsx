import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';
import { ContentManagementRoutes } from 'routes/ContentManagementRoutes';

const RemoteCourseComplete = lazy(
  () => import('content-module/CourseComplete'),
);

const CourseComplete = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteCourseComplete hostPath={ContentManagementRoutes} />
    </ErrorBoundaryWrapper>
  );
};

export default CourseComplete;
