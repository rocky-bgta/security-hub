import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteLanguage = lazy(() => import('miscellaneous-module/Languages'));

const Language = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteLanguage />
    </ErrorBoundaryWrapper>
  );
};

export default Language;
