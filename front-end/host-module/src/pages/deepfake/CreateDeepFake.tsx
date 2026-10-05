import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteCreateDeepFake = lazy(
  () => import('phishing-module/CreateDeepFake'),
);

const CreateDeepFake = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteCreateDeepFake />
    </ErrorBoundaryWrapper>
  );
};

export default CreateDeepFake;
