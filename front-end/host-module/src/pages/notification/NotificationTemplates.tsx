import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteNotificationTemplates = lazy(
  () => import('account-module/NotificationTemplates'),
);

const NotificationTemplates = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteNotificationTemplates />
    </ErrorBoundaryWrapper>
  );
};

export default NotificationTemplates;
