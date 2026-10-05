import { MouseEvent } from 'react';

import { CaptionsIcon, ExitFullScreenIcon, FullScreenIcon } from 'assets/icons';
import { VideoQuality } from 'components/custom-video-player/hooks/useVideoQuality';
import PlaybackControls from 'components/custom-video-player/PlaybackControls';
import ProgressBar from 'components/custom-video-player/ProgressBar';
import SettingsMenu from 'components/custom-video-player/SettingsMenu';
import { ContentTypes, IInteractiveContent } from 'models/Content';
import { cn } from 'utils/Helper';

interface VideoLanguageOption {
  code: string;
  label: string;
}

interface VideoControlsProps {
  showControls: boolean;
  activeContent: IInteractiveContent | null;
  currentTime: number;
  duration: number;
  maxWatchedTime: number;
  isPlaying: boolean;
  isMuted: boolean;
  volume: number;
  showVolumeControls: boolean;
  isFullscreen: boolean;
  caption: string;
  contentType: string;
  isMpdFile: boolean;
  showSettings: boolean;
  availableQualities: VideoQuality[];
  currentQuality: number;
  sortedContentList: IInteractiveContent[];
  unlockedMarkers: number[];
  nextUnlockMarker: number | null;
  allowSeekAnywhere: boolean;
  availableLanguages?: VideoLanguageOption[];
  currentLanguageCode?: string;
  progressBarProps: any;
  onPlayPause: (e: MouseEvent) => void;
  onSkip: (seconds: number, e?: MouseEvent) => void;
  onToggleMute: () => void;
  onVolumeChange: (volume: number) => void;
  onVolumeMouseEnter: () => void;
  onVolumeMouseLeave: () => void;
  onToggleCaption: () => void;
  onToggleFullscreen: (e: MouseEvent) => void;
  onToggleSettings: (e: MouseEvent) => void;
  onQualityChange: (index: number) => void;
  onLanguageChange?: (code: string) => void;
  onProgressBarClick: (e: MouseEvent<HTMLDivElement>) => void;
  onProgressBarDrag: (e: MouseEvent<HTMLDivElement>) => void;
  onMarkerClick: (e: MouseEvent, time: string) => void;
}

const VideoControls = ({
  showControls,
  activeContent,
  currentTime,
  duration,
  maxWatchedTime,
  isPlaying,
  isMuted,
  volume,
  showVolumeControls,
  isFullscreen,
  caption,
  contentType,
  isMpdFile,
  showSettings,
  availableQualities,
  currentQuality,
  sortedContentList,
  unlockedMarkers,
  nextUnlockMarker,
  allowSeekAnywhere,
  availableLanguages = [],
  currentLanguageCode,
  progressBarProps,
  onPlayPause,
  onSkip,
  onToggleMute,
  onVolumeChange,
  onVolumeMouseEnter,
  onVolumeMouseLeave,
  onToggleCaption,
  onToggleFullscreen,
  onToggleSettings,
  onQualityChange,
  onLanguageChange,
  onProgressBarClick,
  onProgressBarDrag,
  onMarkerClick,
}: VideoControlsProps) => {
  if (!showControls || activeContent) return null;

  return (
    <div
      onClick={e => e.stopPropagation()}
      className="content-absolute content-inset-x-0 content-bottom-0 content-z-20 content-cursor-default content-bg-gradient-to-t content-from-black content-to-transparent content-p-4"
    >
      <ProgressBar
        currentTime={currentTime}
        duration={duration}
        maxWatchedTime={maxWatchedTime}
        contentType={contentType}
        sortedContentList={sortedContentList}
        unlockedMarkers={unlockedMarkers}
        nextUnlockMarker={nextUnlockMarker}
        allowSeekAnywhere={allowSeekAnywhere}
        onClick={onProgressBarClick}
        onDrag={onProgressBarDrag}
        onMarkerClick={onMarkerClick}
        {...progressBarProps}
      />

      <div className="content-flex content-items-center content-justify-between">
        <PlaybackControls
          isPlaying={isPlaying}
          currentTime={currentTime}
          duration={duration}
          isMuted={isMuted}
          volume={volume}
          showVolumeControls={showVolumeControls}
          onPlayPause={onPlayPause}
          onSkip={onSkip}
          onToggleMute={onToggleMute}
          onVolumeChange={onVolumeChange}
          onVolumeMouseEnter={onVolumeMouseEnter}
          onVolumeMouseLeave={onVolumeMouseLeave}
        />

        <div className="content-flex content-items-center content-space-x-2">
          {contentType !== ContentTypes.INTERACTIVE_VIDEO && (
            <button
              onClick={onToggleCaption}
              className={cn(
                'content-relative content-z-20 content-ml-6 content-text-white hover:content-text-gray-300',
                caption ? 'content-border-b-2 content-border-white' : '',
              )}
            >
              <CaptionsIcon />
            </button>
          )}

          <button
            onClick={onToggleFullscreen}
            className="content-relative content-z-20 content-ml-6 content-text-white hover:content-text-gray-300"
          >
            {isFullscreen ? <FullScreenIcon /> : <ExitFullScreenIcon />}
          </button>

          {(isMpdFile || availableLanguages.length > 1) && (
            <SettingsMenu
              showSettings={showSettings}
              availableQualities={availableQualities}
              currentQuality={currentQuality}
              availableLanguages={availableLanguages}
              currentLanguageCode={currentLanguageCode}
              onToggleSettings={onToggleSettings}
              onQualityChange={onQualityChange}
              onLanguageChange={onLanguageChange}
            />
          )}
        </div>
      </div>
    </div>
  );
};

export default VideoControls;
