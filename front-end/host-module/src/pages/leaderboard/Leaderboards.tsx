import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteLeaderboards = lazy(
  () => import('miscellaneous-module/Leaderboard'),
);

const Leaderboards = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteLeaderboards />
    </ErrorBoundaryWrapper>
  );
};

export default Leaderboards;
