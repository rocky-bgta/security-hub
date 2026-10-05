import { useNavigate } from 'react-router-dom';

import { useAuth } from 'hooks/UseAuth';
import { AspireAdminRoutes } from 'routes/AspireAdminRoutes';
import { BillingManagementRoutes } from 'routes/BillingManagementRoutes';
import { ROLE } from 'utils/Role';

const BillingManagement = () => {
  const { role } = useAuth();
  const navigate = useNavigate();

  if (role === ROLE.ASPIRE_ADMIN)
    navigate(AspireAdminRoutes.billingHistory.path);
  if (role === ROLE.CLIENT_ADMIN)
    navigate(BillingManagementRoutes.pendingPayment.path);

  return null;
};

export default BillingManagement;
