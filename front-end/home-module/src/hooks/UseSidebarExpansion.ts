import { useMemo, useState } from 'react';

import { INavigationItem } from 'models/SideMenu';
import { findActiveParentName } from 'utils/SidebarNav';

export const useSidebarExpansion = (
  menus: INavigationItem[],
  pathname: string,
) => {
  const routeExpandedItem = useMemo(
    () => findActiveParentName(menus, pathname),
    [menus, pathname],
  );

  const [manualState, setManualState] = useState<{
    pathname: string;
    extraOpen: string | null;
    collapsedItems: string[];
  }>({
    pathname,
    extraOpen: null,
    collapsedItems: [],
  });

  const collapsedItems =
    manualState.pathname === pathname ? manualState.collapsedItems : [];
  const extraOpen =
    manualState.pathname === pathname ? manualState.extraOpen : null;

  const expandedItems = useMemo(() => {
    const items = new Set<string>();
    if (routeExpandedItem && !collapsedItems.includes(routeExpandedItem)) {
      items.add(routeExpandedItem);
    }
    if (extraOpen && !collapsedItems.includes(extraOpen)) {
      items.add(extraOpen);
    }
    return items;
  }, [routeExpandedItem, extraOpen, collapsedItems]);

  const isItemExpanded = (itemName: string) => expandedItems.has(itemName);

  const toggleExpanded = (itemName: string) => {
    setManualState(prev => {
      const collapsed =
        prev.pathname === pathname ? prev.collapsedItems : [];
      const extra = prev.pathname === pathname ? prev.extraOpen : null;
      const isOpen = expandedItems.has(itemName);

      if (isOpen) {
        return {
          pathname,
          extraOpen: extra === itemName ? null : extra,
          collapsedItems: collapsed.includes(itemName)
            ? collapsed
            : [...collapsed, itemName],
        };
      }

      return {
        pathname,
        extraOpen: routeExpandedItem === itemName ? extra : itemName,
        collapsedItems: collapsed.filter(name => name !== itemName),
      };
    });
  };

  return { isItemExpanded, toggleExpanded };
};
