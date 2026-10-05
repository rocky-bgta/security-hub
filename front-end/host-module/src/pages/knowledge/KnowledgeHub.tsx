import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteKnowledgeHub = lazy(
  () => import('miscellaneous-module/KnowledgeHub'),
);

const KnowledgeHub = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteKnowledgeHub />
    </ErrorBoundaryWrapper>
  );
};

export default KnowledgeHub;
