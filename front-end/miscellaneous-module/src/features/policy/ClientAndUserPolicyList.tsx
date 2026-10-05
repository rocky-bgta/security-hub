import { useAuth } from 'hooks/UseAuth';
import NotFound from 'pages/NotFound';
import { ROLE } from 'utils/Role';
import UserPolicyList from './UserPolicyList';
import ClientPolicyList from './ClientPolicyList';

const ClientAndUserPolicyList = () => {
  const { role } = useAuth();

  if (role === ROLE.CLIENT_ADMIN) {
    return <ClientPolicyList />;
  }

  if (role === ROLE.CLIENT_USER) {
    return <UserPolicyList />;
  }

  return <NotFound />;
};

export default ClientAndUserPolicyList;
