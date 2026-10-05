// import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

// const RemoteNotificationAnalytics = lazy(
//   () => import('miscellaneous-module/NotificationAnalytics'),
// );

const NotificationAnalytics = () => {
  return (
    <ErrorBoundaryWrapper>
      {/* <RemoteNotificationAnalytics /> */}
      <div>Coming Soon</div>
    </ErrorBoundaryWrapper>
  );
};

export default NotificationAnalytics;
