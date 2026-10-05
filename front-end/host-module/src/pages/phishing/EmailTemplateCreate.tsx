import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteEmailTemplateCreate = lazy(
  () => import('phishing-module/EmailTemplateCreate'),
);

const EmailTemplateCreate = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteEmailTemplateCreate />
    </ErrorBoundaryWrapper>
  );
};

export default EmailTemplateCreate;
