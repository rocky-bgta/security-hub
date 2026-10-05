import { Navigate } from 'react-router-dom';
import { routes } from 'routes/AppRoutes';

const BreachMonitor = () => {
  return <Navigate to={routes.breachMonitorDashboard.path} replace />;
};

export default BreachMonitor;
