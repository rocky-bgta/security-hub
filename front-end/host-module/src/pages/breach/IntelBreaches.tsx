import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteIntelBreaches = lazy(() => import('phishing-module/IntelBreaches'));

const IntelBreaches = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteIntelBreaches />
    </ErrorBoundaryWrapper>
  );
};

export default IntelBreaches;
