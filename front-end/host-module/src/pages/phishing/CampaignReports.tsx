import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteCampaignReports = lazy(
  () => import('phishing-module/CampaignReports'),
);

type Channel = 'phishing' | 'smishing' | 'vishing';

const CampaignReports = ({ channel }: { channel?: Channel }) => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteCampaignReports channel={channel} />
    </ErrorBoundaryWrapper>
  );
};

export default CampaignReports;
