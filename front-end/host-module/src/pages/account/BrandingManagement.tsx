import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteBrandingManagement = lazy(
  () => import('account-module/BrandingManagement'),
);

const BrandingManagement = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteBrandingManagement />
    </ErrorBoundaryWrapper>
  );
};

export default BrandingManagement;
