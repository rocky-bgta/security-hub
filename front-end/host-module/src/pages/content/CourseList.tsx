import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';
import { ClientUserRoutes } from 'routes/ClientUserRoutes';

const RemoteCourseList = lazy(() => import('content-module/CourseList'));

const CourseList = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteCourseList hostPath={ClientUserRoutes} />
    </ErrorBoundaryWrapper>
  );
};

export default CourseList;
