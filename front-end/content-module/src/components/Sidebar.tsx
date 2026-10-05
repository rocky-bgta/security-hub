import {
  DropdownIcon,
  MenuBookmarkIcon,
  MenuCampaignIcon,
  MenuCertificateIcon,
  MenuDashboardIcon,
  MenuLibraryIcon,
  MenuProfileIcon,
} from 'assets/icons';
import clsx from 'clsx';
import {
  cloneElement,
  Fragment,
  ReactElement,
  useEffect,
  useState,
} from 'react';
import { Link, useLocation } from 'react-router-dom';

interface MenuItem {
  path?: string;
  title: string;
  icon?: ReactElement;
  type: 'link' | 'sub';
  active?: boolean;
  badgeType?: string;
  children?: MenuItem[];
}

export const MENUS: MenuItem[] = [
  {
    title: 'Dashboard',
    icon: <MenuDashboardIcon className="content-opacity-50" />,
    type: 'link',
    path: '/user/dashboard',
    active: false,
  },
  {
    title: 'Library',
    icon: <MenuLibraryIcon />,
    type: 'sub',
    badgeType: 'primary',
    active: false,
    children: [
      {
        path: '/user/my-library',
        title: 'Packages',
        type: 'link',
      },
      {
        path: '/user/all-courses',
        title: 'All Courses',
        type: 'link',
      },
    ],
  },
  {
    title: 'Campaigns',
    icon: <MenuCampaignIcon className="content-opacity-50" />,
    type: 'link',
    path: '/user/dashboard',
    badgeType: 'primary',
    active: false,
  },
  {
    title: 'Bookmark',
    icon: <MenuBookmarkIcon className="content-opacity-50" />,
    type: 'link',
    path: '/user/bookmarks',
    badgeType: 'primary',
    active: false,
  },
  {
    title: 'Certification',
    icon: <MenuCertificateIcon className="content-opacity-50" />,
    type: 'link',
    path: '/user/certificate-list',
    badgeType: 'primary',
    active: false,
  },
  {
    title: 'Accounts',
    icon: <MenuProfileIcon className="content-opacity-50" />,
    type: 'link',
    path: '/user/profile',
    badgeType: 'primary',
    active: false,
  },
];

