import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteRoles = lazy(() => import('miscellaneous-module/Roles'));

const Roles = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteRoles />
    </ErrorBoundaryWrapper>
  );
};

export default Roles;
