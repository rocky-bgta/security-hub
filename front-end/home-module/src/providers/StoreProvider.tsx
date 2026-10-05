import { ReactNode, useCallback, useEffect, useRef, useState } from 'react';

import useAPI from 'hooks/UseAPI';
import useAuth from 'hooks/UseAuth';
import { IResponse, IUserInfo, StoreContext } from 'models/Context';
import { INavigationItem, ISideMenuResponse, MenuType } from 'models/SideMenu';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { IBrandingData, IBrandingResponse } from 'models/Branding';
import { isSuccessResponse } from 'utils/Helper';

const buildMenusFromRolePermission = (
  rolePermission: ISideMenuResponse,
  onboardBy?: string,
): Array<INavigationItem> => {
  if (!rolePermission?.userMenuResponses?.length) {
    return [];
  }

  let parentMenus = rolePermission.userMenuResponses
    .filter(menu => menu.menuType === MenuType.MAIN_MENU)
    .sort((a, b) => a.sequenceNumber - b.sequenceNumber);

  const childMenus = rolePermission.userMenuResponses
    .filter(menu => menu.menuType === MenuType.SUB_MENU)
    .sort((a, b) => a.sequenceNumber - b.sequenceNumber);

  if (rolePermission.roleName === 'USER') {
    if (!parentMenus[0]) {
      return [];
    }

    return [
      {
        name: parentMenus[0].name,
        icon: parentMenus[0].icon,
        url: parentMenus[0].url,
        subItems: [],
      },
      ...childMenus.map(menu => ({
        name: menu.name,
        icon: menu.icon,
        url: menu.url,
        subItems: [],
      })),
    ];
  }

  parentMenus.forEach(parent => {
    parent.subItems = childMenus.filter(
      child => child.parentMenuId === parent.id,
    );
  });

  if (onboardBy === 'TRIAL') {
    parentMenus = parentMenus.filter(
      menu => !menu.name.toLowerCase().includes('product management'),
    );
  }

  return parentMenus.map(menu => ({
    name: menu.name,
    icon: menu.icon,
    url: menu.url,
    subItems: menu.subItems.map(sub => ({
      name: sub.name,
      icon: sub.icon,
      url: sub.url,
      subItems: [],
    })),
  }));
};

const StoreProvider = ({ children }: { children: ReactNode }) => {
  const [userInfo, setUserInfo] = useState<IUserInfo>({} as IUserInfo);
  const [menus, setMenus] = useState<Array<INavigationItem>>([]);
  const [isStoreReady, setIsStoreReady] = useState(false);
  const [brandingInfo, setBrandingInfo] = useState<IBrandingData>({
    companyName: 'Aspire Tech',
    logoFilePath: '',
  });
  const { isAuthenticated } = useAuth();
  const apiClient = useAPI();
  const hasInitializedStoreRef = useRef<boolean>(false);
  const isFetchingRef = useRef<boolean>(false);

  useEffect(() => {
    if (!isAuthenticated) {
      hasInitializedStoreRef.current = false;
      isFetchingRef.current = false;
      setIsStoreReady(false);
      return;
    }

    if (hasInitializedStoreRef.current || isFetchingRef.current) {
      return;
    }

    const fetchUserInfo = async () => {
      isFetchingRef.current = true;
      setIsStoreReady(false);

      try {
        // user-details is fast; role-permissions can take 40s+ on remote Mongo.
        // Set userInfo as soon as it returns so BaseLayout can leave the spinner.
        const userInfoPromise = apiClient.get<IUserInfo>(
          API_END_POINTS.FETCH_USER_INFO,
        );
        const sideMenuPromise = apiClient
          .get<Array<ISideMenuResponse>>(
            API_END_POINTS.SIDEMENU.replace(/\/$/, ''),
          )
          .catch(error => {
            console.error('Error fetching sidebar menu:', error);
            return null;
          });

        const userInfoResponse = await userInfoPromise;

        if (!userInfoResponse?.data?.email) {
          hasInitializedStoreRef.current = true;
          setIsStoreReady(true);
          return;
        }

        setUserInfo(prev => ({
          ...prev,
          ...userInfoResponse.data,
        }));
        // Unblock the shell as soon as identity is known.
        setIsStoreReady(true);

        const sideMenuResponse = await sideMenuPromise;
        const rolePermission = sideMenuResponse?.data?.[0];
        setMenus(
          rolePermission
            ? buildMenusFromRolePermission(
                rolePermission,
                userInfoResponse.data.onboardBy,
              )
            : [],
        );

        hasInitializedStoreRef.current = true;
      } catch (error) {
        console.error('Failed to fetch user info:', error);
        hasInitializedStoreRef.current = true;
        setIsStoreReady(true);
      } finally {
        isFetchingRef.current = false;
      }
    };

    const fetchBrandingInfo = async () => {
      try {
        const response: IResponse<IBrandingResponse> = await apiClient.get(
          API_END_POINTS.GET_BRANDING,
        );

        if (!isSuccessResponse(response.statusCode)) {
          throw new Error(response.message || 'Failed to fetch branding data');
        }
        setBrandingInfo({
          companyName: response.data?.companyName || 'Aspire Tech',
          logoFilePath: response.data?.logoFilePath || '',
        });
      } catch (error) {
        console.error('Error fetching branding data:', error);
      }
    };

    fetchUserInfo();
    fetchBrandingInfo();
  }, [isAuthenticated, apiClient]);

  const clearStore = useCallback(() => {
    setUserInfo({} as IUserInfo);
    setMenus([]);
    setIsStoreReady(false);
    hasInitializedStoreRef.current = false;
  }, []);

  const [notificationChangeKey, setNotificationChangeKey] = useState(0);

  const markNotificationAsRead = useCallback(
    async (id: string): Promise<boolean> => {
      try {
        const response = await apiClient.put(
          API_END_POINTS.MARK_NOTIFICATION_AS_READ.replace(':id', id),
        );
        if (isSuccessResponse(response.statusCode)) {
          setNotificationChangeKey(k => k + 1);
          return true;
        }
        return false;
      } catch (error) {
        console.error('Error marking notification as read:', error);
        return false;
      }
    },
    [apiClient],
  );

  const markAllNotificationsAsRead = useCallback(async (): Promise<boolean> => {
    try {
      const response = await apiClient.put(
        API_END_POINTS.MARK_ALL_NOTIFICATIONS_AS_READ,
      );
      if (isSuccessResponse(response.statusCode)) {
        setNotificationChangeKey(k => k + 1);
        return true;
      }
      return false;
    } catch (error) {
      console.error('Error marking all notifications as read:', error);
      return false;
    }
  }, [apiClient]);

  return (
    <StoreContext.Provider
      value={{
        userInfo,
        setUserInfo,
        menus,
        isStoreReady,
        brandingInfo,
        setBrandingInfo,
        clearStore,
        notificationChangeKey,
        markNotificationAsRead,
        markAllNotificationsAsRead,
      }}
    >
      {children}
    </StoreContext.Provider>
  );
};

export default StoreProvider;
