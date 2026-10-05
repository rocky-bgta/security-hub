import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteDepartmentList = lazy(
  () => import('miscellaneous-module/DepartmentList'),
);

const DepartmentList = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteDepartmentList />
    </ErrorBoundaryWrapper>
  );
};

export default DepartmentList;
