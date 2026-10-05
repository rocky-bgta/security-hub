import {
  Bell,
  BookCheck,
  BookOpenCheck,
  BookPlus,
  CircleQuestionMark,
  Info,
  KeyRound,
  LockOpen,
  LogOut,
  Mail,
  PackageCheck,
  PackagePlus,
  ShoppingCart,
  UserCog,
  UserPlus,
  UserX,
  Users,
} from 'lucide-react';
import {
  Dispatch,
  SetStateAction,
  useCallback,
  useEffect,
  useRef,
  useState,
} from 'react';
import { GrCaretDown } from 'react-icons/gr';
import { IoMdNotificationsOutline } from 'react-icons/io';
import { Link, useNavigate } from 'react-router-dom';

import { BarIcon } from 'assets/icons';
import { Button } from 'common/Button';
import useAPI from 'hooks/UseAPI';
import useAuth from 'hooks/UseAuth';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { MspRoutes } from 'routes/MspRoutes';
import { routes } from 'routes/Route';
import { FILE_PATH_PREFIX, PUBLIC_URL } from 'utils/Constants';
import {
  cn,
  formatDateAndTime,
  getDeviceInfo,
  isSuccessResponse,
  objectToQueryString,
} from 'utils/Helper';
import useStore from 'hooks/UseStore';
import { IList } from 'models/Global';
import { INotification, NotificationType } from 'models/Navbar';
import { ClientUserRoutes } from 'routes/ClientUserRoutes';

const Logo = PUBLIC_URL + '/images/logo.svg';
const ProfileImage = PUBLIC_URL + '/images/profile.jpg';
const LanguageFlag = PUBLIC_URL + '/images/usa-flag.png';

interface IProps {
  hostPath: typeof MspRoutes;
  setShowSidebar: Dispatch<SetStateAction<boolean>>;
}

