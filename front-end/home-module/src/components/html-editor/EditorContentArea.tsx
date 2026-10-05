import { Type } from 'lucide-react';
import { cn } from 'utils/Helper';
import type { EditorTab } from './types';

interface EditorContentAreaProps {
  activeTab: EditorTab;
  iframeRef: React.RefObject<HTMLIFrameElement | null>;
  codeValue: string;
  onCodeChange: (code: string) => void;
  readOnly: boolean;
  isFullscreen: boolean;
  hasContent: boolean;
  iframeTitle?: string;
}

const EditorContentArea = ({
  activeTab,
  iframeRef,
  codeValue,
  onCodeChange,
  readOnly,
  isFullscreen,
  hasContent,
  iframeTitle = 'HTML Editor',
}: EditorContentAreaProps) => (
  <div
    className={cn(
      'home-relative home-bg-white',
      isFullscreen ? 'home-flex-1 home-overflow-hidden' : 'home-h-[600px]',
    )}
  >
    {(activeTab === 'visual' || activeTab === 'preview') && (
      <iframe
        ref={iframeRef}
        title={iframeTitle}
        className="home-size-full home-border-0"
      />
    )}

    {activeTab === 'code' && (
      <textarea
        value={codeValue}
        onChange={e => onCodeChange(e.target.value)}
        readOnly={readOnly}
        className="home-size-full home-resize-none home-bg-[#1e1e2e] home-p-4 home-font-mono home-text-sm home-text-green-400 focus:home-outline-none"
        spellCheck={false}
        placeholder="Paste or type your HTML here..."
      />
    )}

    {!hasContent && activeTab !== 'code' && (
      <div className="home-pointer-events-none home-absolute home-inset-0 home-flex home-items-center home-justify-center home-bg-white/90">
        <div className="home-text-center home-text-gray-400">
          <Type className="home-mx-auto home-mb-3 home-size-12 home-opacity-30" />
          <p className="home-font-medium">No content found</p>
          <p className="home-mt-1 home-text-sm">Paste content in the editor</p>
        </div>
      </div>
    )}
  </div>
);

export default EditorContentArea;
