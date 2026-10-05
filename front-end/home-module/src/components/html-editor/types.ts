import type { ReactNode } from 'react';

export type {
  AIElementGenerateParams,
  AIGenerateProviderType,
  AIPromptSubmitPayload,
  EditorTab,
  FloatingToolbarPosition,
  IAIModelItem,
  ImageDialogState,
} from 'models/HtmlEditor';

export {
  AI_GENERATE_PROVIDER_TYPES,
  EDITOR_STYLE_ID,
  FONT_SIZES,
  HEADING_OPTIONS,
  SELECTABLE_TAGS,
} from 'models/HtmlEditor';

export interface HtmlEditorFeatures {
  captureForm?: boolean;
}

export interface HtmlEditorProps {
  value: string;
  onChange: (value: string) => void;
  readOnly?: boolean;
  error?: string;
  onSave?: () => void;
  iframeTitle?: string;
  onAIGenerate?: (
    params: import('models/HtmlEditor').AIElementGenerateParams,
  ) => Promise<string | undefined>;
  features?: HtmlEditorFeatures;
  renderCaptureFormModal?: (props: {
    isOpen: boolean;
    onInsert: (html: string) => void;
    onClose: () => void;
  }) => ReactNode;
}
