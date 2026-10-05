import { useAuth } from 'hooks/UseAuth';
import { ROLE } from 'utils/Role';
import AspireAdminPackageLicense from './aspire-admin/PackageLicense';
import ClientAdminPackageLicense from './client-admin/PackageLicense';

const PackageLicense = () => {
  const { role } = useAuth();

  if (role === ROLE.ASPIRE_ADMIN) {
    return <AspireAdminPackageLicense />;
  }

  if (role === ROLE.CLIENT_ADMIN) {
    return <ClientAdminPackageLicense />;
  }
};

export default PackageLicense;
