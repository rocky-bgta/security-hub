// import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

// const RemoteDepartmentList = lazy(
//   () => import('account-module/DepartmentList'),
// );

const DepartmentList = () => {
  return (
    <ErrorBoundaryWrapper>
      {/* <RemoteDepartmentList /> */}
      <div>Coming Soon</div>
    </ErrorBoundaryWrapper>
  );
};

export default DepartmentList;
