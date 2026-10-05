import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteCompanyImpersonation = lazy(
  () => import('phishing-module/CompanyImpersonation'),
);

const CompanyImpersonation = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteCompanyImpersonation />
    </ErrorBoundaryWrapper>
  );
};

export default CompanyImpersonation;
