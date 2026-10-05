import { useAuth } from 'hooks/UseAuth';
import { ROLE } from 'utils/Role';
import AspireAdminSubPackages from './aspire-admin/SubPackages';
import ClientAdminSubPackages from './client-admin/SubPackages';
import MSPAdminSubPackages from './msp-admin/SubPackages';

const SubPackages = () => {
  const { role } = useAuth();

  if (role === ROLE.ASPIRE_ADMIN) {
    return <AspireAdminSubPackages />;
  }

  if (role === ROLE.MSP_ADMIN) {
    return <MSPAdminSubPackages />;
  }

  if (role === ROLE.CLIENT_ADMIN) {
    return <ClientAdminSubPackages />;
  }
};

export default SubPackages;
