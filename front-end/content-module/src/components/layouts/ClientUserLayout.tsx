import { lazy, ReactNode } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';
import { routes } from 'routes/Routes';

const RemoteClientUserLayout = lazy(
  () => import('home-module/ClientUserLayout'),
);

interface IProps {
  children: ReactNode;
  hostPath: typeof routes;
}

const ClientUserLayout = ({ children, hostPath }: IProps) => {
  return (
    <ErrorBoundaryWrapper loadingFallback={null}>
      <RemoteClientUserLayout hostPath={hostPath}>
        {children}
      </RemoteClientUserLayout>
    </ErrorBoundaryWrapper>
  );
};

export default ClientUserLayout;
