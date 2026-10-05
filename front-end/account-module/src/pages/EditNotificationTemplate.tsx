import EditNotificationTemplate from 'features/aspire-admin/EditNotificationTemplate';
import { useAuth } from 'hooks/UseAuth';
import { ROLE } from 'utils/Role';
import NotFound from './NotFound';

const EditNotificationTemplatePage = () => {
  const { role } = useAuth();

  if (role === ROLE.ASPIRE_ADMIN) {
    return <EditNotificationTemplate />;
  }

  return <NotFound />;
};

export default EditNotificationTemplatePage;
