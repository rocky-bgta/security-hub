import { useNavigate } from 'react-router-dom';

import { useAuth } from 'hooks/UseAuth';
import { LicenseManagementRoutes } from 'routes/LicenseManagementRoutes';
import { ROLE } from 'utils/Role';

const LicenseManagement = () => {
  const { role } = useAuth();
  const navigate = useNavigate();

  if (role === ROLE.CLIENT_ADMIN) {
    navigate(LicenseManagementRoutes.licenseOverview.path);
  }

  return null;
};

export default LicenseManagement;
