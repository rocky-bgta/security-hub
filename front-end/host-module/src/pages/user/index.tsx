import { useNavigate } from 'react-router-dom';

import { useAuth } from 'hooks/UseAuth';
import { AspireAdminRoutes } from 'routes/AspireAdminRoutes';
import { UserManagementRoutes } from 'routes/UserManagementRoutes';
import { ROLE } from 'utils/Role';

const UserManagement = () => {
  const { role } = useAuth();
  const navigate = useNavigate();

  if (role === ROLE.ASPIRE_ADMIN)
    navigate(AspireAdminRoutes.userOnboarding.path);
  else navigate(UserManagementRoutes.users.path);

  return null;
};

export default UserManagement;
