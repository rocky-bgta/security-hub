import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteUserRanges = lazy(() => import('miscellaneous-module/UserRanges'));

const UserRanges = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteUserRanges />
    </ErrorBoundaryWrapper>
  );
};

export default UserRanges;
