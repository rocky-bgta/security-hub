import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteAttackTemplateLibrary = lazy(
  () => import('phishing-module/AttackTemplateLibrary'),
);

const AttackTemplateLibrary = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteAttackTemplateLibrary />
    </ErrorBoundaryWrapper>
  );
};

export default AttackTemplateLibrary;
