import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteTextEditor = lazy(() => import('home-module/TextEditor'));

interface IProps {
  value: string;
  onChange: (value: string) => void;
  className?: string;
  toolbar?: string;
  isDark?: boolean;
}

const TextEditor = ({
  value,
  onChange,
  className,
  toolbar,
  isDark = false,
}: IProps) => {
  return (
    <ErrorBoundaryWrapper loadingFallback={null}>
      <RemoteTextEditor
        value={value}
        onChange={onChange}
        className={className}
        toolbar={toolbar}
        isDark={isDark}
      />
    </ErrorBoundaryWrapper>
  );
};

export default TextEditor;
