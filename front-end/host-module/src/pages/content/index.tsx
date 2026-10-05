import { useNavigate } from 'react-router-dom';

import { useAuth } from 'hooks/UseAuth';
import { AspireAdminRoutes } from 'routes/AspireAdminRoutes';
import { ContentManagementRoutes } from 'routes/ContentManagementRoutes';
import { ROLE } from 'utils/Role';

const ContentManagement = () => {
  const { role } = useAuth();
  const navigate = useNavigate();

  if (role === ROLE.ASPIRE_ADMIN) navigate(AspireAdminRoutes.addContent.path);
  else navigate(ContentManagementRoutes.courseList.path);

  return null;
};

export default ContentManagement;
