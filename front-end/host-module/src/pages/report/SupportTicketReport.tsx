import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteSupportTicketReport = lazy(
  () => import('miscellaneous-module/SupportTicketReport'),
);

const SupportTicketReport = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteSupportTicketReport />
    </ErrorBoundaryWrapper>
  );
};

export default SupportTicketReport;
