import Loader from 'common/loader/Loader';
import { useAuth } from 'hooks/UseAuth';
import { Navigate } from 'react-router-dom';
import { routes } from 'routes/Routes';
import { ROLE } from 'utils/Role';
import AspireAdminBulkImport from './aspire-admin/BulkImport';
import ClientAdminBulkImport from './client-admin/UserBulkImport';

const BulkImport = ({ hostPath = routes }) => {
  const { loading, role, isAuthenticated } = useAuth();

  if (loading) {
    return <Loader />;
  }

  if (!isAuthenticated) {
    return <Navigate to={routes.login.path} replace />;
  }

  if (role === ROLE.CLIENT_ADMIN) {
    return <ClientAdminBulkImport hostPath={hostPath} />;
  }

  if (role === ROLE.ASPIRE_ADMIN) {
    return <AspireAdminBulkImport />;
  }

  return <Loader />;
};

export default BulkImport;
