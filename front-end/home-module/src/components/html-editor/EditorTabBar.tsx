import { Code, Eye, Maximize2, Minimize2, Type } from 'lucide-react';
import { cn } from 'utils/Helper';
import type { EditorTab } from './types';

const TAB_CONFIG: { id: EditorTab; icon: React.ReactNode; label: string }[] = [
  { id: 'preview', icon: <Eye className="home-size-4" />, label: 'Preview' },
  {
    id: 'visual',
    icon: <Type className="home-size-4" />,
    label: 'Visual Editor',
  },
  { id: 'code', icon: <Code className="home-size-4" />, label: 'Code' },
];

interface EditorTabBarProps {
  activeTab: EditorTab;
  onTabChange: (tab: EditorTab) => void;
  isFullscreen: boolean;
  onToggleFullscreen: () => void;
}

const EditorTabBar = ({
  activeTab,
  onTabChange,
  isFullscreen,
  onToggleFullscreen,
}: EditorTabBarProps) => (
  <div className="home-flex home-items-center home-justify-between home-bg-[#1a1a2e] home-px-2">
    <div className="home-flex">
      {TAB_CONFIG.map(({ id, icon, label }) => (
        <button
          key={id}
          type="button"
          onClick={() => onTabChange(id)}
          className={cn(
            'home-flex home-items-center home-gap-1.5 home-px-4 home-py-2.5 home-text-sm home-font-medium home-transition-colors',
            activeTab === id
              ? 'home-border-b-2 home-border-blue-500 home-bg-white/10 home-text-white'
              : 'home-text-gray-400 hover:home-bg-white/5 hover:home-text-gray-200',
          )}
        >
          {icon}
          {label}
        </button>
      ))}
    </div>
    <button
      type="button"
      onClick={onToggleFullscreen}
      className="home-rounded home-p-1.5 home-text-gray-400 hover:home-bg-white/10 hover:home-text-white"
      title={isFullscreen ? 'Exit fullscreen' : 'Fullscreen'}
    >
      {isFullscreen ? (
        <Minimize2 className="home-size-4" />
      ) : (
        <Maximize2 className="home-size-4" />
      )}
    </button>
  </div>
);

export default EditorTabBar;
