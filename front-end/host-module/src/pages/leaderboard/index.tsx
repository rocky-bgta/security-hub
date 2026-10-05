import { useNavigate } from 'react-router-dom';

import { useAuth } from 'hooks/UseAuth';
import { LeaderboardManagementRoutes } from 'routes/LeaderboardManagementRoutes';
import { ROLE } from 'utils/Role';

const LeaderboardManagement = () => {
  const { role } = useAuth();
  const navigate = useNavigate();

  if (role === ROLE.ASPIRE_ADMIN)
    navigate(LeaderboardManagementRoutes.leaderboard.path);
  if (role === ROLE.CLIENT_ADMIN)
    navigate(LeaderboardManagementRoutes.leaderboard.path);

  return null;
};

export default LeaderboardManagement;
