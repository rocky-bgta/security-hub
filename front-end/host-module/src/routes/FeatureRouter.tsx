import { Navigate, Route, Routes } from 'react-router-dom';

import { routes } from 'routes/AppRoutes';
import FeatureList from 'pages/feature/List';

const FeatureRouter = () => {
  return (
    <Routes>
      <Route index element={<Navigate to="feature-list" />} />
      <Route path={routes.featureList.path} element={<FeatureList />} />
    </Routes>
  );
};

export default FeatureRouter;
