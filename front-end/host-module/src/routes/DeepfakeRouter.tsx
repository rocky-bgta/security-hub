import { Route, Routes } from 'react-router-dom';

import ContentLibrary from 'pages/deepfake/ContentLibrary';
import CreateDeepFake from 'pages/deepfake/CreateDeepFake';
import DeepfakeManagement from 'pages/deepfake';
import DeepfakeProviderConfiguration from 'pages/deepfake/DeepfakeProviderConfiguration';
import PhishingDashboard from 'pages/phishing/PhishingDashboard';
import { routes } from 'routes/AppRoutes';

const DeepfakeRouter = () => {
  return (
    <Routes>
      <Route index element={<DeepfakeManagement />} />
      <Route
        path={routes.deepfakeDashboard.path}
        element={<PhishingDashboard />}
      />
      <Route path={routes.deepfakeCreate.path} element={<CreateDeepFake />} />
      <Route path={routes.deepfakeEdit.path} element={<CreateDeepFake />} />
      <Route
        path={routes.deepFakeContentLibrary.path}
        element={<ContentLibrary />}
      />
      <Route
        path={routes.deepfakeProviderConfiguration.path}
        element={<DeepfakeProviderConfiguration />}
      />
    </Routes>
  );
};

export default DeepfakeRouter;
