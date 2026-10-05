// import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

// const RemoteNotificationScheduler = lazy(
//   () => import('miscellaneous-module/NotificationScheduler'),
// );

const NotificationScheduler = () => {
  return (
    <ErrorBoundaryWrapper>
      {/* <RemoteNotificationScheduler /> */}
      <div>Coming Soon</div>
    </ErrorBoundaryWrapper>
  );
};

export default NotificationScheduler;
