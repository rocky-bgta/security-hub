import { Route, Routes } from 'react-router-dom';

import BaseLayout from 'components/layouts/BaseLayout';
import ResetPassword from 'pages/auth/ResetPassword';
import Login from 'pages/auth/Login';
import RequestResetPassword from 'pages/auth/RequestResetPassword';
import Dashboard from 'pages/dashboard/Dashboard';
import NotFound from 'pages/NotFound';
import { routes } from 'routes/Route';
import TwoFactorAuthSetup from 'pages/auth/TwoFactorAuthSetup';
import TwoFactorAuthVerify from 'pages/auth/TwoFactorAuthVerify';
import SetPassword from 'pages/auth/SetPassword';
import AuthLayout from 'components/layouts/AuthLayout';

const authChildPath = (path: string) => path.replace(/^\/auth\/?/, '');

const AppRouter = () => {
  return (
    <Routes>
      <Route path="/" element={<BaseLayout />}>
        <Route index element={<Dashboard />} />
      </Route>

      <Route path="/auth" element={<AuthLayout />}>
        <Route path={authChildPath(routes.login.path)} element={<Login />} />
        <Route
          path={authChildPath(routes.twoFactorAuthSetup.path)}
          element={<TwoFactorAuthSetup />}
        />
        <Route
          path={authChildPath(routes.twoFactorAuthVerify.path)}
          element={<TwoFactorAuthVerify />}
        />
        <Route
          path={authChildPath(routes.requestResetPassword.path)}
          element={<RequestResetPassword />}
        />
        <Route
          path={authChildPath(routes.resetPassword.path)}
          element={<ResetPassword />}
        />
        <Route
          path={authChildPath(routes.setPassword.path)}
          element={<SetPassword />}
        />
      </Route>

      <Route path="*" element={<NotFound />} />
    </Routes>
  );
};

export default AppRouter;
