import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';
import type { HtmlEditorProps } from 'models/HtmlEditor';

const RemoteHtmlEditor = lazy(() => import('home-module/HtmlEditor'));

const HtmlEditor = (props: HtmlEditorProps) => {
  return (
    <ErrorBoundaryWrapper loadingFallback={null}>
      <RemoteHtmlEditor {...props} />
    </ErrorBoundaryWrapper>
  );
};

export default HtmlEditor;
