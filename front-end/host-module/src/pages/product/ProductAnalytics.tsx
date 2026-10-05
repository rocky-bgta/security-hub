import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteProductAnalytics = lazy(
  () => import('content-module/ProductAnalytics'),
);

const ProductAnalytics = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteProductAnalytics />
    </ErrorBoundaryWrapper>
  );
};

export default ProductAnalytics;
