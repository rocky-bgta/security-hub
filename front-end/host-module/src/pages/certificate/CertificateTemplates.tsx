import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteCertificateTemplates = lazy(
  () => import('content-module/CertificateTemplates'),
);

const CertificateTemplates = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteCertificateTemplates />
    </ErrorBoundaryWrapper>
  );
};

export default CertificateTemplates;
