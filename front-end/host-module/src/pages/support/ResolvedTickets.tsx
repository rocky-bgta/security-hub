import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteResolvedTickets = lazy(
  () => import('miscellaneous-module/ResolvedTickets'),
);

const ResolvedTickets = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteResolvedTickets />
    </ErrorBoundaryWrapper>
  );
};

export default ResolvedTickets;
