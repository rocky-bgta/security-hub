import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteSmsTemplateCreate = lazy(
  () => import('phishing-module/SmsTemplateCreate'),
);

const SmsTemplateCreate = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteSmsTemplateCreate />
    </ErrorBoundaryWrapper>
  );
};

export default SmsTemplateCreate;
