import { useNavigate } from 'react-router-dom';

import { useAuth } from 'hooks/UseAuth';
import { AccountManagementRoutes } from 'routes/AccountManagementRoutes';
import { AspireAdminRoutes } from 'routes/AspireAdminRoutes';
import { ROLE } from 'utils/Role';

const AccountManagement = () => {
  const { role } = useAuth();
  const navigate = useNavigate();

  if (role === ROLE.ASPIRE_ADMIN)
    navigate(AspireAdminRoutes.profileInformation.path);
  if (role === ROLE.CLIENT_ADMIN)
    navigate(AccountManagementRoutes.personalInformation.path);

  return null;
};

export default AccountManagement;
