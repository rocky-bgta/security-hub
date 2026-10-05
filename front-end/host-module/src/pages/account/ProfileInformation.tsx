// import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

// const RemoteProfileInformation = lazy(
//   () => import('account-module/ProfileInformation'),
// );

const ProfileInformation = () => {
  return (
    <ErrorBoundaryWrapper>
      {/* <RemoteProfileInformation /> */}
      <div>Coming Soon</div>
    </ErrorBoundaryWrapper>
  );
};

export default ProfileInformation;
