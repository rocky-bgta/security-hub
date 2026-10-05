import { Navigate, Outlet, useLocation } from 'react-router-dom';

import Loader from 'common/loader/Loader';
import AspireAdminLayout from 'components/layouts/AspireAdminLayout';
import ClientAdminLayout from 'components/layouts/ClientAdminLayout';
import { useAuth } from 'hooks/UseAuth';
import { routes } from 'routes/Routes';
import { ROLE } from 'utils/Role';
import ClientUserLayout from './ClientUserLayout';
import { useStore } from 'hooks/UseStore';
import MspLayout from './MspLayout';

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

  if (role === ROLE.MSP_ADMIN) {
    return (
      <MspLayout hostPath={routes}>
        <Outlet />
      </MspLayout>
    );
  }

  if (role === ROLE.CLIENT_ADMIN) {
    return (
      <ClientAdminLayout hostPath={routes}>
        <Outlet />
      </ClientAdminLayout>
    );
  }

  if (role === ROLE.CLIENT_USER) {
    return (
      <ClientUserLayout hostPath={routes}>
        <Outlet />
      </ClientUserLayout>
    );
  }

  return <Loader />;
};

export default BaseLayout;
