import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';
import ClientUserAccountLayout from 'components/layout/ClientUserAccoutLayout';
import { ClientUserRoutes } from 'routes/ClientUserRoutes';

const RemoteClientUserProfileSettings = lazy(
  () => import('content-module/ProfileSettings'),
);

const ClientUserProfileSettings = () => {
  return (
    <ClientUserAccountLayout hostPath={ClientUserRoutes}>
      <ErrorBoundaryWrapper>
        <RemoteClientUserProfileSettings />
      </ErrorBoundaryWrapper>
    </ClientUserAccountLayout>
  );
};

export default ClientUserProfileSettings;
