import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteLandingPageCreate = lazy(
  () => import('phishing-module/LandingPageCreate'),
);

const LandingPageCreate = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteLandingPageCreate />
    </ErrorBoundaryWrapper>
  );
};

export default LandingPageCreate;
