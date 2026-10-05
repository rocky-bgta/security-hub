import { useNavigate } from 'react-router-dom';

import { useAuth } from 'hooks/UseAuth';
import { AspireAdminRoutes } from 'routes/AspireAdminRoutes';
import { PackageManagementRoutes } from 'routes/PackageManagementRoutes';
import { ROLE } from 'utils/Role';

const PackageManagement = () => {
  const { role } = useAuth();
  const navigate = useNavigate();

  if (role === ROLE.ASPIRE_ADMIN)
    navigate(AspireAdminRoutes.assignedPackages.path);
  else navigate(PackageManagementRoutes.addPackage.path);

  return null;
};

export default PackageManagement;
