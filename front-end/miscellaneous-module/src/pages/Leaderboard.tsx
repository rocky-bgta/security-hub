import { useAuth } from 'hooks/UseAuth';

import { ROLE } from 'utils/Role';
import AspireLeaderboard from './leader-board/AspireLeaderboard';
import ClientLeaderboard from './leader-board/ClientLeaderboard';
import MSPAdminLeaderboard from './leader-board/MspLeaderboard';

const Leaderboard = () => {
  const { role } = useAuth();

  if (role === ROLE.CLIENT_ADMIN) {
    return <ClientLeaderboard />;
  }

  if (role === ROLE.ASPIRE_ADMIN) {
    return <AspireLeaderboard />;
  }

  if (role === ROLE.MSP_ADMIN) {
    return <MSPAdminLeaderboard />;
  }
  return null;
};

export default Leaderboard;
