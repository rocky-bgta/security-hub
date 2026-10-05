import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteCategoryList = lazy(
  () => import('miscellaneous-module/CategoryList'),
);

const CategoryList = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteCategoryList />
    </ErrorBoundaryWrapper>
  );
};

export default CategoryList;
