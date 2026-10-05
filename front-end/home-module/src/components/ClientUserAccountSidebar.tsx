import { NavLink } from 'react-router-dom';
import { ClientUserRoutes } from 'routes/ClientUserRoutes';
import { cn } from 'utils/Helper';

interface IProps {
  hostPath: typeof ClientUserRoutes;
}

const ClientUserAccountSidebar = ({ hostPath }: IProps) => {
  return (
    <nav>
      <ul className="home-m-0 home-mt-2 home-p-0">
        <li className="home-mb-1 home-block">
          <NavLink
            to={hostPath.accountProfile.path}
            className={({ isActive }) =>
              cn(
                'home-block home-rounded home-px-4 home-py-2 home-text-base home-transition home-duration-300',
                isActive
                  ? 'home-bg-graphite home-text-white'
                  : 'hover:bg-graphite hover:text-white home-text-cloudy-white',
              )
            }
          >
            My Profile
          </NavLink>
        </li>
        <li className="home-mb-1 home-block">
          <NavLink
            to={hostPath.accountProfileSettings.path}
            className={({ isActive }) =>
              cn(
                'home-block home-rounded home-px-4 home-py-2 home-text-base home-transition home-duration-300',
                isActive
                  ? 'home-bg-graphite home-text-white'
                  : 'hover:bg-graphite hover:text-white home-text-cloudy-white',
              )
            }
          >
            Profile Setting
          </NavLink>
        </li>
        <li className="home-mb-1 home-block">
          <NavLink
            to={hostPath.accountSecurity.path}
            className={({ isActive }) =>
              cn(
                'home-block home-rounded home-px-4 home-py-2 home-text-base home-transition home-duration-300',
                isActive
                  ? 'home-bg-graphite home-text-white'
                  : 'hover:bg-graphite hover:text-white home-text-cloudy-white',
              )
            }
          >
            Account Security
          </NavLink>
        </li>
      </ul>
    </nav>
  );
};

export default ClientUserAccountSidebar;
