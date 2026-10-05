import { lazy, ReactNode } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';
import { routes } from 'routes/Routes';

const RemoteMspLayout = lazy(
  () => import('home-module/MspLayout'),
);

interface IProps {
  children: ReactNode;
  hostPath: typeof routes;
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
