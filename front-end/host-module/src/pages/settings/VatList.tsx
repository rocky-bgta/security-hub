import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteVatList = lazy(() => import('billing-module/VatList'));

const VatList = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteVatList />
    </ErrorBoundaryWrapper>
  );
};

export default VatList;
