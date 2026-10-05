import { lazy, ReactNode } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteStoreProvider = lazy(() => import('home-module/StoreProvider'));

interface IProps {
  children: ReactNode;
}

export const StoreProvider = ({ children }: IProps) => {
  return (
    <ErrorBoundaryWrapper loadingFallback={null}>
      <RemoteStoreProvider>{children}</RemoteStoreProvider>
    </ErrorBoundaryWrapper>
  );
};
