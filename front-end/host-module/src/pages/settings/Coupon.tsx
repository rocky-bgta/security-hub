import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteCouponList = lazy(() => import('billing-module/CouponList'));

const CouponList = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteCouponList />
    </ErrorBoundaryWrapper>
  );
};

export default CouponList;
