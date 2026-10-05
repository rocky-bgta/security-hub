import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteSenderProfileManagement = lazy(
  () => import('phishing-module/SenderProfileManagement'),
);

const SenderProfileManagement = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteSenderProfileManagement />
    </ErrorBoundaryWrapper>
  );
};

export default SenderProfileManagement;
