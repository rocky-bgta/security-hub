import { Navigate, Route, Routes } from 'react-router-dom';

import BaseLayout from 'components/layouts/BaseLayout';

import { routes } from 'routes/Route';
import NotFound from 'pages/NotFound';
import Login from 'pages/Login';
import PersonalInformation from 'pages/PersonalInformation';
import BrandingManagement from 'pages/BrandingManagement';
import SecuritySettings from 'pages/SecuritySettings';
import NotificationSettings from 'pages/NotificationSettings';
import RoleWiseNotificationSettings from 'pages/RoleWiseNotificationSettings';
import NotificationTemplates from 'pages/NotificationTemplates';
import EditNotificationTemplate from 'features/aspire-admin/EditNotificationTemplate';
import UserAccountSettings from 'pages/UserAccountSettings';
import GlobalSettings from 'pages/GlobalSettings';
import UpdatePassword from 'pages/UpdatePassword';
import MFASetup from 'pages/MFASetup';
import MFAVerify from 'pages/MFAVerify';

const AppRouter = () => {
  return (
    <Routes>
      <Route path="/" element={<BaseLayout />}>
        <Route
          index
          element={<Navigate to={routes.personalInformation.path} replace />}
        />

        <Route
          path={routes.personalInformation.path}
          element={<PersonalInformation />}
        />
        <Route
          path={routes.brandingManagement.path}
          element={<BrandingManagement />}
        />
        <Route
          path={routes.securitySettings.path}
          element={<SecuritySettings />}
        />
        <Route
          path={routes.notificationSettings.path}
          element={<NotificationSettings />}
        />
        <Route
          path={routes.roleWiseNotificationSettings.path}
          element={<RoleWiseNotificationSettings />}
        />
        <Route
          path={routes.notificationTemplates.path}
          element={<NotificationTemplates />}
        />
        <Route
          path={routes.editNotificationTemplate.path}
          element={<EditNotificationTemplate />}
        />
        <Route
          path={routes.userAccountSettings.path}
          element={<UserAccountSettings />}
        />
        <Route path={routes.globalSettings.path} element={<GlobalSettings />} />
        <Route
          path={routes.accountSecurity.path}
          element={<UpdatePassword />}
        />
      </Route>

      <Route path={routes.login.path} element={<Login />} />
      <Route path={routes.twoFactorAuthSetup.path} element={<MFASetup />} />
      <Route path={routes.twoFactorAuthVerify.path} element={<MFAVerify />} />

      <Route path="*" element={<NotFound />} />
    </Routes>
  );
};

export default AppRouter;
