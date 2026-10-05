import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemotePendingTickets = lazy(
  () => import('miscellaneous-module/PendingTickets'),
);

const PendingTickets = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemotePendingTickets />
    </ErrorBoundaryWrapper>
  );
};

export default PendingTickets;
