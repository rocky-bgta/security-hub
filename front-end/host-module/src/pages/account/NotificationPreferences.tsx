// import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

// const RemoteNotificationPreferences = lazy(
//   () => import('account-module/NotificationPreferences'),
// );

const NotificationPreferences = () => {
  return (
    <ErrorBoundaryWrapper>
      {/* <RemoteNotificationPreferences /> */}
      <div>Coming Soon</div>
    </ErrorBoundaryWrapper>
  );
};

export default NotificationPreferences;
