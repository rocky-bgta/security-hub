import type { EditorTab } from './types';

interface EditorStatusBarProps {
  activeTab: EditorTab;
  charCount: number;
  lineCount: number;
}

const EditorStatusBar = ({
  activeTab,
  charCount,
  lineCount,
}: EditorStatusBarProps) => (
  <div className="home-flex home-items-center home-justify-between home-bg-[#1a1a2e] home-px-3 home-py-1.5 home-text-xs home-text-gray-400">
    <div className="home-flex home-items-center home-gap-3">
      {activeTab === 'visual' && (
        <span className="home-rounded home-bg-yellow-600/20 home-px-2 home-py-0.5 home-text-yellow-300">
          Ctrl+S to save changes
        </span>
      )}
      {activeTab === 'visual' && (
        <span className="home-text-blue-300">
          Click text to edit &middot; Click images to replace
        </span>
      )}
    </div>
    <div className="home-flex home-items-center home-gap-3">
      <span>{charCount.toLocaleString()} chars</span>
      <span>{lineCount} lines</span>
    </div>
  </div>
);

export default EditorStatusBar;
