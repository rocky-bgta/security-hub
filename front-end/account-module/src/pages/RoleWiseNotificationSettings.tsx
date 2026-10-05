import RoleWiseNotificationSettings from 'features/aspire-admin/RoleWiseNotificationSettings';
import { useAuth } from 'hooks/UseAuth';
import { ROLE } from 'utils/Role';
import NotFound from './NotFound';

const RoleWiseNotificationSettingsPage = () => {
  const { role } = useAuth();

  if (role === ROLE.ASPIRE_ADMIN) {
    return <RoleWiseNotificationSettings />;
  }

  return <NotFound />;
};

export default RoleWiseNotificationSettingsPage;
