// import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

// const RemoteArchivedNews = lazy(
//   () => import('miscellaneous-module/ArchivedNews'),
// );

const ArchivedNews = () => {
  return (
    <ErrorBoundaryWrapper>
      {/* <RemoteArchivedNews /> */}
      <div>Coming Soon</div>
    </ErrorBoundaryWrapper>
  );
};

export default ArchivedNews;
