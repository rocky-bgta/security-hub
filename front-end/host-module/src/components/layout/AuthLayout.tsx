import { lazy } from 'react';
import { Outlet } from 'react-router-dom';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteAuthLayout = lazy(() => import('home-module/AuthLayout'));

const AuthLayout = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteAuthLayout>
        <Outlet />
      </RemoteAuthLayout>
    </ErrorBoundaryWrapper>
  );
};

export default AuthLayout;
