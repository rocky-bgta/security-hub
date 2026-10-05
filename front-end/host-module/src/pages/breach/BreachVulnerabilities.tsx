import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteBreachVulnerabilities = lazy(
  () => import('phishing-module/BreachVulnerabilities'),
);

const BreachVulnerabilities = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteBreachVulnerabilities />
    </ErrorBoundaryWrapper>
  );
};

export default BreachVulnerabilities;
