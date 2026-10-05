import { useNavigate } from 'react-router-dom';

import { useAuth } from 'hooks/UseAuth';
import { AspireAdminRoutes } from 'routes/AspireAdminRoutes';
import { ReportManagementRoutes } from 'routes/ReportManagementRoutes';
import { ROLE } from 'utils/Role';

const ReportManagement = () => {
  const { role } = useAuth();
  const navigate = useNavigate();

  if (role === ROLE.ASPIRE_ADMIN) navigate(AspireAdminRoutes.userReport.path);
  if (role === ROLE.MSP_ADMIN)
    navigate(ReportManagementRoutes.licenseReport.path);

  return null;
};

export default ReportManagement;
