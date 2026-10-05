// import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

// const RemoteBrandingList = lazy(
//   () => import('miscellaneous-module/BrandingList'),
// );

const BrandingList = () => {
  return (
    <ErrorBoundaryWrapper>
      {/* <RemoteBrandingList /> */}
      <div>Coming Soon</div>
    </ErrorBoundaryWrapper>
  );
};

export default BrandingList;
