import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteManageNews = lazy(() => import('miscellaneous-module/LatestNews'));

const ManageNews = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteManageNews />
    </ErrorBoundaryWrapper>
  );
};

export default ManageNews;
