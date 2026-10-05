// import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

// const RemoteNotifications = lazy(
//   () => import('miscellaneous-module/Notifications'),
// );

const Notifications = () => {
  return (
    <ErrorBoundaryWrapper>
      {/* <RemoteNotifications /> */}
      <div>Coming Soon</div>
    </ErrorBoundaryWrapper>
  );
};

export default Notifications;
