import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteSupportTickets = lazy(
  () => import('miscellaneous-module/SupportTickets'),
);

const SupportTickets = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteSupportTickets />
    </ErrorBoundaryWrapper>
  );
};

export default SupportTickets;
