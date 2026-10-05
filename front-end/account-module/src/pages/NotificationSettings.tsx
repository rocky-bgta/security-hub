import AspireAdminNotificationSettings from 'features/aspire-admin/NotificationSettings';
import ClientAdminNotificationSettings from 'features/client-admin/NotificationSettings';
import { useAuth } from 'hooks/UseAuth';
import { ROLE } from 'utils/Role';
import NotFound from './NotFound';

const NotificationSettings = () => {
  const { role } = useAuth();

  if (role === ROLE.ASPIRE_ADMIN) {
    return <AspireAdminNotificationSettings />;
  }

  if (role === ROLE.CLIENT_ADMIN) {
    return <ClientAdminNotificationSettings />;
  }

  return <NotFound />;
};

export default NotificationSettings;
