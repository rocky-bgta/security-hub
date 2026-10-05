import { lazy, ReactNode } from 'react';
import { Navigate } from 'react-router-dom';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';
import { useStore } from 'hooks/UseStore';
import Loader from 'common/loader/Loader';
import { routes } from 'routes/Routes';

const RemoteSelfClientAdminLayout = lazy(
  () => import('home-module/SelfClientAdminLayout'),
);

interface IProps {
  children: ReactNode;
}

const SelfClientAdminLayout = ({ children }: IProps) => {
  const { userInfo } = useStore();

  if (!userInfo.userId) {
    return <Loader />;
  }

  if (!userInfo.selfOnboardingUser) {
    return <Navigate to={routes.dashboard.path} replace />;
  }

  return (
    <ErrorBoundaryWrapper>
      <RemoteSelfClientAdminLayout>{children}</RemoteSelfClientAdminLayout>
    </ErrorBoundaryWrapper>
  );
};

export default SelfClientAdminLayout;
