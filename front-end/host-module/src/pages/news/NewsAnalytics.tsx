// import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

// const RemoteNewsAnalytics = lazy(
//   () => import('miscellaneous-module/NewsAnalytics'),
// );

const NewsAnalytics = () => {
  return (
    <ErrorBoundaryWrapper>
      {/* <RemoteNewsAnalytics /> */}
      <div>Coming Soon</div>
    </ErrorBoundaryWrapper>
  );
};

export default NewsAnalytics;
