import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';
import { ContentManagementRoutes } from 'routes/ContentManagementRoutes';

const RemotePhishingList = lazy(() => import('content-module/PhishingList'));

const PhishingList = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemotePhishingList hostPath={ContentManagementRoutes} />
    </ErrorBoundaryWrapper>
  );
};

export default PhishingList;
