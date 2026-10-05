import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteClientBuyProduct = lazy(
  () => import('user-module/ClientBuyProduct'),
);

const ClientBuyProduct = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteClientBuyProduct />
    </ErrorBoundaryWrapper>
  );
};

export default ClientBuyProduct;
