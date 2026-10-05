import { Navigate, Route, Routes } from 'react-router-dom';

import MenuList from 'pages/menu/MenuList';
import { routes } from 'routes/AppRoutes';

const MenuRouter = () => {
  return (
    <Routes>
      <Route index element={<Navigate to="menu-list" />} />
      <Route path={routes.menuList.path} element={<MenuList />} />
    </Routes>
  );
};

export default MenuRouter;
