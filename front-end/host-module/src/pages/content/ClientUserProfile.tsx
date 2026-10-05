import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';
import ClientUserAccountLayout from 'components/layout/ClientUserAccoutLayout';
import { ClientUserRoutes } from 'routes/ClientUserRoutes';

const RemoteClientUserProfile = lazy(
  () => import('content-module/UserProfile'),
);

const ClientUserProfile = () => {
  return (
    <ClientUserAccountLayout hostPath={ClientUserRoutes}>
      <ErrorBoundaryWrapper>
        <RemoteClientUserProfile />
      </ErrorBoundaryWrapper>
    </ClientUserAccountLayout>
  );
};

export default ClientUserProfile;
