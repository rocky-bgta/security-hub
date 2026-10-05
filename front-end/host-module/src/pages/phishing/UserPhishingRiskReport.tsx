import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteUserPhishingRiskReport = lazy(
  () => import('phishing-module/UserPhishingRiskReport'),
);

type Channel = 'phishing' | 'smishing' | 'vishing';

const UserPhishingRiskReport = ({ channel }: { channel?: Channel }) => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteUserPhishingRiskReport channel={channel} />
    </ErrorBoundaryWrapper>
  );
};

export default UserPhishingRiskReport;
