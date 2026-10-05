import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteLandingPageLibrary = lazy(
  () => import('phishing-module/LandingPageLibrary'),
);

const LandingPageLibrary = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteLandingPageLibrary />
    </ErrorBoundaryWrapper>
  );
};

export default LandingPageLibrary;
