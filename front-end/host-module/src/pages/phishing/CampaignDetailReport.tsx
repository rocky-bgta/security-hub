import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteCampaignDetailReport = lazy(
  () => import('phishing-module/CampaignDetailReport'),
);

type Channel = 'phishing' | 'smishing' | 'vishing';

const CampaignDetailReport = ({ channel }: { channel?: Channel }) => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteCampaignDetailReport channel={channel} />
    </ErrorBoundaryWrapper>
  );
};

export default CampaignDetailReport;
