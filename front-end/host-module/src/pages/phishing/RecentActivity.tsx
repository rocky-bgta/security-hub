import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteRecentActivity = lazy(
  () => import('phishing-module/RecentActivity'),
);

type Channel = 'phishing' | 'smishing' | 'vishing';

const RecentActivity = ({ channel }: { channel?: Channel }) => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteRecentActivity channel={channel} />
    </ErrorBoundaryWrapper>
  );
};

export default RecentActivity;
