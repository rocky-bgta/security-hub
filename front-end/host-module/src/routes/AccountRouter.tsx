import { Route, Routes } from 'react-router-dom';

import AccountManagement from 'pages/account';
import ChangePassword from 'pages/account/ChangePassword';
import GlobalSettings from 'pages/account/GlobalSettings';
import NotificationPreferences from 'pages/account/NotificationPreferences';
import NotificationSettings from 'pages/account/NotificationSettings';
import PersonalInformation from 'pages/account/PersonalInformation';
import ProfileInformation from 'pages/account/ProfileInformation';
import SecuritySettings from 'pages/account/SecuritySettings';
import UserAccountSettings from 'pages/account/UserAccountSettings';
import { routes } from 'routes/AppRoutes';
import UpdatePassword from 'pages/account/UpdatePassword';
import BrandingManagement from 'pages/account/BrandingManagement';

const AccountRouter = () => {
  return (
    <Routes>
      <Route index element={<AccountManagement />} />

      <Route
        path={routes.profileInformation.path}
        element={<ProfileInformation />}
      />
      <Route path={routes.changePassword.path} element={<ChangePassword />} />
      <Route
        path={routes.notificationPreferences.path}
        element={<NotificationPreferences />}
      />

      <Route
        path={routes.personalInformation.path}
        element={<PersonalInformation />}
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
        path={routes.userAccountSettings.path}
        element={<UserAccountSettings />}
      />
      <Route path={routes.globalSettings.path} element={<GlobalSettings />} />
      <Route
        path={routes.accountSecurity.path}
        element={<UpdatePassword />}
      />
      <Route path={routes.branding.path} element={<BrandingManagement />} />
    </Routes>
  );
};

export default AccountRouter;
