import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteCertificateIssued = lazy(
  () => import('content-module/CertificateIssued'),
);

const CertificateIssued = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteCertificateIssued />
    </ErrorBoundaryWrapper>
  );
};

export default CertificateIssued;
