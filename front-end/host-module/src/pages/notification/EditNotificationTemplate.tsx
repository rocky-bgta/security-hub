import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteEditNotificationTemplate = lazy(
  () => import('account-module/EditNotificationTemplate'),
);

const EditNotificationTemplate = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteEditNotificationTemplate />
    </ErrorBoundaryWrapper>
  );
};

export default EditNotificationTemplate;
