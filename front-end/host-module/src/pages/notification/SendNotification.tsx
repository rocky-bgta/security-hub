// import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

// const RemoteSendNotification = lazy(
//   () => import('miscellaneous-module/SendNotification'),
// );

const SendNotification = () => {
  return (
    <ErrorBoundaryWrapper>
      {/* <RemoteSendNotification /> */}
      <div>Coming Soon</div>
    </ErrorBoundaryWrapper>
  );
};

export default SendNotification;
