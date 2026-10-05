import AspireAdminClientActivityLogs from 'features/aspire-admin/ClientActivityLogs';
import ClientAdminActivityLogs from 'features/client-admin/ClientActivityLogs';
import MSPAdminClientActivityLogs from 'features/msp-admin/ClientActivityLogs';
import { useAuth } from 'hooks/UseAuth';

import { ROLE } from 'utils/Role';

const ActivityLogs = () => {
  const { role } = useAuth();

  if (role === ROLE.ASPIRE_ADMIN) {
    return <AspireAdminClientActivityLogs />;
  }

  if (role === ROLE.CLIENT_ADMIN) {
    return <ClientAdminActivityLogs />;
  }
  if (role === ROLE.MSP_ADMIN) {
    return <MSPAdminClientActivityLogs />;
  }
  return null;
};

export default ActivityLogs;
