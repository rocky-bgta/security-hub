// import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

// const RemoteCreateDepartment = lazy(
//   () => import('account-module/CreateDepartment'),
// );

const CreateDepartment = () => {
  return (
    <ErrorBoundaryWrapper>
      {/* <RemoteCreateDepartment /> */}
      <div>Coming Soon</div>
    </ErrorBoundaryWrapper>
  );
};

export default CreateDepartment;
