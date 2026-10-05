import { useNavigate } from 'react-router-dom';

import { useAuth } from 'hooks/UseAuth';
import { AspireAdminRoutes } from 'routes/AspireAdminRoutes';
import { SupportManagementRoutes } from 'routes/SupportManagementRoutes';
import { ROLE } from 'utils/Role';

const SupportManagement = () => {
  const { role } = useAuth();
  const navigate = useNavigate();

  if (role === ROLE.ASPIRE_ADMIN)
    navigate(AspireAdminRoutes.ticketLibrary.path);
  else navigate(SupportManagementRoutes.supportTickets.path);

  return null;
};

export default SupportManagement;
