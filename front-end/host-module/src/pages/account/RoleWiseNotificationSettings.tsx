import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteRoleWiseNotificationSettings = lazy(
  () => import('account-module/RoleWiseNotificationSettings'),
);

const RoleWiseNotificationSettings = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteRoleWiseNotificationSettings />
    </ErrorBoundaryWrapper>
  );
};

export default RoleWiseNotificationSettings;
