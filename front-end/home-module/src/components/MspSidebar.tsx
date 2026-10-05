import { ChevronRight } from 'lucide-react';
import { useState } from 'react';
import { NavLink } from 'react-router-dom';

import { GetMenuIcon } from 'components/SidebarMenuIcons';
import useStore from 'hooks/UseStore';
import { INavigationItem } from 'models/SideMenu';
import { cn } from 'utils/Helper';

interface IProps {
  className?: string;
}

const MspSidebar = ({ className }: IProps) => {
  const [expandedItems, setExpandedItems] = useState<Array<string>>([]);
  const { menus } = useStore();

  const toggleExpanded = (itemName: string) => {
    setExpandedItems(prev =>
      prev.includes(itemName)
        ? prev.filter(name => name !== itemName)
        : [...prev, itemName],
    );
  };

  const renderNavigationItem = (item: INavigationItem, level = 0) => {
    const hasSubItems = item.subItems.length !== 0,
      isExpanded = expandedItems.includes(item.name);

    const paddingLeft =
      level === 0 ? 'home-pl-2' : level === 1 ? 'home-pl-6' : 'home-pl-10';

    if (hasSubItems) {
      return (
        <li key={item.name}>
          <button
            onClick={() => toggleExpanded(item.name)}
            className={cn(
              'home-group home-flex home-w-full home-items-center home-gap-x-3 home-rounded-md home-p-2 home-text-left home-leading-6 home-transition-colors',
              'home-text-[#8E9499] hover:home-bg-graphite hover:home-text-white',
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
          {isExpanded && (
            <ul className="home-mt-1 home-space-y-1">
              {item.subItems?.map(subItem =>
                renderNavigationItem(subItem, level + 1),
              )}
            </ul>
          )}
        </li>
      );
    }

    return (
      <li key={item.name}>
        <NavLink
          to={item.url}
          className={({ isActive }) =>
            cn(
              isActive
                ? 'home-bg-primary home-text-white'
                : 'home-text-[#8E9499] hover:home-bg-graphite hover:home-text-white',
              'home-group home-flex home-gap-x-3 home-rounded-md home-p-2 home-leading-6 home-transition-colors',
              paddingLeft,
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

export default MspSidebar;
