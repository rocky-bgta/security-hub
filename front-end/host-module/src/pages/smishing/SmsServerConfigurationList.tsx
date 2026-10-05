import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteSmsServerConfigurationList = lazy(
  () => import('phishing-module/SmsServerConfigurationList'),
);

const SmsServerConfigurationList = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteSmsServerConfigurationList />
    </ErrorBoundaryWrapper>
  );
};

export default SmsServerConfigurationList;
