import { useAuth } from 'hooks/UseAuth';

import { ROLE } from 'utils/Role';
import ClientAdminResolvedTicket from './ClientAdminResolvedTicket';
import AspireAdminResolvedTicket from './AspireAdminResolvedTicket';
import MSPAdminResolvedTicket from './MSPAdminResolvedTicket';

const ResolvedTicket = () => {
  const { role } = useAuth();

  if (role === ROLE.CLIENT_ADMIN) return <ClientAdminResolvedTicket />;
  if (role === ROLE.ASPIRE_ADMIN) return <AspireAdminResolvedTicket />;
  if (role === ROLE.MSP_ADMIN) return <MSPAdminResolvedTicket />;
  return null;
};

export default ResolvedTicket;
