import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';
import { ClientUserRoutes } from 'routes/ClientUserRoutes';

const RemotePolicyList = lazy(() => import('miscellaneous-module/PolicyList'));

const ClientUserPolicyList = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemotePolicyList hostPath={ClientUserRoutes} />
    </ErrorBoundaryWrapper>
  );
};

export default ClientUserPolicyList;