const MspNavbar = ({ hostPath, setShowSidebar }: IProps) => {
  const navigate = useNavigate();
  const { role, logout } = useAuth();
  const { userInfo, brandingInfo } = useStore();
  const apiClient = useAPI();

  const dropdownRef = useRef<HTMLDivElement>(null);
  const notificationRef = useRef<HTMLDivElement>(null);
  const [dropdownVisible, setDropdownVisible] = useState<boolean>(false);
  const [notificationVisible, setNotificationVisible] =
    useState<boolean>(false);
  const [notifications, setNotifications] = useState<IList<INotification>>({
    offset: 0,
    pageSize: 10,
    total: 0,
    items: [],
  });
  const [notificationCount, setNotificationCount] = useState<number>(0);

  const getNotifications = useCallback(async () => {
    try {
      const response = await apiClient.get(
        API_END_POINTS.GET_USER_NOTIFICATIONS +
        objectToQueryString({ offset: 0, pageSize: 20 }),
      );

      setNotifications({
        offset: response.data.offset,
        pageSize: response.data.pageSize,
        total: response.data.total,
        items: response.data.items.filter(
          (notification: INotification) => !notification.read,
        ),
      });
    } catch (error) {
      console.error(error);
    }
  }, [apiClient]);

  const getNotificationCount = useCallback(async () => {
    try {
      const response = await apiClient.get(
        API_END_POINTS.GET_USER_NOTIFICATION_COUNT,
      );
      setNotificationCount(response.data);
    } catch (error) {
      console.error(error);
    }
  }, [apiClient]);

  useEffect(() => {
    getNotificationCount();
    getNotifications();
  }, [getNotificationCount, getNotifications]);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (
        dropdownRef.current &&
        !dropdownRef.current.contains(event.target as Node)
      ) {
        setDropdownVisible(false);
      }
      if (
        notificationRef.current &&
        !notificationRef.current.contains(event.target as Node)
      ) {
        setNotificationVisible(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);

    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, []);

  const toggleDropdown = () => setDropdownVisible(prev => !prev);
  const toggleNotification = () => setNotificationVisible(prev => !prev);
  const toggleSidebar = () => setShowSidebar(prev => !prev);

  const handleClickSignOut = async () => {
    try {
      const payload = {
        deviceInfo: getDeviceInfo(),
      };
      await apiClient.post(API_END_POINTS.LOGOUT, { data: payload });
    } catch (error) {
      console.error('Error during logout:', error);
    } finally {
      logout();
      navigate(routes.login.path, { replace: true });
    }
  };

  const markAsRead = async (id: string) => {
    try {
      const response = await apiClient.put(
        API_END_POINTS.MARK_NOTIFICATION_AS_READ.replace(':id', id),
      );
      if (isSuccessResponse(response.statusCode)) {
        getNotifications();
        getNotificationCount();
      }
    } catch (error) {
      console.error(error);
    }
  };

  const markAllAsRead = async () => {
    try {
      const response = await apiClient.put(
        API_END_POINTS.MARK_ALL_NOTIFICATIONS_AS_READ,
      );
      if (isSuccessResponse(response.statusCode)) {
        getNotifications();
        getNotificationCount();
      }
    } catch (error) {
      console.error(error);
    }
  };

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

  const handleClickBuy = () => {
    navigate(routes.buyProduct.path);
  };

  return (
    <header className="home-sticky home-top-0 home-z-40 home-flex home-w-full home-justify-between home-bg-deep-ocean home-px-4 home-py-3 home-text-white">
      <div className="home-flex home-items-center home-gap-4">
        <button onClick={toggleSidebar}>
          <BarIcon />
        </button>
        <Link to={hostPath.dashboard.path}>
          <img
            src={
              brandingInfo.logoFilePath
                ? FILE_PATH_PREFIX + brandingInfo.logoFilePath
                : Logo
            }
            alt="Logo"
            className="home-h-auto home-max-h-[40px]"
          />
        </Link>
        <p className="home-text-lg home-font-semibold home-text-cloudy-white">
          {brandingInfo.companyName}
        </p>
      </div>
      <div className="home-flex home-items-center home-gap-7">
        <Button size="sm" variant="default" onClick={handleClickBuy}>
          <ShoppingCart /> Buy Products
        </Button>

        {/* Notification Icon with Badge */}
        <div className="home-relative" ref={notificationRef}>
          <button
            className="home-relative home-flex home-items-center home-justify-center"
            onClick={toggleNotification}
          >
            <Bell className="home-text-2xl home-text-[#9a9a9a] hover:home-text-cloudy-white" />
            {notificationCount > 0 && (
              <span className="home-absolute home--right-1 home--top-1 home-flex home-size-4 home-items-center home-justify-center home-rounded-full home-bg-vibrant-red home-text-[10px] home-font-semibold home-text-white">
                {notificationCount}
              </span>
            )}
          </button>

          {/* Notification Dropdown */}
          {notificationVisible && (
            <div className="home-absolute home-right-0 home-top-12 home-z-50 home-w-96 home-rounded-lg home-border home-border-graphite home-bg-deep-ocean home-shadow-2xl">
              {/* Header */}
              <div className="home-flex home-items-center home-justify-between home-border-b home-border-graphite home-p-4">
                <h3 className="home-text-lg home-font-semibold home-text-white">
                  Notifications
                </h3>
                {notificationCount > 0 && (
                  <button
                    onClick={markAllAsRead}
                    className="home-text-sm home-text-[#13cd9c] home-transition-colors hover:home-text-[#0fb58a]"
                  >
                    Mark all as read
                  </button>
                )}
              </div>

              {/* Notification List */}
              <div className="home-max-h-[500px] home-overflow-y-auto">
                {notifications.items.length === 0 ? (
                  <div className="home-flex home-flex-col home-items-center home-justify-center home-py-12">
                    <IoMdNotificationsOutline className="home-mb-3 home-text-5xl home-text-graphite" />
                    <p className="home-text-cloudy-white">No notifications</p>
                  </div>
                ) : (
                  notifications.items.map(notification => (
                    <div
                      key={notification.id}
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
                                onClick={() => markAsRead(notification.id)}
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
                  ))
                )}
              </div>

              {/* Footer */}
              {notifications.items.length > 0 && (
                <div className="home-border-t home-border-graphite home-p-3">
                  <Link
                    to={ClientUserRoutes.notification.path}
                    className="home-block home-w-full home-rounded-lg home-py-2 home-text-center home-text-sm home-text-[#13cd9c] home-transition-colors hover:home-bg-[#42556740]"
                  >
                    View all notifications
                  </Link>
                </div>
              )}
            </div>
          )}
        </div>
        <Link
          to="https://securityawarenesstraining.ai/company/contact"
          target="_blank"
          rel="noopener noreferrer"
          title="Help"
        >
          <CircleQuestionMark className="home-text-2xl home-text-[#9a9a9a] hover:home-text-cloudy-white" />
        </Link>
        <div className="home-flex home-items-center home-gap-1">
          <img
            className="home-size-6 home-rounded-full"
            src={LanguageFlag}
            alt="Language"
          />
          <p className="home-text-sm home-text-cloudy-white">English</p>
        </div>
        <div onClick={toggleDropdown} ref={dropdownRef}>
          <div className="home-flex home-cursor-pointer home-items-center home-gap-1.5">
            <img
              src={
                userInfo?.profilePicture
                  ? FILE_PATH_PREFIX + userInfo.profilePicture
                  : ProfileImage
              }
              alt="Profile Image"
              className="home-size-9 home-rounded-full"
            />
            <p className="home-text-sm home-capitalize home-text-cloudy-white">
              {userInfo?.fullName
                ? userInfo.fullName
                : role.split('_').join(' ').toLowerCase() || 'Admin'}
            </p>
            <GrCaretDown
              className={cn(
                'home-text-xs home-text-cloudy-white home-transition-all home-duration-200 home-ease-in-out',
                dropdownVisible ? 'home-rotate-180' : 'home-rotate-0',
              )}
            />
          </div>
          {dropdownVisible && (
            <div className="home-absolute home-right-2 home-top-16 home-z-50 home-min-w-80 home-rounded-b home-bg-deep-ocean home-p-2 home-shadow-soft-shadow">
              <div className="home-flex home-items-center home-gap-5 home-rounded-lg home-bg-[#42556740] home-p-3">
                <img
                  className="home-size-14 home-rounded-full"
                  src={
                    userInfo?.profilePicture
                      ? FILE_PATH_PREFIX + userInfo.profilePicture
                      : ProfileImage
                  }
                  alt="Profile Image"
                />
                <div>
                  <h3 className="home-break-all home-font-semibold home-text-white">
                    {userInfo?.fullName}
                  </h3>
                  <p className="home-break-all home-text-cloudy-white">
                    {userInfo?.email}
                  </p>
                </div>
              </div>

              <div className="home-my-2 home-flex home-flex-col home-gap-y-2">
                <Button
                  variant="ghost"
                  className="!home-justify-start home-gap-4 !home-text-base"
                  onClick={handleClickSignOut}
                >
                  <LogOut className="!home-size-6" /> Log out
                </Button>
              </div>
            </div>
          )}
        </div>
      </div>
    </header>
  );
};

export default MspNavbar;
