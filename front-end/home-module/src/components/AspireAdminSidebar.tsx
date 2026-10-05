import { ChevronRight } from 'lucide-react';
import { ReactNode } from 'react';
import { NavLink, useLocation } from 'react-router-dom';

import { GetMenuIcon } from 'components/SidebarMenuIcons';
import useStore from 'hooks/UseStore';
import { useSidebarExpansion } from 'hooks/UseSidebarExpansion';
import { INavigationItem } from 'models/SideMenu';
import { cn } from 'utils/Helper';
import { hasActiveDescendant, isRouteActive } from 'utils/SidebarNav';

interface IProps {
  className?: string;
}

const AspireAdminSidebar = ({ className }: IProps) => {
  const { menus } = useStore();
  const { pathname } = useLocation();
  const { isItemExpanded, toggleExpanded } = useSidebarExpansion(
    menus,
    pathname,
  );

  const renderSubmenu = (children: ReactNode, isExpanded: boolean) => (
    <div
      className={cn(
        'home-grid home-transition-[grid-template-rows,opacity] home-duration-200 home-ease-in-out',
        isExpanded
          ? 'home-grid-rows-[1fr] home-opacity-100'
          : 'home-grid-rows-[0fr] home-opacity-0',
      )}
    >
      <ul className="home-mt-1 home-space-y-1 home-overflow-hidden">
        {children}
      </ul>
    </div>
  );

  const renderNavigationItem = (item: INavigationItem, level = 0) => {
    const hasSubItems = item.subItems.length !== 0;
    const isExpanded = isItemExpanded(item.name);
    const isChildActive = hasActiveDescendant(item, pathname);

    const paddingLeft =
      level === 0 ? 'home-pl-2' : level === 1 ? 'home-pl-6' : 'home-pl-10';

    if (hasSubItems) {
      return (
        <li key={item.name}>
          <button
            onClick={() => toggleExpanded(item.name)}
            className={cn(
              'home-group home-flex home-w-full home-items-center home-gap-x-3 home-rounded-md home-p-2 home-text-left home-leading-6 home-transition-colors',
              isChildActive
                ? 'home-text-white'
                : 'home-text-[#8E9499] hover:home-bg-white/25 hover:home-text-white',
              paddingLeft,
            )}
          >
            {GetMenuIcon(item.icon)}
            <span className="home-flex-1">{item.name}</span>

            <ChevronRight
              className={cn(
                'home-h-4 home-w-4 home-transition-all home-duration-200 home-ease-in-out',
                isExpanded ? 'home-rotate-90' : 'home-rotate-0',
              )}
            />
          </button>
          {renderSubmenu(
            item.subItems.map(subItem =>
              renderNavigationItem(subItem, level + 1),
            ),
            isExpanded,
          )}
        </li>
      );
    }

    return (
      <li key={item.name}>
        <NavLink
          to={item.url}
          className={() => {
            const isActive = isRouteActive(item.url, pathname);

            return cn(
              isActive
                ? level === 0
                  ? 'home-bg-primary home-text-white'
                  : 'home-bg-primary/20 home-font-medium home-text-primary'
                : 'home-text-[#8E9499] hover:home-bg-white/25 hover:home-text-white',
              'home-group home-flex home-items-center home-gap-x-3 home-rounded-md home-p-2 home-leading-6 home-transition-colors',
              paddingLeft,
            );
          }}
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

export default AspireAdminSidebar;
