import { useAuth } from 'hooks/UseAuth';

import { ROLE } from 'utils/Role';
import ClientAdminPendingTickets from './ClientAdminPendingTickets';
import AspireAdminPendingTickets from './AspireAdminPendingTickets';
import MSPAdminPendingTickets from './MspAdminPendingTickets';

const PendingTickets = () => {
  const { role } = useAuth();

  if (role === ROLE.CLIENT_ADMIN) return <ClientAdminPendingTickets />;
  if (role === ROLE.MSP_ADMIN) return <MSPAdminPendingTickets />;
  if (role === ROLE.ASPIRE_ADMIN) return <AspireAdminPendingTickets />;
  return null;
};

export default PendingTickets;
