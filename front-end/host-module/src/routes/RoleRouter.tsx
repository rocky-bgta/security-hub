import { Navigate, Route, Routes } from 'react-router-dom';

import PermissionList from 'pages/role/PermissionList';
import Roles from 'pages/role/Roles';
import { routes } from 'routes/AppRoutes';

const RoleRouter = () => {
  return (
    <Routes>
      <Route index element={<Navigate to="role-list" />} />

      <Route path={routes.roleList.path} element={<Roles />} />
      <Route path={routes.permissionList.path} element={<PermissionList />} />
    </Routes>
  );
};

export default RoleRouter;
