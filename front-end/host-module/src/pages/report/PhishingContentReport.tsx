import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemotePhishingContentReport = lazy(
  () => import('miscellaneous-module/PhishingContentReport'),
);

const PhishingContentReport = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemotePhishingContentReport />
    </ErrorBoundaryWrapper>
  );
};

export default PhishingContentReport;
