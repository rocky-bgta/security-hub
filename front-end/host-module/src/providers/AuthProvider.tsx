import { lazy, ReactNode } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteAuthProvider = lazy(() => import('home-module/AuthProvider'));

interface IProps {
  children: ReactNode;
}

export const AuthProvider = ({ children }: IProps) => {
  return (
    <ErrorBoundaryWrapper loadingFallback={null}>
      <RemoteAuthProvider>{children}</RemoteAuthProvider>
    </ErrorBoundaryWrapper>
  );
};
