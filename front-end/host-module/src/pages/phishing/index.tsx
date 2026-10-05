import { Navigate, useLocation } from 'react-router-dom';
import { routes } from 'routes/AppRoutes';

const PhishingManagement = () => {
  const { pathname } = useLocation();

  if (pathname.startsWith('/administration-management')) {
    return <Navigate to="/administration-management/configurations" replace />;
  }

  return <Navigate to={`/phishing-management${routes.phishingDashboard.path}`} replace />;
};

export default PhishingManagement;
