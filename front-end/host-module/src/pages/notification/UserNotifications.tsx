import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteNotifications = lazy(
  () => import('content-module/UserNotification'),
);

const UserNotifications = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteNotifications />
    </ErrorBoundaryWrapper>
  );
};

export default UserNotifications;
