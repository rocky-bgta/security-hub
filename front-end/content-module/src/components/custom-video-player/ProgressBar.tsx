import { MouseEvent } from 'react';

import { ContentTypes, IInteractiveContent } from 'models/Content';
import { cn, formatTime } from 'utils/Helper';

interface ProgressBarProps {
  currentTime: number;
  duration: number;
  maxWatchedTime: number;
  isHovering: boolean;
  hoverPosition: number;
  isDragging: boolean;
  contentType: string;
  sortedContentList: IInteractiveContent[];
  unlockedMarkers: number[];
  nextUnlockMarker: number | null;
  allowSeekAnywhere: boolean;
  onClick: (e: MouseEvent<HTMLDivElement>) => void;
  onHover: (e: MouseEvent<HTMLDivElement>) => void;
  onDrag: (e: MouseEvent<HTMLDivElement>) => void;
  onLeave: () => void;
  onMouseDown: () => void;
  onMouseUp: () => void;
  onMarkerClick: (e: MouseEvent, time: string) => void;
}

const ProgressBar = ({
  currentTime,
  duration,
  maxWatchedTime,
  isHovering,
  hoverPosition,
  isDragging,
  contentType,
  allowSeekAnywhere,
  onClick,
  onHover,
  onDrag,
  onLeave,
  onMouseDown,
  onMouseUp,
}: ProgressBarProps) => {
  return (
    <div
      className={cn(
        'content-relative content-mb-2 content-h-1 content-rounded-full content-bg-white content-backdrop-blur-sm',
        isHovering ? 'content-cursor-pointer' : 'content-cursor-default',
      )}
      onClick={onClick}
      onMouseMove={e => {
        onHover(e);
        if (isDragging && allowSeekAnywhere) {
          onDrag(e);
        }
      }}
      onMouseLeave={onLeave}
      onMouseDown={onMouseDown}
      onMouseUp={onMouseUp}
    >
      {/* Current progress */}
      <div
        className="content-absolute content-left-0 content-top-0 content-h-full content-cursor-pointer content-rounded-full content-bg-[#C90024]"
        style={{ width: `${(currentTime / duration) * 100}%` }}
      />

      {/* Interactive video restrictions */}
      {contentType === ContentTypes.INTERACTIVE_VIDEO && (
        <>
          <div
            className="content-absolute content-left-0 content-top-0 content-h-full content-cursor-pointer content-rounded-full content-bg-gray-500"
            style={{
              width: `${(maxWatchedTime / duration) * 100}%`,
              opacity: 0,
            }}
          />
          <div
            className="content-absolute content-top-0 content-h-full content-cursor-not-allowed content-rounded-full content-bg-gray-700"
            style={{
              left: `${(maxWatchedTime / duration) * 100}%`,
              width: `${100 - (maxWatchedTime / duration) * 100}%`,
            }}
          />
        </>
      )}

      {contentType !== ContentTypes.INTERACTIVE_VIDEO && (
        <div className="content-absolute content-left-0 content-top-0 content-size-full content-cursor-pointer content-rounded-full" />
      )}

      {/* Progress handle */}
      <div
        className={cn(
          'content-absolute content-top-1/2 content-h-3 content-w-3 -content-translate-x-1/2 -content-translate-y-1/2 content-transform content-cursor-pointer content-rounded-full content-bg-white content-shadow-md content-transition-transform',
          isHovering || isDragging ? 'scale-125' : 'scale-0',
        )}
        style={{ left: `${(currentTime / duration) * 100}%` }}
      />

      {/* Hover tooltip */}
      {isHovering && !isDragging && (
        <div
          className="content-absolute content-bottom-4 -content-translate-x-1/2 content-transform content-rounded content-bg-black content-bg-opacity-80 content-px-2 content-py-1 content-text-xs content-text-white"
          style={{ left: `${hoverPosition * 100}%` }}
        >
          {formatTime(hoverPosition * duration)}
        </div>
      )}

      {/* Content markers - Commented out in original */}
      {/* {sortedContentList.map((item, index) => {
        const markerTime = timeToSeconds(item.time);
        const isUnlocked = unlockedMarkers.includes(markerTime);
        const isNextUnlock = nextUnlockMarker === markerTime;

        return (
          <div
            key={index}
            className={cn(
              'group content-absolute content-top-1/2 content-h-2.5 content-w-2.5 -content-translate-y-1/2 content-transform content-cursor-pointer content-rounded-full content-border-2 content-shadow-md content-transition-all hover:content-scale-125',
              isUnlocked
                ? 'content-border-blue-500 content-bg-white hover:content-bg-blue-100'
                : isNextUnlock
                  ? 'content-border-yellow-500 content-bg-yellow-100 hover:content-bg-yellow-200'
                  : 'content-cursor-not-allowed content-border-gray-400 content-bg-gray-200',
            )}
            style={{ left: `${(markerTime / duration) * 100}%` }}
            onClick={e => onMarkerClick(e, item.time)}
          >
            <div className="content-absolute content-bottom-full content-left-1/2 content-mb-2 -content-translate-x-1/2 content-transform content-opacity-0 content-transition-opacity group-hover:content-opacity-100">
              <div className="content-rounded content-bg-white content-px-2 content-py-1 content-text-xs content-text-black content-shadow-lg">
                {item.contentBody?.common?.contentName}
                {!isUnlocked && isNextUnlock && ' (Next)'}
                {!isUnlocked && !isNextUnlock && ' (Locked)'}
              </div>
              <div className="content-absolute content-left-1/2 content-top-full content-h-0 content-w-0 -content-translate-x-1/2 content-transform content-border-4 content-border-transparent content-border-t-white" />
            </div>
          </div>
        );
      })} */}
    </div>
  );
};

export default ProgressBar;
