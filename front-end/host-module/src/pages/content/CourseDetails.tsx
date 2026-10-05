import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';
import { useAuth } from 'hooks/UseAuth';
import { ClientUserRoutes } from 'routes/ClientUserRoutes';
import { ContentManagementRoutes } from 'routes/ContentManagementRoutes';
import { ROLE } from 'utils/Role';

const RemoteCourseDetails = lazy(() => import('content-module/CourseDetails'));

const CourseDetails = () => {
  const { role } = useAuth();
  const hostPath =
    role === ROLE.SUPER_ADMIN ? ContentManagementRoutes : ClientUserRoutes;

  return (
    <ErrorBoundaryWrapper>
      <RemoteCourseDetails hostPath={hostPath} />
    </ErrorBoundaryWrapper>
  );
};

export default CourseDetails;
