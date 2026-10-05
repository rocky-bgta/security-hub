export interface ITabProps {
  activeTab: TabNames;
  options: Array<{
    label: string;
    value: TabNames;
  }>;
  onChange?: (name: TabNames) => void;
}

export enum TabNames {
  NONE = 'none',

  GENERAL = 'general',
  ADVANCE = 'advance',

  SETTING = 'setting',
  BACKGROUND = 'background',
  SCORING = 'scoring',
  SLIDES = 'slides',
  TABS = 'tabs',
  CONTENT = 'content',
}
