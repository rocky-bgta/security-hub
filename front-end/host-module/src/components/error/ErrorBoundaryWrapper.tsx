import { ReactNode, Suspense } from 'react';
import { ErrorBoundary } from 'react-error-boundary';

import Loader from 'components/Loader';
import ErrorFallback from 'components/error/ErrorFallback';

interface IErrorWrapperProps {
  children: ReactNode;
  loadingFallback?: ReactNode;
}

const ErrorBoundaryWrapper = ({
  children,
  loadingFallback,
}: IErrorWrapperProps) => {
  return (
    <ErrorBoundary FallbackComponent={ErrorFallback}>
      <Suspense fallback={loadingFallback ?? <Loader mode="container" />}>
        {children}
      </Suspense>
    </ErrorBoundary>
  );
};

export default ErrorBoundaryWrapper;
