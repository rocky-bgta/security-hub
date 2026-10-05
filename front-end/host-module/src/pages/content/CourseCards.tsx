import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';
import { ContentManagementRoutes } from 'routes/ContentManagementRoutes';

const RemoteCourseCards = lazy(() => import('content-module/CourseCards'));

const CourseCards = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteCourseCards hostPath={ContentManagementRoutes} />
    </ErrorBoundaryWrapper>
  );
};

export default CourseCards;
