import { Navigate, Outlet, useLocation } from 'react-router-dom';

import Loader from 'common/loader/Loader';
import AspireAdminLayout from 'components/layouts/AspireAdminLayout';
import ClientAdminLayout from 'components/layouts/ClientAdminLayout';
import { useAuth } from 'hooks/UseAuth';
import { useStore } from 'hooks/UseStore';
import { routes } from 'routes/Routes';
import { ROLE } from 'utils/Role';

const BaseLayout = () => {
  const { loading, role, isAuthenticated } = useAuth();
  const { userInfo } = useStore();
  const location = useLocation();

  if (loading) {
    return <Loader />;
  }

  if (!isAuthenticated) {
    return (
      <Navigate to={routes.login.path} replace state={{ from: location }} />
    );
  }

  if (!userInfo.userId) {
    return <Loader />;
  }

  if (role === ROLE.ASPIRE_ADMIN) {
    return (
      <AspireAdminLayout hostPath={routes}>
        <Outlet />
      </AspireAdminLayout>
    );
  }

  if (role === ROLE.CLIENT_ADMIN) {
    return (
      <ClientAdminLayout hostPath={routes}>
        <Outlet />
      </ClientAdminLayout>
    );
  }

  return <Loader />;
};

export default BaseLayout;
