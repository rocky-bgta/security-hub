import { lazy, ReactNode } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';
import { ClientUserRoutes } from 'routes/ClientUserRoutes';

const RemoteClientUserLayout = lazy(
  () => import('home-module/ClientUserLayout'),
);

interface IProps {
  children: ReactNode;
  hostPath: typeof ClientUserRoutes;
}

const ClientUserLayout = ({ children, hostPath }: IProps) => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteClientUserLayout hostPath={hostPath}>
        {children}
      </RemoteClientUserLayout>
    </ErrorBoundaryWrapper>
  );
};

export default ClientUserLayout;
