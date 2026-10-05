import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteSubPackages = lazy(() => import('content-module/SubPackages'));

const SubPackages = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteSubPackages />
    </ErrorBoundaryWrapper>
  );
};

export default SubPackages;
