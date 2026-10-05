import { Dispatch, SetStateAction, useEffect, useRef, useState } from 'react';
import { GrCaretDown } from 'react-icons/gr';
import { IoLogOutOutline } from 'react-icons/io5';
import { Link, useNavigate } from 'react-router-dom';
import { Bell, CircleQuestionMark } from 'lucide-react';

import { BarIcon } from 'assets/icons';
import { Button } from 'common/Button';
import useAPI from 'hooks/UseAPI';
import useAuth from 'hooks/UseAuth';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { AspireAdminRoutes } from 'routes/AspireAdminRoutes';
import { routes } from 'routes/Route';
import { FILE_PATH_PREFIX, PUBLIC_URL } from 'utils/Constants';
import { cn, getDeviceInfo } from 'utils/Helper';
import useStore from 'hooks/UseStore';

const Logo = PUBLIC_URL + '/images/logo.svg';
const ProfileImage = PUBLIC_URL + '/images/profile.jpg';
const LanguageFlag = PUBLIC_URL + '/images/usa-flag.png';

interface IProps {
  hostPath: typeof AspireAdminRoutes;
  setShowSidebar: Dispatch<SetStateAction<boolean>>;
}

const AspireAdminNavbar = ({ hostPath, setShowSidebar }: IProps) => {
  const navigate = useNavigate();
  const { logout } = useAuth();
  const apiClient = useAPI();
  const { userInfo } = useStore();

  const dropdownRef = useRef<HTMLDivElement>(null);
  const [dropdownVisible, setDropdownVisible] = useState<boolean>(false);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (
        dropdownRef.current &&
        !dropdownRef.current.contains(event.target as Node)
      ) {
        setDropdownVisible(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);

    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, []);

  const toggleDropdown = () => setDropdownVisible(prev => !prev);

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

  return (
    <header className="home-sticky home-top-0 home-z-40 home-flex home-w-full home-justify-between home-bg-deep-ocean home-px-4 home-py-3 home-text-white">
      <div className="home-flex home-items-center home-gap-8">
        <button onClick={toggleSidebar}>
          <BarIcon />
        </button>
        <Link to={hostPath.dashboard.path}>
          <img src={Logo} alt="Logo" />
        </Link>
      </div>
      <div className="home-flex home-items-center home-gap-6">
        <Bell className="home-text-2xl home-text-[#9a9a9a] hover:home-text-cloudy-white" />
        <Link
          to="https://securityawarenesstraining.ai/company/contact"
          target="_blank"
          rel="noopener noreferrer"
          title="Help"
        >
          <CircleQuestionMark className="home-text-2xl home-text-[#9a9a9a] hover:home-text-cloudy-white" />
        </Link>
        <img
          className="home-size-7 home-rounded-full"
          src={LanguageFlag}
          alt="Language"
        />
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
              {userInfo?.fullName}
            </p>
            <GrCaretDown
              className={cn(
                'home-text-xs home-text-cloudy-white home-transition-all home-duration-200 home-ease-in-out',
                dropdownVisible ? 'home-rotate-180' : 'home-rotate-0',
              )}
            />
          </div>
          {dropdownVisible && (
            <div className="home-absolute home-right-5 home-top-16 home-z-50 home-w-80 home-rounded-b home-bg-deep-ocean home-p-2 home-shadow-soft-shadow">
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
                {/* <Button
                  variant="ghost"
                  className="home-gap-4 !home-justify-start !home-text-base"
                >
                  <MenuProfileIcon className="!home-size-6" /> Profile
                </Button>
                <Button
                  variant="ghost"
                  className="home-gap-4 !home-justify-start !home-text-base"
                >
                  <IoHelpCircleOutline className="!home-size-6" /> Help
                </Button> */}
                <Button
                  variant="ghost"
                  className="!home-justify-start home-gap-4 !home-text-base"
                  onClick={handleClickSignOut}
                >
                  <IoLogOutOutline className="!home-size-6" /> Log out
                </Button>
              </div>
            </div>
          )}
        </div>
      </div>
    </header>
  );
};

export default AspireAdminNavbar;
