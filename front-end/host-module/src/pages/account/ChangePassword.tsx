// import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

// const RemoteChangePassword = lazy(
//   () => import('account-module/ChangePassword'),
// );

const ChangePassword = () => {
  return (
    <ErrorBoundaryWrapper>
      {/* <RemoteChangePassword /> */}
      <div>Coming Soon</div>
    </ErrorBoundaryWrapper>
  );
};

export default ChangePassword;
