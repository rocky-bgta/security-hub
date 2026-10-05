import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteNetTerm = lazy(() => import('miscellaneous-module/NetTerm'));

const NetTerm = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteNetTerm />
    </ErrorBoundaryWrapper>
  );
};

export default NetTerm;
