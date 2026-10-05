import {
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

import { INotification, NotificationType } from 'models/Navbar';
import { cn, formatDateAndTime } from 'utils/Helper';

interface NotificationDropdownCardProps {
  notification: INotification;
  onMarkAsRead: (id: string) => void | Promise<boolean | void>;
}

const getNotificationIcon = (type: NotificationType) => {
  const iconClass = 'home-text-2xl home-text-green-500';

  switch (type) {
    case NotificationType.NEW_USER_REGISTERED:
      return <UserPlus className={iconClass} />;
    case NotificationType.WELCOME_EMAIL:
      return <Mail className={iconClass} />;
    case NotificationType.USER_PROFILE_UPDATED:
      return <UserCog className={iconClass} />;
    case NotificationType.PASSWORD_RESET_REQUEST:
      return <LockOpen className={iconClass} />;
    case NotificationType.USER_SUSPENDED:
      return <UserX className={iconClass} />;
    case NotificationType.USER_PASSWORD_CHANGE:
      return <KeyRound className={iconClass} />;
    case NotificationType.BULK_USER_IMPORT_SUMMARY:
      return <Users className={iconClass} />;
    case NotificationType.NEW_COURSE_CREATED:
      return <BookPlus className={iconClass} />;
    case NotificationType.COURSE_UPDATED:
      return <BookCheck className={iconClass} />;
    case NotificationType.COURSE_ASSIGNED:
      return <BookOpenCheck className={iconClass} />;
    case NotificationType.NEW_PACKAGE_CREATED:
      return <PackagePlus className={iconClass} />;
    case NotificationType.PACKAGE_ASSIGNED:
      return <PackageCheck className={iconClass} />;
    default:
      return <Info className="home-text-2xl home-text-blue-500" />;
  }
};

const NotificationDropdownCard = ({
  notification,
  onMarkAsRead,
}: NotificationDropdownCardProps) => {
  return (
    <div
      className={cn(
        'home-relative home-border-b home-border-graphite home-p-4 home-transition-all hover:home-bg-[#42556740]',
      )}
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
          <div className="home-mt-2 home-flex home-items-center home-justify-between">
            <span className="home-text-xs home-text-white/50">
              {formatDateAndTime(notification.createdAt ?? '')}
            </span>
            {!notification.read && (
              <button
                onClick={() => onMarkAsRead(notification.id)}
                className="home-text-xs home-text-[#13cd9c] home-transition-colors hover:home-text-[#0fb58a]"
              >
                Mark as read
              </button>
            )}
          </div>
        </div>
      </div>
      {!notification.read && (
        <div className="home-absolute home-right-5 home-top-5 home-size-2 home--translate-y-1/2 home-rounded-full home-bg-green-500"></div>
      )}
    </div>
  );
};

export default NotificationDropdownCard;
