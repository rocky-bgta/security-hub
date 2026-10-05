import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';
import { ContentManagementRoutes } from 'routes/ContentManagementRoutes';

const RemoteBookMarks = lazy(() => import('content-module/BookMarks'));

const BookMarks = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteBookMarks hostPath={ContentManagementRoutes} />
    </ErrorBoundaryWrapper>
  );
};

export default BookMarks;
