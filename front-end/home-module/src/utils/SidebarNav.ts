import { INavigationItem } from 'models/SideMenu';

const normalizePath = (url: string): string => url.replace(/\/+$/, '') || '/';

const getSectionRoot = (url: string): string => {
  const segment = normalizePath(url).split('/').filter(Boolean)[0];
  return segment ? `/${segment}` : '';
};

const isSameSection = (menuUrl: string, pathname: string): boolean => {
  const menuRoot = getSectionRoot(menuUrl);
  const pathRoot = getSectionRoot(pathname);
  return Boolean(menuRoot && menuRoot === pathRoot);
};

const belongsToMenuSection = (
  item: INavigationItem,
  pathname: string,
): boolean =>
  isSameSection(item.url, pathname) ||
  item.subItems.some(sub => isSameSection(sub.url, pathname));

/**
 * Maps create/edit URLs (not in the sidebar) back to their list/library page
 * so the related menu item stays active.
 */
const resolveMenuPathname = (pathname: string): string =>
  pathname
    .replace(/\/create-landing-page\/?$/, '/landing-pages')
    .replace(/\/edit-landing-page(?:\/[^/]+)?\/?$/, '/landing-pages')
    .replace(/\/create-sms-template\/?$/, '/sms-template-library')
    .replace(/\/edit-sms-template(?:\/[^/]+)?\/?$/, '/sms-template-library')
    .replace(/\/create-template\/?$/, '/template-library')
    .replace(/\/edit-template(?:\/[^/]+)?\/?$/, '/template-library')
    .replace(/\/create-campaign\/?$/, '/campaigns')
    .replace(/\/edit-campaign(?:\/[^/]+)?\/?$/, '/campaigns')
    .replace(/\/create-simulation\/?$/, '/campaigns')
    .replace(/\/edit-simulation(?:\/[^/]+)?\/?$/, '/campaigns');

export const isRouteActive = (menuUrl: string, pathname: string): boolean => {
  if (!menuUrl) return false;

  const menu = normalizePath(menuUrl);
  const path = normalizePath(resolveMenuPathname(pathname));

  if (menu === '/') return path === '/';
  return path === menu || path.startsWith(`${menu}/`);
};

export const findActiveParentName = (
  items: INavigationItem[],
  pathname: string,
  parentName: string | null = null,
): string | null => {
  for (const item of items) {
    if (item.subItems.length > 0) {
      const foundInChildren = findActiveParentName(
        item.subItems,
        pathname,
        item.name,
      );
      if (foundInChildren) return foundInChildren;
      if (isRouteActive(item.url, pathname) || belongsToMenuSection(item, pathname)) {
        return item.name;
      }
    } else if (isRouteActive(item.url, pathname)) {
      return parentName;
    }
  }
  return null;
};

export const hasActiveDescendant = (
  item: INavigationItem,
  pathname: string,
): boolean => {
  if (item.subItems.length === 0) {
    return isRouteActive(item.url, pathname);
  }
  return (
    item.subItems.some(sub => hasActiveDescendant(sub, pathname)) ||
    belongsToMenuSection(item, pathname)
  );
};
