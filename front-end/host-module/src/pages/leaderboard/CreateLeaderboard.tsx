// import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

// const RemoteCreateLeaderboard = lazy(
//   () => import('miscellaneous-module/CreateLeaderboard'),
// );

const CreateLeaderboard = () => {
  return (
    <ErrorBoundaryWrapper>
      {/* <RemoteCreateLeaderboard /> */}
      <div>Coming Soon</div>
    </ErrorBoundaryWrapper>
  );
};

export default CreateLeaderboard;
