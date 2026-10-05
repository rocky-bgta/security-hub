// import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

// const RemoteManageNotifications = lazy(
//   () => import('miscellaneous-module/ManageNotifications'),
// );

const ManageNotifications = () => {
  return (
    <ErrorBoundaryWrapper>
      {/* <RemoteManageNotifications /> */}
      <div>Coming Soon</div>
    </ErrorBoundaryWrapper>
  );
};

export default ManageNotifications;
