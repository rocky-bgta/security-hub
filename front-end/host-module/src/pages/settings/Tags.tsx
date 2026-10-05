import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteTags = lazy(() => import('miscellaneous-module/Tags'));

const Tags = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteTags />
    </ErrorBoundaryWrapper>
  );
};

export default Tags;
