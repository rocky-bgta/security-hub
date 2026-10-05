import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteIPBreaches = lazy(() => import('phishing-module/IPBreaches'));

const IPBreaches = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteIPBreaches />
    </ErrorBoundaryWrapper>
  );
};

export default IPBreaches;
