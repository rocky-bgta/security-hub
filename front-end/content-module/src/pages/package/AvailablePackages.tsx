import { useAuth } from 'hooks/UseAuth';
import { ROLE } from 'utils/Role';
import AspireAdminAvailablePackages from './aspire-admin/AvailablePackages';
import ClientAdminAvailablePackages from './client-admin/AvailablePackages';

const AvailablePackages = () => {
  const { role } = useAuth();

  if (role === ROLE.ASPIRE_ADMIN) {
    return <AspireAdminAvailablePackages />;
  }

  if (role === ROLE.CLIENT_ADMIN) {
    return <ClientAdminAvailablePackages />;
  }
};

export default AvailablePackages;
