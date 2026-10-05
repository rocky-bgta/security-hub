import { useLocation } from 'react-router-dom';

import DeepfakeProviderConfiguration from 'pages/deepfake/DeepfakeProviderConfiguration';
import ProviderConfiguration from 'pages/phishing/ProviderConfiguration';

const ProviderConfigurationRoute = () => {
  const { pathname } = useLocation();

  if (pathname.includes('/deepfake-management/')) {
    return <DeepfakeProviderConfiguration />;
  }

  return <ProviderConfiguration />;
};

export default ProviderConfigurationRoute;
