import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';
import { AspireAdminRoutes } from 'routes/AspireAdminRoutes';

const RemoteRequestPolicy = lazy(
  () => import('miscellaneous-module/RequestPolicy'),
);

const RequestPolicy = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteRequestPolicy hostPath={AspireAdminRoutes} />
    </ErrorBoundaryWrapper>
  );
};

export default RequestPolicy;
