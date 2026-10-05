import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteMenuList = lazy(() => import('miscellaneous-module/Menu'));

const MenuList = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteMenuList />
    </ErrorBoundaryWrapper>
  );
};

export default MenuList;
