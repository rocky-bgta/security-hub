import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteResourceCategories = lazy(
  () => import('miscellaneous-module/ResourceCategories'),
);

const ResourceCategories = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteResourceCategories />
    </ErrorBoundaryWrapper>
  );
};

export default ResourceCategories;
