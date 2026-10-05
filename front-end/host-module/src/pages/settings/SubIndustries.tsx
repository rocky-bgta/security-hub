import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteSubIndustries = lazy(
  () => import('miscellaneous-module/SubIndustries'),
);

const SubIndustries = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteSubIndustries />
    </ErrorBoundaryWrapper>
  );
};

export default SubIndustries;
