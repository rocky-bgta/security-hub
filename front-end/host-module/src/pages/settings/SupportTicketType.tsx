import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteSupportTicketType = lazy(
  () => import('miscellaneous-module/SupportTicketType'),
);

const SupportTicketType = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteSupportTicketType />
    </ErrorBoundaryWrapper>
  );
};

export default SupportTicketType;