const Sidebar = () => {
  const location = useLocation();
  const pathname = location.pathname;
  const [menuItems, setMenuItems] = useState<MenuItem[]>(MENUS);

  const checkIsActive = (href: string): boolean => {
    return pathname === href;
  };

  useEffect(() => {
    const currentUrl = window.location.pathname;
    const updatedMenuItems = [...menuItems];

    updatedMenuItems.forEach((item: MenuItem) => {
      if (item.path === currentUrl) setNavActive(item);
      if (!item.children) return;

      item.children.forEach((subItem: MenuItem) => {
        if (subItem.path === currentUrl) setNavActive(subItem);
        if (!subItem.children) return;

        subItem.children.forEach((subSubItem: MenuItem) => {
          if (subSubItem.path === currentUrl) {
            setNavActive(subSubItem);
          }
        });
      });
    });
  }, []);

  const setNavActive = (item: MenuItem) => {
    const updatedMenuItems = [...menuItems];

    updatedMenuItems.forEach((menuItem: MenuItem) => {
      if (menuItem !== item) menuItem.active = false;

      if (menuItem.children && menuItem.children.includes(item)) {
        menuItem.active = true;
      }

      if (menuItem.children) {
        menuItem.children.forEach((submenuItem: MenuItem) => {
          if (submenuItem.children && submenuItem.children.includes(item)) {
            menuItem.active = true;
            submenuItem.active = true;
          }
        });
      }
    });

    item.active = !item.active;
    setMenuItems(updatedMenuItems);
  };

  const toggleNavActive = (item: MenuItem) => {
    const updatedMenuItems = [...menuItems];

    if (!item.active) {
      updatedMenuItems.forEach((menuItem: MenuItem) => {
        if (updatedMenuItems.includes(item)) menuItem.active = false;
        if (!menuItem.children) return;

        menuItem.children.forEach((subItem: MenuItem) => {
          if (menuItem.children?.includes(item)) {
            subItem.active = false;
          }

          if (!subItem.children) return;

          subItem.children.forEach((subSubItem: MenuItem) => {
            if (subItem.children?.includes(item)) {
              subSubItem.active = false;
            }
          });
        });
      });
    }

    item.active = !item.active;
    setMenuItems(updatedMenuItems);
  };

  return (
    <Fragment>
      <div className="content-h-full content-p-6">
        <div className="content-h-full content-overflow-y-auto">
          <ul className="content-m-0 content-p-0">
            {menuItems.map((menuItem: MenuItem, i: number) => (
              <li
                className={clsx(
                  'content-relative content-mb-4',
                  menuItem.active && 'content-text-white',
                )}
                key={i}
              >
                {menuItem.type === 'sub' && (
                  <button
                    className="content-flex content-w-full content-items-center content-justify-between content-text-white content-opacity-50 content-transition-colors"
                    onClick={() => toggleNavActive(menuItem)}
                  >
                    <span className="content-flex content-items-center">
                      {menuItem.icon && (
                        <span className="content-mr-2">
                          {cloneElement(menuItem.icon as ReactElement<any>, {
                            className: clsx(
                              menuItem.active && 'content-active-menu-icon',
                            ),
                          })}
                        </span>
                      )}
                      {menuItem.title}
                    </span>
                    <span
                      className={clsx(
                        'content-transform content-transition-transform',
                        menuItem.active && 'content-rotate-180',
                      )}
                    >
                      <DropdownIcon className="content-opacity-50" />
                    </span>
                  </button>
                )}
                {menuItem.type === 'link' && (
                  <Link
                    to={menuItem.path || '#'}
                    className={clsx(
                      'content-flex content-items-center content-text-white content-transition-colors',
                      checkIsActive(menuItem.path || '')
                        ? 'content-opacity-100'
                        : 'content-opacity-50',
                    )}
                  >
                    <span className="content-flex content-items-center">
                      {menuItem.icon && (
                        <span className="content-mr-2">
                          {cloneElement(menuItem.icon as ReactElement<any>, {
                            className: clsx(
                              checkIsActive(menuItem.path || '') &&
                                'content-active-menu-icon',
                            ),
                          })}
                        </span>
                      )}
                      {menuItem.title}
                    </span>
                    {menuItem.children && (
                      <span className="content-ml-auto">
                        <DropdownIcon className="content-opacity-50" />
                      </span>
                    )}
                  </Link>
                )}
                {menuItem.children && (
                  <ul
                    className={clsx(
                      'content-overflow-hidden content-pl-4 content-transition-all content-duration-300',
                      menuItem.active
                        ? 'content-max-h-screen content-opacity-100'
                        : 'content-max-h-0 content-opacity-0',
                    )}
                  >
                    {menuItem.children.map(
                      (childrenItem: MenuItem, index: number) => (
                        <li key={index}>
                          {childrenItem.type === 'sub' && (
                            <button
                              onClick={() => toggleNavActive(childrenItem)}
                              className="content-flex content-w-full content-items-center content-justify-between content-px-6 content-py-2 content-text-white content-opacity-50 content-transition-colors"
                            >
                              {childrenItem.title}{' '}
                              <span
                                className={clsx(
                                  'content-transform content-transition-transform',
                                  childrenItem.active && 'content-rotate-180',
                                )}
                              >
                                <DropdownIcon className="content-opacity-50" />
                              </span>
                            </button>
                          )}
                          {childrenItem.type === 'link' && (
                            <Link
                              to={childrenItem.path || '#'}
                              className={clsx(
                                'content-block content-px-6 content-py-2 content-text-white content-transition-colors',
                                checkIsActive(childrenItem.path || '')
                                  ? 'content-opacity-100'
                                  : 'content-opacity-50',
                              )}
                            >
                              {childrenItem.title}{' '}
                            </Link>
                          )}
                          {childrenItem.children && (
                            <ul
                              className={clsx(
                                'content-overflow-hidden content-pl-4 content-transition-all content-duration-300',
                                childrenItem.active
                                  ? 'content-max-h-screen content-opacity-100'
                                  : 'content-max-h-0 content-opacity-0',
                              )}
                            >
                              {childrenItem.children.map(
                                (childrenSubItem: MenuItem, key: number) => (
                                  <li key={key}>
                                    {childrenSubItem.type === 'link' && (
                                      <Link
                                        to={childrenSubItem.path || '#'}
                                        className={clsx(
                                          'content-block content-px-6 content-py-2 content-text-white content-opacity-50 content-transition-colors',
                                          checkIsActive(
                                            childrenSubItem.path || '',
                                          ) &&
                                            'content-text-blue-600 dark:content-text-blue-300',
                                        )}
                                      >
                                        {childrenSubItem.title}
                                      </Link>
                                    )}
                                  </li>
                                ),
                              )}
                            </ul>
                          )}
                        </li>
                      ),
                    )}
                  </ul>
                )}
              </li>
            ))}
          </ul>
        </div>
      </div>
    </Fragment>
  );
};

export default Sidebar;
