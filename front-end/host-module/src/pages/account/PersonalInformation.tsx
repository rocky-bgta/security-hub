import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemotePersonalInformation = lazy(
  () => import('account-module/PersonalInformation'),
);

const PersonalInformation = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemotePersonalInformation />
    </ErrorBoundaryWrapper>
  );
};

export default PersonalInformation;
