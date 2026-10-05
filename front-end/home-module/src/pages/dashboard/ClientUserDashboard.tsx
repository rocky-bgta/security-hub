import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';
import { routes } from 'routes/Route';

const RemoteClientUserDashboard = lazy(
  () => import('content-module/ClientUserDashboard'),
);

interface IProps {
  hostPath: typeof routes;
}

const ClientUserDashboard = ({ hostPath }: IProps) => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteClientUserDashboard hostPath={hostPath} />
    </ErrorBoundaryWrapper>
  );
};

export default ClientUserDashboard;
