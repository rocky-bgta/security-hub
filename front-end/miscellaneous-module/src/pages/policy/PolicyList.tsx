import AspireAdminPolicyList from 'features/policy/AspireAdminPolicyList';
import ClientAndUserPolicyList from 'features/policy/ClientAndUserPolicyList';
import MSPAdminPolicyList from 'features/policy/MspPolicyList';
import { useAuth } from 'hooks/UseAuth';
import NotFound from 'pages/NotFound';
import { ROLE } from 'utils/Role';

const PolicyList = () => {
  const { role } = useAuth();

  if (role === ROLE.ASPIRE_ADMIN) {
    return <AspireAdminPolicyList />;
  }

  if (role === ROLE.CLIENT_ADMIN || role === ROLE.CLIENT_USER ) {
    return <ClientAndUserPolicyList />;
  }

  if (role === ROLE.MSP_ADMIN) {
    return <MSPAdminPolicyList />;
  }

  return <NotFound />;
};

export default PolicyList;
