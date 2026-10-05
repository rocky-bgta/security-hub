import type { ReactNode } from 'react';

export interface AIElementGenerateParams {
  prompt: string;
  providerType: string;
  model: string;
  elementHtml: string;
  templateCode: string;
}

export interface HtmlEditorProps {
  value: string;
  onChange: (value: string) => void;
  readOnly?: boolean;
  error?: string;
  onSave?: () => void;
  iframeTitle?: string;
  onAIGenerate?: (
    params: AIElementGenerateParams,
  ) => Promise<string | undefined>;
  features?: { captureForm?: boolean };
  renderCaptureFormModal?: (props: {
    isOpen: boolean;
    onInsert: (html: string) => void;
    onClose: () => void;
  }) => ReactNode;
}
