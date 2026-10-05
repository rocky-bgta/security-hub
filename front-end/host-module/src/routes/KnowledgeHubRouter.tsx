import { Route, Routes } from 'react-router-dom';

import KnowledgeHubManagement from 'pages/knowledge';
import KnowledgeHub from 'pages/knowledge/KnowledgeHub';
import ManageResources from 'pages/knowledge/ManageResources';
import ResourceAnalytics from 'pages/knowledge/ResourceAnalytics';
import ResourceCategories from 'pages/knowledge/ResourceCategories';
import { routes } from 'routes/AppRoutes';

const KnowledgeHubRouter = () => {
  return (
    <Routes>
      <Route index element={<KnowledgeHubManagement />} />

      <Route path={routes.manageResources.path} element={<ManageResources />} />
      <Route
        path={routes.resourceCategories.path}
        element={<ResourceCategories />}
      />
      <Route
        path={routes.resourceAnalytics.path}
        element={<ResourceAnalytics />}
      />

      <Route path={routes.knowledgeHub.path} element={<KnowledgeHub />} />
    </Routes>
  );
};

export default KnowledgeHubRouter;
