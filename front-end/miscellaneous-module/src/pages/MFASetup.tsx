import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteMFASetup = lazy(() => import('home-module/MFASetup'));

const MFASetup = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteMFASetup />
    </ErrorBoundaryWrapper>
  );
};

export default MFASetup;
