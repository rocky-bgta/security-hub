import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteCertificateHistory = lazy(
  () => import('content-module/CertificateHistory'),
);

const CertificateHistory = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteCertificateHistory />
    </ErrorBoundaryWrapper>
  );
};

export default CertificateHistory;
