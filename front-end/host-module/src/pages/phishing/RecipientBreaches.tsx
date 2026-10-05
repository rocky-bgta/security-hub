import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteRecipientBreaches = lazy(
  () => import('phishing-module/RecipientBreaches'),
);

const RecipientBreaches = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteRecipientBreaches />
    </ErrorBoundaryWrapper>
  );
};

export default RecipientBreaches;
