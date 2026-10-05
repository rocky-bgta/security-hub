import { useNavigate } from 'react-router-dom';

import { useAuth } from 'hooks/UseAuth';
import { AspireAdminRoutes } from 'routes/AspireAdminRoutes';
import { PolicyManagementRoutes } from 'routes/PolicyManagementRoutes';
import { ROLE } from 'utils/Role';

const PolicyManagement = () => {
  const { role } = useAuth();
  const navigate = useNavigate();

  if (role === ROLE.ASPIRE_ADMIN) navigate(AspireAdminRoutes.policyList.path);
  if (role === ROLE.MSP_ADMIN)
    navigate(PolicyManagementRoutes.assignedPolicies.path);

  return null;
};

export default PolicyManagement;
