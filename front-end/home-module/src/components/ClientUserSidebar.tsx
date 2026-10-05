import { NavLink } from 'react-router-dom';

import { GetMenuIcon } from 'components/SidebarMenuIcons';
import useStore from 'hooks/UseStore';
import { INavigationItem } from 'models/SideMenu';
import { cn } from 'utils/Helper';

interface IProps {
  className?: string;
}

const ClientUserSidebar = ({ className }: IProps) => {
  const { menus } = useStore();

  const renderNavigationItem = (item: INavigationItem) => {
    return (
      <li key={item.name}>
        <NavLink
          to={item.url}
          className={({ isActive }) =>
            cn(
              isActive
                ? 'home-bg-primary home-text-white'
                : 'home-text-[#8E9499] hover:home-bg-white/25 hover:home-text-white',
              'home-group home-flex home-items-center home-gap-x-3 home-rounded-md home-p-2 home-pl-2 home-leading-6 home-transition-colors',
            )
          }
        >
          {GetMenuIcon(item.icon)}
          {item.name}
        </NavLink>
      </li>
    );
  };

  return (
    <nav className={cn('home-p-6', className)}>
      <ul className="-home-mx-2 home-space-y-1">
        {menus.map(item => renderNavigationItem(item))}
      </ul>
    </nav>
  );
};

export default ClientUserSidebar;
