import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteOrganizationSizes = lazy(
  () => import('miscellaneous-module/OrganizationSizes'),
);

const OrganizationSizes = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteOrganizationSizes />
    </ErrorBoundaryWrapper>
  );
};

export default OrganizationSizes;
