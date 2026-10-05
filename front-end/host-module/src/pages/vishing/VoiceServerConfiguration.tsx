import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteVoiceServerConfiguration = lazy(
  () => import('phishing-module/VoiceServerConfiguration'),
);

const VoiceServerConfiguration = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteVoiceServerConfiguration />
    </ErrorBoundaryWrapper>
  );
};

export default VoiceServerConfiguration;
