import Loader from 'common/loader/Loader';
import { useAuth } from 'hooks/UseAuth';
import { Navigate } from 'react-router-dom';
import { routes } from 'routes/Routes';
import { ROLE } from 'utils/Role';
import ClientUser from './client-admin/UserList';
import ClientUsersList from './aspire-admin/ClientUsersList';

const AllUsers = () => {
  const { loading, role, isAuthenticated } = useAuth();

  if (loading) {
    return <Loader />;
  }

  if (!isAuthenticated) {
    return <Navigate to={routes.login.path} replace />;
  }

  if (role === ROLE.ASPIRE_ADMIN) {
    return <ClientUsersList />;
  }

  if (role === ROLE.CLIENT_ADMIN) {
    return <ClientUser />;
  }

  if (role === ROLE.MSP_ADMIN) {
    return <div>Coming Soon</div>;
  }

  return <Loader />;
};

export default AllUsers;
