import { MouseEvent as ReactMouseEvent } from 'react';

import { formatTime } from 'utils/Helper';
import {
  BackWardIcon,
  ForwardIcon,
  PauseIcon,
  PlayIcon,
  MuteIcon,
  UnMuteIcon,
} from 'assets/icons';

interface PlaybackControlsProps {
  isPlaying: boolean;
  currentTime: number;
  duration: number;
  isMuted: boolean;
  volume: number;
  showVolumeControls: boolean;
  onPlayPause: (e: ReactMouseEvent) => void;
  onSkip: (seconds: number, e?: ReactMouseEvent) => void;
  onToggleMute: () => void;
  onVolumeChange: (volume: number) => void;
  onVolumeMouseEnter: () => void;
  onVolumeMouseLeave: () => void;
}

const PlaybackControls = ({
  isPlaying,
  currentTime,
  duration,
  isMuted,
  volume,
  showVolumeControls,
  onPlayPause,
  onSkip,
  onToggleMute,
  onVolumeChange,
  onVolumeMouseEnter,
  onVolumeMouseLeave,
}: PlaybackControlsProps) => {
  const handleVolumeClick = (e: ReactMouseEvent) => {
    const rect = e.currentTarget.getBoundingClientRect();
    const width = rect.width;
    const clickX = e.clientX - rect.left;
    const newVolume = clickX / width;
    onVolumeChange(newVolume);
  };

  const handleVolumeDrag = (e: ReactMouseEvent) => {
    e.stopPropagation();
    const trackRect = e.currentTarget.parentElement?.getBoundingClientRect();
    if (!trackRect) return;

    const handleDrag = (moveEvent: MouseEvent) => {
      const mouseX = moveEvent.clientX - trackRect.left;
      const newVolume = mouseX / trackRect.width;
      onVolumeChange(newVolume);
    };

    const stopDrag = () => {
      document.removeEventListener('mousemove', handleDrag);
      document.removeEventListener('mouseup', stopDrag);
    };

    document.addEventListener('mousemove', handleDrag);
    document.addEventListener('mouseup', stopDrag);
  };

  return (
    <div className="content-flex content-items-center content-space-x-2">
      <button
        onClick={onPlayPause}
        className="content-text-white hover:content-text-gray-300"
      >
        {isPlaying ? <PlayIcon /> : <PauseIcon />}
      </button>

      <button
        onClick={e => onSkip(-10, e)}
        className="content-text-white hover:content-text-gray-300"
      >
        <BackWardIcon />
      </button>

      <button
        onClick={e => onSkip(10, e)}
        className="content-text-white hover:content-text-gray-300"
      >
        <ForwardIcon />
      </button>

      <div
        className="content-group content-relative content-flex content-items-center"
        onMouseEnter={onVolumeMouseEnter}
        onMouseLeave={onVolumeMouseLeave}
      >
        <button
          onClick={onToggleMute}
          className="content-relative content-z-20 content-text-white hover:content-text-gray-300"
        >
          {isMuted || volume === 0 ? <MuteIcon /> : <UnMuteIcon />}
        </button>

        <div
          className={`content-absolute content-left-8 content-top-1/2 content-z-10 content-flex content-h-8 -content-translate-y-1/2 content-items-center content-overflow-hidden content-pr-4 content-transition-all content-duration-200 ${
            showVolumeControls ? 'content-w-16' : 'content-w-0'
          }`}
        >
          <button
            className="content-relative content-h-1 content-w-full content-cursor-pointer content-rounded-full content-bg-gray-600"
            onClick={handleVolumeClick}
          >
            <div
              className="content-absolute content-left-0 content-top-0 content-h-full content-rounded-full content-bg-white"
              style={{ width: `${volume * 100}%` }}
            />
            <div
              className="content-absolute content-top-1/2 content-size-3 -content-translate-y-1/2 content-cursor-pointer content-rounded-full content-bg-white content-opacity-0 content-transition-opacity content-duration-200 group-hover:content-opacity-100"
              style={{ left: `${volume * 100}%` }}
              onMouseDown={handleVolumeDrag}
            />
          </button>
        </div>
      </div>

      <span
        className="content-text-sm content-text-white"
        style={{
          marginLeft: showVolumeControls ? '80px' : '10px',
          transition: 'margin-left 0.2s ease-in-out',
        }}
      >
        {formatTime(currentTime)} / {formatTime(duration)}
      </span>
    </div>
  );
};

export default PlaybackControls;
