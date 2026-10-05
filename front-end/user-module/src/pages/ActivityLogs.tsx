import { useAuth } from 'hooks/UseAuth';

import { ROLE } from 'utils/Role';
import AllUserActivityLogs from './aspire-admin/AllUserActivityLogs';
import ClientActivityLogs from './client-admin/ClientActivityLogs';
import UserActivityLogs from './user/UserActivityLogs';
import MSPActivityLogs from './msp-admin/ActivityLogs';

const ActivityLogs = () => {
  const { role } = useAuth();

  if (role === ROLE.ASPIRE_ADMIN) {
    return <AllUserActivityLogs />;
  }

  if (role === ROLE.CLIENT_ADMIN) {
    return <ClientActivityLogs />;
  }

  if (role === ROLE.CLIENT_USER) {
    return <UserActivityLogs />;
  }
  
  if (role === ROLE.MSP_ADMIN) {
    return <MSPActivityLogs />;
  }
  return null;
};

export default ActivityLogs;
