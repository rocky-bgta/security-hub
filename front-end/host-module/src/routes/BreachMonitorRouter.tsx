import { Route, Routes } from 'react-router-dom';

import { routes } from 'routes/AppRoutes';
import BreachMonitor from 'pages/breach';
import BreachMonitorDashboard from 'pages/breach/BreachMonitorDashboard';
import EmailBreaches from 'pages/breach/EmailBreaches';
import IPBreaches from 'pages/breach/IPBreaches';
import IntelBreaches from 'pages/breach/IntelBreaches';
import BreachVulnerabilities from 'pages/breach/BreachVulnerabilities';
import CompanyImpersonation from 'pages/breach/CompanyImpersonation';

const BreachMonitorRouter = () => {
  return (
    <Routes>
      <Route index element={<BreachMonitor />} />
      <Route
        path={routes.breachMonitorDashboard.path}
        element={<BreachMonitorDashboard />}
      />
      <Route
        path={routes.breachMonitorEmails.path}
        element={<EmailBreaches />}
      />
      <Route path={routes.breachMonitorIps.path} element={<IPBreaches />} />
      <Route
        path={routes.breachMonitorThreatIntel.path}
        element={<IntelBreaches />}
      />
      <Route
        path={routes.breachMonitorVulnerabilities.path}
        element={<BreachVulnerabilities />}
      />
      <Route
        path={routes.breachMonitorCompanyImpersonation.path}
        element={<CompanyImpersonation />}
      />
    </Routes>
  );
};

export default BreachMonitorRouter;
