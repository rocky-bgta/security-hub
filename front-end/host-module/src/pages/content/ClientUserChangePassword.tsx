import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';
import ClientUserAccountLayout from 'components/layout/ClientUserAccoutLayout';
import { ClientUserRoutes } from 'routes/ClientUserRoutes';

const RemoteClientUserChangePassword = lazy(
  () => import('content-module/ChangePassword'),
);

const ClientUserChangePassword = () => {
  return (
    <ClientUserAccountLayout hostPath={ClientUserRoutes}>
      <ErrorBoundaryWrapper>
        <RemoteClientUserChangePassword />
      </ErrorBoundaryWrapper>
    </ClientUserAccountLayout>
  );
};

export default ClientUserChangePassword;
