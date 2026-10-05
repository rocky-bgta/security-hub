import { lazy } from 'react';
import { Outlet } from 'react-router-dom';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';
import { routes } from 'routes/Routes';

const RemoteClientUserAccountLayout = lazy(
  () => import('home-module/ClientUserAccountLayout'),
);

const ClientUserAccountLayout = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteClientUserAccountLayout hostPath={routes}>
        <Outlet />
      </RemoteClientUserAccountLayout>
    </ErrorBoundaryWrapper>
  );
};

export default ClientUserAccountLayout;
