import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteMFAVerify = lazy(() => import('home-module/MFAVerify'));

const MFAVerify = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteMFAVerify />
    </ErrorBoundaryWrapper>
  );
};

export default MFAVerify;
