import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';
import { ContentManagementRoutes } from 'routes/ContentManagementRoutes';

const RemoteContentCourseHeader = lazy(
  () => import('content-module/CourseHeader'),
);

const ContentCourseHeader = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteContentCourseHeader hostPath={ContentManagementRoutes} />
    </ErrorBoundaryWrapper>
  );
};

export default ContentCourseHeader;
