import { ReactNode, useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';

import { AuthContext, IToken } from 'models/Context';
import { routes } from 'routes/Route';
import { LocalStorageKey } from 'utils/Constants';
import ROLE, { roleList } from 'utils/Role';
import {
  getToken,
  removeAllTokensButPersistents,
  setToken,
} from 'utils/TokenStorage';

const AuthProvider = ({ children }: { children: ReactNode }) => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState<boolean>(true);
  const [isAuthenticating, setIsAuthenticating] = useState<boolean>(false);
  const [isAuthenticated, setIsAuthenticated] = useState<boolean>(false);
  const [role, setRole] = useState<string>(ROLE.NONE);
  const [roleNames, setRoleNames] = useState<Array<string>>([]);
  const [accessToken, setAccessToken] = useState<string>('');
  const [refreshToken, setRefreshToken] = useState<string>('');

  useEffect(() => {
    const loadFromStorage = () => {
      const storedRole = getToken(LocalStorageKey.ROLE);
      if (storedRole && roleList.includes(storedRole)) {
        setRole(storedRole);
        setIsAuthenticated(true);
      }

      const storedAccessToken = getToken(LocalStorageKey.ACCESS_TOKEN);
      const storedRefreshToken = getToken(LocalStorageKey.REFRESH_TOKEN);

      if (storedAccessToken) setAccessToken(storedAccessToken);
      if (storedRefreshToken) setRefreshToken(storedRefreshToken);

      setIsAuthenticating(false);
      setLoading(false);
    };

    loadFromStorage();
  }, []);

  const setAuthenticationInProgress = useCallback((inProgress: boolean) => {
    setIsAuthenticating(inProgress);
  }, []);

  const login = useCallback(
    ({ accessToken, refreshToken, roleNames }: IToken) => {
      setToken(LocalStorageKey.ACCESS_TOKEN, accessToken);
      setToken(LocalStorageKey.REFRESH_TOKEN, refreshToken);
      setToken(LocalStorageKey.ROLE, roleNames[0]);

      setAccessToken(accessToken);
      setRefreshToken(refreshToken);
      setRole(roleNames[0]);
      setRoleNames(roleNames);
      setIsAuthenticating(false);
      setIsAuthenticated(true);
    },
    [],
  );

  const logout = useCallback(() => {
    removeAllTokensButPersistents();

    setAccessToken('');
    setRefreshToken('');
    setRole(ROLE.NONE);
    setRoleNames([]);
    setIsAuthenticating(false);
    setIsAuthenticated(false);

    navigate(routes.login.path);
  }, [navigate]);

  return (
    <AuthContext.Provider
      value={{
        accessToken,
        refreshToken,
        loading,
        isAuthenticating,
        role,
        roleNames,
        isAuthenticated,
        setAuthenticationInProgress,
        login,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export default AuthProvider;
