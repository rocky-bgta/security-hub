// import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

// const RemoteAddPackage = lazy(
//   () => import('content-module/AddPackage'),
// );

const AddPackage = () => {
  return (
    <ErrorBoundaryWrapper>
      {/* <RemoteAddPackage /> */}
      <div>Coming Soon</div>
    </ErrorBoundaryWrapper>
  );
};

export default AddPackage;
