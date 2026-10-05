import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteNotificationSettings = lazy(
  () => import('account-module/NotificationSettings'),
);

const NotificationSettings = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteNotificationSettings />
    </ErrorBoundaryWrapper>
  );
};

export default NotificationSettings;
