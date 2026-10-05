import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteOrganizationTypes = lazy(
  () => import('miscellaneous-module/OrganizationTypes'),
);

const OrganizationTypes = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteOrganizationTypes />
    </ErrorBoundaryWrapper>
  );
};

export default OrganizationTypes;
