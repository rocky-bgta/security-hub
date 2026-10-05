import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteCertificateAnalytics = lazy(
  () => import('content-module/CertificateAnalytics'),
);

const CertificateAnalytics = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteCertificateAnalytics />
    </ErrorBoundaryWrapper>
  );
};

export default CertificateAnalytics;
