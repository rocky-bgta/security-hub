import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteCourseStatistics = lazy(
  () => import('phishing-module/CourseStatistics'),
);

type Channel = 'phishing' | 'smishing' | 'vishing';

const CourseStatistics = ({ channel }: { channel?: Channel }) => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteCourseStatistics channel={channel} />
    </ErrorBoundaryWrapper>
  );
};

export default CourseStatistics;
