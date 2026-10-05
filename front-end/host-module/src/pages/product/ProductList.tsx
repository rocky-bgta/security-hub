import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';
import { ContentManagementRoutes } from 'routes/ContentManagementRoutes';

const RemoteProductList = lazy(() => import('content-module/ProductList'));

const ProductList = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteProductList hostPath={ContentManagementRoutes} />
    </ErrorBoundaryWrapper>
  );
};

export default ProductList;
