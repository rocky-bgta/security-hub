import { Fragment, lazy } from 'react';

import ContentCourseHeader from 'components/ContentCourseHeader';
import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteCourseChapters = lazy(
  () => import('content-module/CourseChapters'),
);

const CourseChapters = () => {
  return (
    <Fragment>
      <ContentCourseHeader />
      <ErrorBoundaryWrapper>
        <RemoteCourseChapters />
      </ErrorBoundaryWrapper>
    </Fragment>
  );
};

export default CourseChapters;
