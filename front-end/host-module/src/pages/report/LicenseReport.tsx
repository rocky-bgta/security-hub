import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteLicenseReport = lazy(
  () => import('miscellaneous-module/LicenseReport'),
);

const LicenseReport = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteLicenseReport />
    </ErrorBoundaryWrapper>
  );
};

export default LicenseReport;
