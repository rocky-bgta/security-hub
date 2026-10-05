import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemotePolicyType = lazy(() => import('miscellaneous-module/PolicyType'));

const PolicyType = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemotePolicyType />
    </ErrorBoundaryWrapper>
  );
};

export default PolicyType;
