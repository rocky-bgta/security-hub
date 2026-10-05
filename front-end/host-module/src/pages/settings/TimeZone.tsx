import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteTimeZone = lazy(() => import('miscellaneous-module/TimeZone'));

const TimeZone = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteTimeZone />
    </ErrorBoundaryWrapper>
  );
};

export default TimeZone;
