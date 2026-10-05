import { useNavigate } from 'react-router-dom';

import { useAuth } from 'hooks/UseAuth';
import { AspireAdminRoutes } from 'routes/AspireAdminRoutes';
import { CertificateManagementRoutes } from 'routes/CertificateManagementRoutes';
import { ROLE } from 'utils/Role';

const CertificateManagement = () => {
  const { role } = useAuth();
  const navigate = useNavigate();

  if (role === ROLE.ASPIRE_ADMIN)
    navigate(AspireAdminRoutes.certificateTemplate.path);
  if (role === ROLE.CLIENT_ADMIN)
    navigate(CertificateManagementRoutes.certificateHistory.path);

  return null;
};

export default CertificateManagement;
