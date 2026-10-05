import NotificationTemplates from 'features/aspire-admin/NotificationTemplates';
import { useAuth } from 'hooks/UseAuth';
import { ROLE } from 'utils/Role';
import NotFound from './NotFound';

const NotificationTemplatesPage = () => {
  const { role } = useAuth();

  if (role === ROLE.ASPIRE_ADMIN) {
    return <NotificationTemplates />;
  }

  return <NotFound />;
};

export default NotificationTemplatesPage;
