import AspireAdminPersonalInformation from 'features/aspire-admin/PersonalInformation';
import ClientAdminPersonalInformation from 'features/client-admin/PersonalInformation';
import { useAuth } from 'hooks/UseAuth';
import { ROLE } from 'utils/Role';
import NotFound from './NotFound';

const PersonalInformation = () => {
  const { role } = useAuth();

  if (role === ROLE.ASPIRE_ADMIN) {
    return <AspireAdminPersonalInformation />;
  }

  if (role === ROLE.CLIENT_ADMIN) {
    return <ClientAdminPersonalInformation />;
  }

  if (role === ROLE.MSP_ADMIN) {
    return <div> Coming Soon </div>;
  }

  return <NotFound />;
};

export default PersonalInformation;
