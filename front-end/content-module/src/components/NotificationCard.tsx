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
import { NotificationType } from 'models/Notification';

import { cn } from 'utils/Helper';

interface NotificationCardProps {
  id: string;
  notificationType: NotificationType;
  title?: string;
  message: string;
  date: string;
  read: boolean;
  onMarkAsRead?: (id: string) => void;
  onClose?: (id: string) => void;
}

const NotificationCard = ({
  id,
  notificationType,
  title,
  message,
  date,
  read,
  onMarkAsRead,
  onClose,
}: NotificationCardProps) => {
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

  return (
    <div
      className={cn(
        'content-relative content-flex content-items-center content-gap-6 content-border-b content-border-card-border content-p-4 content-transition-all hover:content-bg-primary/10',
        !read && 'content-bg-primary/10',
      )}
    >
      <div className="content-shrink-0">
        {getNotificationIcon(notificationType)}
      </div>

      {/* Message Content */}
      <div className="content-min-w-0 content-flex-1">
        <div className="content-flex content-items-start content-justify-between content-gap-2">
          <div className="content-flex content-items-center content-gap-2">
            <p
              className={cn(
                'content-text-sm content-font-semibold',
                !read ? 'content-text-primary' : 'content-text-white',
              )}
            >
              {title}
            </p>
            {/* Unread Dot */}
            {!read && (
              <div className="content-size-2 content-rounded-full content-bg-primary" />
            )}
          </div>
          {!read && (
            <button
              onClick={() => onMarkAsRead?.(id)}
              className="content-text-sm content-text-primary content-transition-colors hover:content-text-primary/90"
            >
              Mark as read
            </button>
          )}
        </div>

        <p
          className={cn(
            'content-mt-1 content-line-clamp-2 content-text-sm',
            read ? 'content-text-gray-300' : 'content-text-gray-100',
          )}
        >
          {message}
        </p>

        <div className="content-mt-2">
          <span className="content-text-xs content-text-gray-400">{date}</span>
        </div>
      </div>
    </div>
  );
};

export default NotificationCard;
