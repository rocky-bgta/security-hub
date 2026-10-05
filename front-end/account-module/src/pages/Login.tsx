import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteLogin = lazy(() => import('home-module/Login'));

const Login = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteLogin />
    </ErrorBoundaryWrapper>
  );
};

export default Login;
