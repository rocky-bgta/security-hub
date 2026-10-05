import { Navigate, Route, Routes } from 'react-router-dom';

import CreateDepartment from 'pages/department/CreateDepartment';
import DepartmentList from 'pages/department/DepartmentList';
import { routes } from 'routes/AppRoutes';

const DepartmentRouter = () => {
  return (
    <Routes>
      <Route index element={<Navigate to="department-list" />} />
      <Route path={routes.departmentList.path} element={<DepartmentList />} />
      <Route
        path={routes.createDepartment.path}
        element={<CreateDepartment />}
      />
    </Routes>
  );
};

export default DepartmentRouter;
