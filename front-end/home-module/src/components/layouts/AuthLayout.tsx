import { useEffect } from 'react';
import { Navigate, Outlet, useLocation } from 'react-router-dom';

import Loader from 'common/loader/Loader';
import useAuth from 'hooks/UseAuth';
import useStore from 'hooks/UseStore';
import { routes } from 'routes/Route';

const AuthLayout = () => {
  const { pathname } = useLocation();
  const { loading, isAuthenticated, isAuthenticating } = useAuth();
  const { clearStore } = useStore();

  useEffect(() => {
    if (loading || isAuthenticated || isAuthenticating) {
      return;
    }

    clearStore();
  }, [clearStore, isAuthenticated, isAuthenticating, loading, pathname]);

  if (loading) {
    return <Loader />;
  }

  if (isAuthenticated) {
    return <Navigate to={routes.dashboard.path} replace />;
  }

  return <Outlet />;
};

export default AuthLayout;
