import { useAuth } from 'hooks/UseAuth';
import { ROLE } from 'utils/Role';
import AspireAdminAssignedPackages from './aspire-admin/AssignedPackages';
import ClientAdminAssignedPackages from './client-admin/AssignedPackages';

const AssignedPackages = () => {
  const { role } = useAuth();

  if (role === ROLE.ASPIRE_ADMIN) {
    return <AspireAdminAssignedPackages />;
  }

  if (role === ROLE.CLIENT_ADMIN) {
    return <ClientAdminAssignedPackages />;
  }
};

export default AssignedPackages;
