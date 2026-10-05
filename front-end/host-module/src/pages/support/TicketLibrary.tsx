// import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

// const RemoteTicketLibrary = lazy(
//   () => import('miscellaneous-module/TicketLibrary'),
// );

const TicketLibrary = () => {
  return (
    <ErrorBoundaryWrapper>
      {/* <RemoteTicketLibrary /> */}
      <div>Coming Soon</div>
    </ErrorBoundaryWrapper>
  );
};

export default TicketLibrary;
