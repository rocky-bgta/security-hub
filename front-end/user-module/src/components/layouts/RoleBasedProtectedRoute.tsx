import { ReactNode } from 'react';
import { Navigate } from 'react-router-dom';

import Loader from 'common/loader/Loader';
import { useAuth } from 'hooks/UseAuth';
import NotFound from 'pages/NotFound';
import { routes } from 'routes/Routes';
import { ROLE } from 'utils/Role';

interface IProps {
  children: ReactNode;
  allowed: Array<string>;
}

const RoleBasedProtectedRoute = ({ children, allowed }: IProps) => {
  const { loading, role, isAuthenticated } = useAuth();

  if (loading) {
    return <Loader />;
  }

  if (!isAuthenticated) {
    return <Navigate to={routes.login.path} replace />;
  }

  if (!allowed.includes(role)) return <NotFound />;

  return <>{children}</>;
};

export default RoleBasedProtectedRoute;
