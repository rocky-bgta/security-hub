import { useAuth } from 'hooks/UseAuth';
import { ROLE } from 'utils/Role';
import ClientAdminUpdatePassword from 'features/client-admin/UpdatePassword';
import NotFound from './NotFound';

const UpdatePassword = () => {
  const { role } = useAuth();

  if (role === ROLE.CLIENT_ADMIN) {
    return <ClientAdminUpdatePassword />;
  }

  if (role === ROLE.MSP_ADMIN) {
    return <div> Coming Soon </div>;
  }

  return <NotFound />;
};

export default UpdatePassword;
