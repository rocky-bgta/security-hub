export type EditorTab = 'preview' | 'visual' | 'code';

export const AI_GENERATE_PROVIDER_TYPES = [
  'OPENAI',
  'GEMINI',
  'CLAUDE',
  'ZAI',
] as const;

export type AIGenerateProviderType =
  (typeof AI_GENERATE_PROVIDER_TYPES)[number];

export interface IAIModelItem {
  id: string;
  name: string;
  providerType: string;
  default: boolean;
  active: boolean;
}

export interface AIElementGenerateParams {
  prompt: string;
  providerType: AIGenerateProviderType;
  model: string;
  elementHtml: string;
  templateCode: string;
}

/** Payload from AIPromptModal when generating HTML for a selected element */
export interface AIPromptSubmitPayload {
  prompt: string;
  providerType: AIGenerateProviderType;
  model: string;
}

export interface ImageDialogState {
  src: string;
  alt: string;
}

export interface FloatingToolbarPosition {
  top: number;
  left: number;
  width: number;
  elementTag: string;
}

export const HEADING_OPTIONS = [
  { value: 'p', label: 'Paragraph' },
  { value: 'h1', label: 'Heading 1' },
  { value: 'h2', label: 'Heading 2' },
  { value: 'h3', label: 'Heading 3' },
  { value: 'h4', label: 'Heading 4' },
  { value: 'h5', label: 'Heading 5' },
  { value: 'h6', label: 'Heading 6' },
];

export const FONT_SIZES = [
  { value: '1', label: '8px' },
  { value: '2', label: '10px' },
  { value: '3', label: '12px' },
  { value: '4', label: '14px' },
  { value: '5', label: '18px' },
  { value: '6', label: '24px' },
  { value: '7', label: '36px' },
];

export const EDITOR_STYLE_ID = '__lp-editor-styles__';

export const SELECTABLE_TAGS = new Set([
  'SECTION',
  'ARTICLE',
  'NAV',
  'HEADER',
  'FOOTER',
  'MAIN',
  'ASIDE',
  'DIV',
  'P',
  'H1',
  'H2',
  'H3',
  'H4',
  'H5',
  'H6',
  'BLOCKQUOTE',
  'PRE',
  'FIGURE',
  'FIGCAPTION',
  'FORM',
  'BUTTON',
  'A',
  'IMG',
  'VIDEO',
  'PICTURE',
  'UL',
  'OL',
  'LI',
  'TABLE',
  'TR',
  'TD',
  'TH',
  'SPAN',
]);
