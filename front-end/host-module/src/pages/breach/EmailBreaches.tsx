import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteEmailBreaches = lazy(() => import('phishing-module/EmailBreaches'));

const EmailBreaches = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteEmailBreaches />
    </ErrorBoundaryWrapper>
  );
};

export default EmailBreaches;
