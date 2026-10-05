import { Route, Routes } from 'react-router-dom';

import PolicyManagement from 'pages/policy';
import AssignedPolicy from 'pages/policy/AssignedPolicy';
import PolicyList from 'pages/policy/PolicyList';
import RequestedPolicy from 'pages/policy/RequestedPolicy';
import RequestPolicy from 'pages/policy/RequestPolicy';
import { routes } from 'routes/AppRoutes';

const PolicyRouter = () => {
  return (
    <Routes>
      <Route index element={<PolicyManagement />} />

      <Route path={routes.policyList.path} element={<PolicyList />} />
      <Route path={routes.requestPolicy.path} element={<RequestPolicy />} />

      <Route path={routes.assignedPolicies.path} element={<AssignedPolicy />} />
      <Route
        path={routes.requestedPolicies.path}
        element={<RequestedPolicy />}
      />
    </Routes>
  );
};

export default PolicyRouter;
