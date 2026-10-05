import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteIndustries = lazy(() => import('miscellaneous-module/Industries'));

const Industries = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteIndustries />
    </ErrorBoundaryWrapper>
  );
};

export default Industries;
