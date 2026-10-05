import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteSmsTemplateLibrary = lazy(
  () => import('phishing-module/SmsTemplateLibrary'),
);

const SmsTemplateLibrary = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteSmsTemplateLibrary />
    </ErrorBoundaryWrapper>
  );
};

export default SmsTemplateLibrary;
