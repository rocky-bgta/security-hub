import { useAuth } from 'hooks/UseAuth';
import { ROLE } from 'utils/Role';
import AspireAdminClientLicenseHistory from './aspire-admin/ClientLicenseHistory';
import MSPAdminClientLicenseHistory from './msp-admin/ClientLicenseHistory';

const ClientLicenseHistory = () => {
  const { role } = useAuth();

  if (role === ROLE.ASPIRE_ADMIN) {
    return <AspireAdminClientLicenseHistory />;
  }

  if (role === ROLE.MSP_ADMIN) {
    return <MSPAdminClientLicenseHistory />;
  }
};

export default ClientLicenseHistory;
