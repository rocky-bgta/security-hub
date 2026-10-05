import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteTemplateLibrary = lazy(
  () => import('phishing-module/TemplateLibrary'),
);

const TemplateLibrary = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteTemplateLibrary />
    </ErrorBoundaryWrapper>
  );
};

export default TemplateLibrary;
