import { Navigate, Route, Routes } from 'react-router-dom';

import ManageNotifications from 'pages/notification/ManageNotifications';
import NotificationAnalytics from 'pages/notification/NotificationAnalytics';
import NotificationScheduler from 'pages/notification/NotificationScheduler';
import NotificationTemplates from 'pages/notification/NotificationTemplates';
import EditNotificationTemplate from 'pages/notification/EditNotificationTemplate';
import SendNotification from 'pages/notification/SendNotification';
import { routes } from 'routes/AppRoutes';
import NotificationSettings from 'pages/account/NotificationSettings';
import RoleWiseNotificationSettings from 'pages/account/RoleWiseNotificationSettings';
import UserNotifications from 'pages/notification/UserNotifications';

const NotificationRouter = () => {
  return (
    <Routes>
      <Route index element={<Navigate to="send-notification" />} />

      <Route
        path={routes.sendNotification.path}
        element={<SendNotification />}
      />
      <Route
        path={routes.manageNotifications.path}
        element={<ManageNotifications />}
      />
      <Route
        path={routes.notificationScheduler.path}
        element={<NotificationScheduler />}
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
        path={routes.notificationAnalytics.path}
        element={<NotificationAnalytics />}
      />
      <Route
        path={routes.notificationSettings.path}
        element={<NotificationSettings />}
      />
      <Route
        path={routes.roleWiseNotificationSettings.path}
        element={<RoleWiseNotificationSettings />}
      />
      <Route path={routes.notification.path} element={<UserNotifications />} />
    </Routes>
  );
};

export default NotificationRouter;
