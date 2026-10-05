// import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

// const RemoteCreateNews = lazy(
//   () => import('miscellaneous-module/CreateNews'),
// );

const CreateNews = () => {
  return (
    <ErrorBoundaryWrapper>
      {/* <RemoteCreateNews /> */}
      <div>Coming Soon</div>
    </ErrorBoundaryWrapper>
  );
};

export default CreateNews;
