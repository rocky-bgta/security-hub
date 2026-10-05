import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteCertificateReport = lazy(
  () => import('miscellaneous-module/CertificateReport'),
);

const CertificateReport = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteCertificateReport />
    </ErrorBoundaryWrapper>
  );
};

export default CertificateReport;
