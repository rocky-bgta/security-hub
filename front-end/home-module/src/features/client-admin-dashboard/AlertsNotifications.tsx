import {
  Bell,
  BookCheck,
  BookOpenCheck,
  BookPlus,
  Info,
  KeyRound,
  LockOpen,
  Mail,
  PackageCheck,
  PackagePlus,
  UserCog,
  UserPlus,
  UserX,
  Users,
} from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import useAPI from 'hooks/UseAPI';
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { INotification, NotificationType } from 'models/Navbar';
import { IList } from 'models/Global';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { ClientUserRoutes } from 'routes/ClientUserRoutes';
import {
  cn,
  formatDateAndTime,
  isSuccessResponse,
  objectToQueryString,
} from 'utils/Helper';
import Loader from 'common/loader/Loader';

const actionButtonClass =
  'home-text-xs home-text-green-500 home-transition-colors hover:home-text-[#0fb58a]';

const AlertsNotifications = () => {
  const apiClient = useAPI();
  const navigate = useNavigate();
  const [notifications, setNotifications] = useState<IList<INotification>>({
    offset: 0,
    pageSize: 10,
    total: 0,
    items: [],
  });
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    getNotifications();
  }, []);

  const getNotifications = async () => {
    try {
      setIsLoading(true);
      const response = await apiClient.get(
        API_END_POINTS.GET_USER_NOTIFICATIONS +
          objectToQueryString({
            offset: 0,
            pageSize: 10,
          }),
      );
      if (!isSuccessResponse(response.statusCode)) {
        throw new Error(response.message);
      }
      setNotifications(response.data);
    } catch (error) {
      console.error(error);
    } finally {
      setIsLoading(false);
    }
  };

  const getNotificationIcon = (type: NotificationType) => {
    const iconClass = 'home-text-2xl home-text-green-500';

    switch (type) {
      case NotificationType.NEW_USER_REGISTERED:
        return <UserPlus className={iconClass} />; // New user added
      case NotificationType.WELCOME_EMAIL:
        return <Mail className={iconClass} />; // Welcome message/email
      case NotificationType.USER_PROFILE_UPDATED:
        return <UserCog className={iconClass} />; // Profile updated
      case NotificationType.PASSWORD_RESET_REQUEST:
        return <LockOpen className={iconClass} />; // Password reset request
      case NotificationType.USER_SUSPENDED:
        return <UserX className={iconClass} />; // User suspended
      case NotificationType.USER_PASSWORD_CHANGE:
        return <KeyRound className={iconClass} />; // Password changed
      case NotificationType.BULK_USER_IMPORT_SUMMARY:
        return <Users className={iconClass} />; // Bulk import summary
      case NotificationType.NEW_COURSE_CREATED:
        return <BookPlus className={iconClass} />; // New course created
      case NotificationType.COURSE_UPDATED:
        return <BookCheck className={iconClass} />; // Course updated
      case NotificationType.COURSE_ASSIGNED:
        return <BookOpenCheck className={iconClass} />; // Course assigned
      case NotificationType.NEW_PACKAGE_CREATED:
        return <PackagePlus className={iconClass} />; // New package created
      case NotificationType.PACKAGE_ASSIGNED:
        return <PackageCheck className={iconClass} />; // Package assigned
      default:
        return <Info className="home-text-2xl home-text-blue-500" />; // Default info
    }
  };

  const markAsRead = async (id: string) => {
    try {
      const response = await apiClient.put(
        API_END_POINTS.MARK_NOTIFICATION_AS_READ.replace(':id', id),
      );
      if (!isSuccessResponse(response.statusCode)) {
        throw new Error(response.message);
      }
      setNotifications(prev => ({
        ...prev,
        items: prev.items.map(notification =>
          notification.id === id
            ? { ...notification, read: true }
            : notification,
        ),
      }));
    } catch (error) {
      console.error(error);
    }
  };

  const viewNotification = () => {
    navigate(ClientUserRoutes.notification.path);
  };

  const renderNotificationAction = (notification: INotification) => {
    if (!notification.read) {
      return (
        <button
          onClick={() => markAsRead(notification.id)}
          className={actionButtonClass}
        >
          Mark as read
        </button>
      );
    }

    return (
      <button onClick={viewNotification} className={actionButtonClass}>
        View Notification
      </button>
    );
  };

  return (
    <Card className="home-h-full">
      <CardHeader>
        <CardTitle className="home-text-xl home-font-semibold home-text-white">
          Alerts & Notifications
        </CardTitle>
        {/* <CardDescription className="home-text-cloudy-white">
          Recent system alerts
        </CardDescription> */}
      </CardHeader>

      <CardContent className="home-space-y-6">
        <div className="home-max-h-[320px] home-overflow-y-auto home-pr-1">
          {isLoading ? (
            <Loader mode="container" />
          ) : notifications.items.length === 0 ? (
            <div className="home-flex home-h-[320px] home-flex-col home-items-center home-justify-center">
              <Bell className="home-mb-3 home-size-10 home-text-cloudy-white" />
              <p className="home-text-cloudy-white">No notifications</p>
            </div>
          ) : (
            notifications.items.map(notification => (
              <div
                key={notification.id}
                className="home-relative home-border-b home-border-graphite home-py-4 home-transition-all hover:home-bg-[#42556740]"
              >
                <div className="home-flex home-gap-3">
                  <div className="home-mt-1 home-shrink-0">
                    {getNotificationIcon(notification.notificationType)}
                  </div>
                  <div className="home-min-w-0 home-flex-1">
                    <div className="home-flex home-items-start home-justify-between home-gap-2">
                      <h4
                        className={cn(
                          'home-text-sm home-font-semibold',
                          !notification.read
                            ? 'home-text-green-500'
                            : 'home-text-white',
                        )}
                      >
                        {notification.title}
                      </h4>
                    </div>
                    <p className="home-mt-1 home-line-clamp-2 home-text-sm home-text-cloudy-white">
                      {notification.message}
                    </p>
                    <div className="home-mt-2 home-flex home-flex-wrap home-items-center home-justify-between home-gap-2">
                      <span className="home-text-xs home-text-white/50">
                        {formatDateAndTime(notification.createdAt ?? '')}
                      </span>
                      {renderNotificationAction(notification)}
                    </div>
                  </div>
                </div>
                {!notification.read && (
                  <div className="home-absolute home-right-2 home-top-5 home-size-2 home--translate-y-1/2 home-rounded-full home-bg-green-500"></div>
                )}
              </div>
            ))
          )}
        </div>
        {/* <Alert>
          <AlertTriangle className="home-h-4 home-w-4" />
          <AlertDescription>
            License Expiry Alert: 24 licenses expiring soon
          </AlertDescription>
        </Alert>
        <Alert>
          <Users className="home-h-4 home-w-4" />
          <AlertDescription>
            Campaign Reminder: Active reminders for July push
          </AlertDescription>
        </Alert>
        <div className="home-mt-6 home-flex home-flex-wrap home-gap-2">
          <Badge variant="destructive">High Risk Users: 4</Badge>
          <Badge variant="secondary" className="home-bg-[#F7C948]">
            Expiring Soon: 24
          </Badge>
          <Badge
            variant="outline"
            className="home-border-transparent home-bg-transparent home-text-white"
          >
            Total Users: 274
          </Badge>
        </div> */}
      </CardContent>
    </Card>
  );
};

export default AlertsNotifications;
