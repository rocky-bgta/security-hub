import { Sparkles, X } from 'lucide-react';
import { useEffect, useRef, useState } from 'react';
import type { FloatingToolbarPosition } from './types';

interface FloatingElementToolbarProps {
  position: FloatingToolbarPosition;
  onAIGenerate?: () => void;
  onDeselect: () => void;
}

const TOOLBAR_HEIGHT = 40;
const GAP = 8;

const FloatingElementToolbar = ({
  position,
  onAIGenerate,
  onDeselect,
}: FloatingElementToolbarProps) => {
  const toolbarRef = useRef<HTMLDivElement>(null);
  const [placement, setPlacement] = useState<'above' | 'below'>('above');

  useEffect(() => {
    const above = position.top - TOOLBAR_HEIGHT - GAP >= 0;
    setTimeout(() => {
      setPlacement(above ? 'above' : 'below');
    }, 0);
  }, [position.top]);

  const topPx =
    placement === 'above'
      ? position.top - TOOLBAR_HEIGHT - GAP
      : position.top + GAP + 4;

  const leftPx = Math.max(8, position.left);

  return (
    <div
      ref={toolbarRef}
      className="home-fixed home-z-[60] home-flex home-items-center home-gap-1 home-rounded-lg home-border home-border-indigo-300/50 home-bg-white home-px-2 home-py-1.5 home-shadow-lg"
      style={{ top: topPx, left: leftPx }}
      onMouseDown={e => e.preventDefault()}
    >
      <span className="home-mr-1 home-rounded home-bg-indigo-50 home-px-1.5 home-py-0.5 home-text-[10px] home-font-semibold home-uppercase home-tracking-wider home-text-indigo-600">
        {position.elementTag}
      </span>

      {onAIGenerate && (
        <>
          <div className="home-mx-1 home-h-5 home-w-px home-bg-gray-200" />

          <button
            type="button"
            onClick={onAIGenerate}
            className="home-flex home-items-center home-gap-1.5 home-rounded-md home-bg-gradient-to-r home-from-indigo-500 home-to-purple-500 home-px-3 home-py-1 home-text-xs home-font-medium home-text-white home-shadow-sm home-transition-all hover:home-from-indigo-600 hover:home-to-purple-600 hover:home-shadow-md active:home-scale-95"
          >
            <Sparkles className="home-size-3.5" />
            Generate AI
          </button>
        </>
      )}

      <div className="home-mx-1 home-h-5 home-w-px home-bg-gray-200" />

      <button
        type="button"
        onClick={onDeselect}
        title="Deselect element"
        className="home-flex home-size-6 home-items-center home-justify-center home-rounded home-text-gray-400 home-transition-colors hover:home-bg-gray-100 hover:home-text-gray-600"
      >
        <X className="home-size-3.5" />
      </button>
    </div>
  );
};

export default FloatingElementToolbar;
