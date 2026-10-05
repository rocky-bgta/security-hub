import { Navigate, Route, Routes } from 'react-router-dom';

import BrandingList from 'pages/branding/BrandingList';
import { routes } from 'routes/AppRoutes';

const BrandingRouter = () => {
  return (
    <Routes>
      <Route index element={<Navigate to="branding-list" />} />
      <Route path={routes.brandingList.path} element={<BrandingList />} />
    </Routes>
  );
};

export default BrandingRouter;
