import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemotePhishingReports = lazy(
  () => import('phishing-module/PhishingReports'),
);

type Channel = 'phishing' | 'smishing' | 'vishing';

const PhishingReports = ({ channel }: { channel?: Channel }) => {
  return (
    <ErrorBoundaryWrapper>
      <RemotePhishingReports channel={channel} />
    </ErrorBoundaryWrapper>
  );
};

export default PhishingReports;
