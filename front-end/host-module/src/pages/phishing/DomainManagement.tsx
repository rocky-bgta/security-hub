import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteDomainManagement = lazy(
  () => import('phishing-module/DomainManagement'),
);

const DomainManagement = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteDomainManagement />
    </ErrorBoundaryWrapper>
  );
};

export default DomainManagement;
