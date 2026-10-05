import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';
import { AspireAdminRoutes } from 'routes/AspireAdminRoutes';

const RemotePolicyList = lazy(() => import('miscellaneous-module/PolicyList'));

const PolicyList = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemotePolicyList hostPath={AspireAdminRoutes} />
    </ErrorBoundaryWrapper>
  );
};

export default PolicyList;
