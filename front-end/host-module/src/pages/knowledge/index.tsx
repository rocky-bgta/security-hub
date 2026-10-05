import { useNavigate } from 'react-router-dom';

import { useAuth } from 'hooks/UseAuth';
import { AspireAdminRoutes } from 'routes/AspireAdminRoutes';
import { KnowledgeHubManagementRoutes } from 'routes/KnowledgeHubManagementRoutes';
import { ROLE } from 'utils/Role';

const KnowledgeHubManagement = () => {
  const { role } = useAuth();
  const navigate = useNavigate();

  if (role === ROLE.ASPIRE_ADMIN)
    navigate(AspireAdminRoutes.manageResources.path);
  if (role === ROLE.CLIENT_ADMIN)
    navigate(KnowledgeHubManagementRoutes.knowledgeHub.path);

  return null;
};

export default KnowledgeHubManagement;
