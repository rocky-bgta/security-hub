// import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

// const RemoteLeaderList = lazy(
//   () => import('miscellaneous-module/LeaderList'),
// );

const LeaderList = () => {
  return (
    <ErrorBoundaryWrapper>
      {/* <RemoteLeaderList /> */}
      <div>Coming Soon</div>
    </ErrorBoundaryWrapper>
  );
};

export default LeaderList;
