import { lazy, ReactNode } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';
import { BillingManagementRoutes } from 'routes/BillingManagementRoutes';
import { UserManagementRoutes } from 'routes/UserManagementRoutes';

const RemoteMspLayout = lazy(
  () => import('home-module/MspLayout'),
);

interface IProps {
  children: ReactNode;
  hostPath: typeof UserManagementRoutes | typeof BillingManagementRoutes;
}

const MspLayout = ({ children, hostPath }: IProps) => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteMspLayout hostPath={hostPath}>
        {children}
      </RemoteMspLayout>
    </ErrorBoundaryWrapper>
  );
};

export default MspLayout;
