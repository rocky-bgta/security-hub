import { lazy, ReactNode } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteAPIClientProvider = lazy(
  () => import('home-module/APIClientProvider'),
);

interface IProps {
  children: ReactNode;
}

export const APIClientProvider = ({ children }: IProps) => {
  return (
    <ErrorBoundaryWrapper loadingFallback={null}>
      <RemoteAPIClientProvider>{children}</RemoteAPIClientProvider>
    </ErrorBoundaryWrapper>
  );
};
