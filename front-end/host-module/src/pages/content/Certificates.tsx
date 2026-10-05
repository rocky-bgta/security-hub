import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteCertificates = lazy(() => import('content-module/Certificates'));

const Certificates = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteCertificates />
    </ErrorBoundaryWrapper>
  );
};

export default Certificates;
