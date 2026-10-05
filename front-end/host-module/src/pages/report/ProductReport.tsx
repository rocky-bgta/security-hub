import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteProductReport = lazy(
  () => import('miscellaneous-module/ProductReport'),
);

const ProductReport = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteProductReport />
    </ErrorBoundaryWrapper>
  );
};

export default ProductReport;
