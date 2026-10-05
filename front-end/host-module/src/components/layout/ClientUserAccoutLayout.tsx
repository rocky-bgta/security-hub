import { lazy, ReactNode } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';
import { ClientUserRoutes } from 'routes/ClientUserRoutes';

const RemoteClientUserAccountLayout = lazy(
  () => import('home-module/ClientUserAccountLayout'),
);

interface IProps {
  children: ReactNode;
  hostPath: typeof ClientUserRoutes;
}

const ClientUserAccountLayout = ({ children, hostPath }: IProps) => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteClientUserAccountLayout hostPath={hostPath}>
        {children}
      </RemoteClientUserAccountLayout>
    </ErrorBoundaryWrapper>
  );
};

export default ClientUserAccountLayout;
