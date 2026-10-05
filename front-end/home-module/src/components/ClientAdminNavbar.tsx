import {
  Bell,
  CircleQuestionMark,
  LogOut,
  ShoppingCart,
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
import { ClientAdminRoutes } from 'routes/ClientAdminRoutes';
import { routes } from 'routes/Route';
import { FILE_PATH_PREFIX, PUBLIC_URL } from 'utils/Constants';
import {
  cn,
  getDeviceInfo,
  objectToQueryString,
} from 'utils/Helper';
import useStore from 'hooks/UseStore';
import { IList } from 'models/Global';
import { INotification } from 'models/Navbar';
import { ClientUserRoutes } from 'routes/ClientUserRoutes';
import NotificationDropdownCard from 'components/NotificationDropdownCard';

const Logo = PUBLIC_URL + '/images/logo.svg';
const ProfileImage = PUBLIC_URL + '/images/profile.jpg';
const LanguageFlag = PUBLIC_URL + '/images/usa-flag.png';

interface IProps {
  hostPath: typeof ClientAdminRoutes;
  setShowSidebar: Dispatch<SetStateAction<boolean>>;
}

const ClientAdminNavbar = ({ hostPath, setShowSidebar }: IProps) => {
  const navigate = useNavigate();
  const { role, logout } = useAuth();
  const {
    userInfo,
    brandingInfo,
    notificationChangeKey,
    markNotificationAsRead,
    markAllNotificationsAsRead,
  } = useStore();
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
  }, [getNotificationCount, getNotifications, notificationChangeKey]);

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

  const handleClickBuy = () => {
    navigate(routes.buyProduct.path);
  };

  return (
    <header className="home-sticky home-top-0 home-z-40 home-flex home-w-full home-items-center home-justify-between home-gap-3 home-bg-deep-ocean home-p-3 home-text-white sm:home-px-4">
      <div className="home-flex home-min-w-0 home-items-center home-gap-3 sm:home-gap-4">
        <button onClick={toggleSidebar} aria-label="Toggle sidebar">
          <BarIcon />
        </button>
        <Link to={hostPath.dashboard.path} className="home-shrink-0">
          <img
            src={
              brandingInfo.logoFilePath
                ? FILE_PATH_PREFIX + brandingInfo.logoFilePath
                : Logo
            }
            alt="Logo"
            className="home-h-auto home-max-h-[32px] sm:home-max-h-[40px]"
          />
        </Link>
        <p className="home-hidden home-truncate home-text-lg home-font-semibold home-text-cloudy-white md:home-block">
          {brandingInfo.companyName}
        </p>
      </div>
      <div className="home-flex home-shrink-0 home-items-center home-gap-3 sm:home-gap-5 lg:home-gap-7">
        <Button size="sm" variant="default" onClick={handleClickBuy}>
          <ShoppingCart />
          <span className="home-hidden sm:home-inline">Buy Products</span>
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
            <div className="home-absolute home-right-0 home-top-12 home-z-50 home-w-[min(24rem,calc(100vw-1.5rem))] home-rounded-lg home-border home-border-graphite home-bg-deep-ocean home-shadow-2xl">
              {/* Header */}
              <div className="home-flex home-items-center home-justify-between home-border-b home-border-graphite home-p-4">
                <h3 className="home-text-lg home-font-semibold home-text-white">
                  Notifications
                </h3>
                {notificationCount > 0 && (
                  <button
                    onClick={markAllNotificationsAsRead}
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
                    <NotificationDropdownCard
                      key={notification.id}
                      notification={notification}
                      onMarkAsRead={markNotificationAsRead}
                    />
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
          className="home-hidden sm:home-inline-flex"
        >
          <CircleQuestionMark className="home-text-2xl home-text-[#9a9a9a] hover:home-text-cloudy-white" />
        </Link>
        <div className="home-hidden home-items-center home-gap-1 sm:home-flex">
          <img
            className="home-size-6 home-rounded-full"
            src={LanguageFlag}
            alt="Language"
          />
          <p className="home-hidden home-text-sm home-text-cloudy-white sm:home-block">
            English
          </p>
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
            <p className="home-hidden home-max-w-32 home-truncate home-text-sm home-capitalize home-text-cloudy-white md:home-block lg:home-max-w-none">
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
            <div className="home-absolute home-right-2 home-top-16 home-z-50 home-w-[min(20rem,calc(100vw-1rem))] home-rounded-b home-bg-deep-ocean home-p-2 home-shadow-soft-shadow">
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

export default ClientAdminNavbar;
