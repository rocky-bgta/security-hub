import { Navigate, Outlet, useLocation } from 'react-router-dom';

import Loader from 'common/loader/Loader';
import AspireAdminLayout from 'components/layouts/AspireAdminLayout';
import ClientAdminLayout from 'components/layouts/ClientAdminLayout';
import ClientUserLayout from 'components/layouts/ClientUserLayout';
import useAuth from 'hooks/UseAuth';
import useStore from 'hooks/UseStore';
import { AspireAdminRoutes } from 'routes/AspireAdminRoutes';
import { ClientAdminRoutes } from 'routes/ClientAdminRoutes';
import { ClientUserRoutes } from 'routes/ClientUserRoutes';
import { routes } from 'routes/Route';
import ROLE from 'utils/Role';
import MspLayout from './MspLayout';
import { MspRoutes } from 'routes/MspRoutes';

const BaseLayout = () => {
  const { loading, role, isAuthenticated } = useAuth();
  const { userInfo, isStoreReady } = useStore();
  const location = useLocation();

  if (loading) {
    return <Loader />;
  }

  if (!isAuthenticated) {
    return (
      <Navigate to={routes.login.path} replace state={{ from: location }} />
    );
  }

  // Wait for store init / user-details only. Do NOT block on menus —
  // role-permissions can take 40s+ against remote Mongo and used to leave
  // this spinner up indefinitely when menus failed or were empty.
  if (!isStoreReady || !userInfo.userId) {
    return <Loader />;
  }

  if (userInfo.selfOnboardingUser) {
    return <Navigate to={routes.selfOnboard.path} replace />;
  }

  if (role === ROLE.SUPER_ADMIN) {
    return (
      <AspireAdminLayout hostPath={AspireAdminRoutes}>
        <Outlet />
      </AspireAdminLayout>
    );
  }

  if (role === ROLE.ASPIRE_ADMIN || role === ROLE.FINANCE_ADMIN) {
    return (
      <AspireAdminLayout hostPath={AspireAdminRoutes}>
        <Outlet />
      </AspireAdminLayout>
    );
  }

  if (role === ROLE.MSP_ADMIN) {
    return (
      <MspLayout hostPath={MspRoutes}>
        <Outlet />
      </MspLayout>
    );
  }

  if (role === ROLE.CLIENT_ADMIN) {
    return (
      <ClientAdminLayout hostPath={ClientAdminRoutes}>
        <Outlet />
      </ClientAdminLayout>
    );
  }

  if (role === ROLE.CLIENT_USER) {
    return (
      <ClientUserLayout hostPath={ClientUserRoutes}>
        <Outlet />
      </ClientUserLayout>
    );
  }

  return <Loader />;
};

export default BaseLayout;
