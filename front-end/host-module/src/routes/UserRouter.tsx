import { Route, Routes } from 'react-router-dom';
import { Fragment } from 'react/jsx-runtime';

import UserManagement from 'pages/user';
import SuspendUser from 'pages/user/SuspendUser';
import SyncUsers from 'pages/user/SyncUsers';
import ClientUserActivityLogs from 'pages/user/ClientUserActivityLogs';
import UserAnalytics from 'pages/user/UserAnalytics';
import UserBulkImport from 'pages/user/UserBulkImport';
import UserOnboarding from 'pages/user/UserOnboarding';
import UserReports from 'pages/user/UserReports';
import Users from 'pages/user/Users';
import { routes } from 'routes/AppRoutes';
import SystemUsers from 'pages/user/SystemUsers';

const UserRouter = () => {
  return (
    <Fragment>
      <Routes>
        <Route index element={<UserManagement />} />

        <Route path={routes.users.path} element={<Users />} />
        <Route path={routes.systemUsers.path} element={<SystemUsers />} />

        <Route path={routes.userOnboarding.path} element={<UserOnboarding />} />
        <Route path={routes.userBulkImport.path} element={<UserBulkImport />} />
        <Route path={routes.syncUser.path} element={<SyncUsers />} />
        <Route path={routes.userList.path} element={<Users />} />
        <Route path={routes.userReports.path} element={<UserReports />} />
        <Route path={routes.userAnalytics.path} element={<UserAnalytics />} />
        <Route
          path={routes.userActivityLogs.path}
          element={<ClientUserActivityLogs />}
        />
        <Route path={routes.suspendUser.path} element={<SuspendUser />} />
      </Routes>
    </Fragment>
  );
};

export default UserRouter;
