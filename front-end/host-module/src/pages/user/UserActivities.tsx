// import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

// const RemoteUserActivities = lazy(() => import('user-module/userActivities'));

const UserActivities = () => {
  return (
    <ErrorBoundaryWrapper>
      {/* <RemoteUserActivities /> */}
      <div>Coming Soon</div>
    </ErrorBoundaryWrapper>
  );
};

export default UserActivities;
