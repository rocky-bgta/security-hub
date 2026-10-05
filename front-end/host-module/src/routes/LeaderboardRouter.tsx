import { Route, Routes } from 'react-router-dom';

import LeaderboardManagement from 'pages/leaderboard';
import CreateLeaderboard from 'pages/leaderboard/CreateLeaderboard';
import LeaderBoards from 'pages/leaderboard/Leaderboards';
import { routes } from 'routes/AppRoutes';

const LeaderboardRouter = () => {
  return (
    <Routes>
      <Route index element={<LeaderboardManagement />} />

      {/* <Route path={routes.leaderList.path} element={<LeaderList />} /> */}
      <Route
        path={routes.createLeaderboard.path}
        element={<CreateLeaderboard />}
      />

      <Route path={routes.leaderList.path} element={<LeaderBoards />} />
    </Routes>
  );
};

export default LeaderboardRouter;
