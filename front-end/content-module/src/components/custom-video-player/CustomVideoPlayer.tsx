import {
  MouseEvent,
  ReactNode,
  useCallback,
  useEffect,
  useMemo,
  useRef,
  useState,
} from 'react';

import { useFullscreen } from 'components/custom-video-player/hooks/useFullscreen';
import { useInteractiveContent } from 'components/custom-video-player/hooks/useInteractiveContent';
import { useProgressBar } from 'components/custom-video-player/hooks/useProgressBar';
import { useVideoControls } from 'components/custom-video-player/hooks/useVideoControls';
import { useVideoPlayer } from 'components/custom-video-player/hooks/useVideoPlayer';
import { useVideoQuality } from 'components/custom-video-player/hooks/useVideoQuality';
import InteractiveOverlay from 'components/custom-video-player/InteractiveOverlay';
import VideoControls from 'components/custom-video-player/VideoControls';
import VideoElement from 'components/custom-video-player/VideoElement';
import {
  ContentTypes,
  IContent,
  IInteractiveContent,
  IInteractiveVideoByLanguage,
  IInteractiveVideoContent,
  IVideoContent,
  TContent,
} from 'models/Content';
import { FILE_PATH_PREFIX } from 'utils/Constants';
import { timeToSeconds } from 'utils/Helper';

interface VideoLanguageOption {
  code: string;
  label: string;
  processVideoUrl: string;
  contentList: Array<IInteractiveContent>;
}

interface IProps {
  content: IContent<IInteractiveVideoContent | IVideoContent>;
  contentRenderer?: (
    content: IContent<TContent>,
    handleNextContent: () => void,
    onComplete?: () => void,
  ) => ReactNode;
  showCompleteButton?: (type: ContentTypes) => void;
  courseComplete?: () => void;
  isAdminUser?: boolean;
}

const CustomVideoPlayer = ({
  content,
  contentRenderer,
  showCompleteButton,
  courseComplete,
  isAdminUser = true,
}: IProps) => {
  const isInteractiveVideoContent =
    content.common.contentType === ContentTypes.INTERACTIVE_VIDEO;

  const interactiveVideo = isInteractiveVideoContent
    ? undefined
    : (content.specific as IVideoContent | undefined)?.interactiveVideo;

  const availableLanguages = useMemo<VideoLanguageOption[]>(() => {
    if (isInteractiveVideoContent) {
      const languageVideos = (content.specific as IInteractiveVideoContent)
        .interactiveVideoByLanguage;

      return languageVideos
        .map((video: IInteractiveVideoByLanguage, index) => ({
          code: video.id,
          label: video.language || `Language ${index + 1}`,
          // Prefer processed DASH (.mpd); fall back to the raw uploaded file.
          processVideoUrl: video.processVideoUrl || video.videoUrl,
          contentList: video.contentList ?? [],
        }))
        .filter(video => !!video.processVideoUrl);
    }

    const source =
      interactiveVideo?.processVideoUrl || interactiveVideo?.videoUrl;

    if (!source) {
      return [];
    }

    return [
      {
        code: 'default',
        label: 'Default',
        processVideoUrl: source,
        contentList: [],
      },
    ];
  }, [
    content.specific,
    interactiveVideo?.processVideoUrl,
    interactiveVideo?.videoUrl,
    isInteractiveVideoContent,
  ]);

  const defaultLanguageCode = useMemo(() => {
    if (!isInteractiveVideoContent) return 'default';

    const languageVideos = (content.specific as IInteractiveVideoContent)
      .interactiveVideoByLanguage;
    const hasSource = (video: IInteractiveVideoByLanguage) =>
      !!(video.processVideoUrl || video.videoUrl);
    const defaultVideo =
      languageVideos.find(video => video.default && hasSource(video)) ||
      languageVideos.find(hasSource);

    return defaultVideo?.id || 'default';
  }, [content.specific, isInteractiveVideoContent]);

  const [selectedLanguageCode, setSelectedLanguageCode] =
    useState<string>(defaultLanguageCode);

  useEffect(() => {
    setSelectedLanguageCode(defaultLanguageCode);
  }, [defaultLanguageCode]);

  useEffect(() => {
    if (!availableLanguages.length) {
      setSelectedLanguageCode(defaultLanguageCode);
      return;
    }

    if (!availableLanguages.some(l => l.code === selectedLanguageCode)) {
      setSelectedLanguageCode(availableLanguages[0].code);
    }
  }, [availableLanguages, defaultLanguageCode, selectedLanguageCode]);

  // Video URL setup
  const selectedLanguage =
    availableLanguages.find(l => l.code === selectedLanguageCode) ||
    availableLanguages[0];
  const processVideoUrl = selectedLanguage?.processVideoUrl || '';
  const videoUrl = processVideoUrl.startsWith('http')
    ? processVideoUrl
    : FILE_PATH_PREFIX + processVideoUrl;

  //  Uncomment this video in local development
  // const videoUrl = "https://dash.akamaized.net/akamai/bbb_30fps/bbb_30fps.mpd";

  const isMpdFile =
    videoUrl.toLowerCase().endsWith('.mpd') && videoUrl !== FILE_PATH_PREFIX;

  const canSeekAnywhere =
    isAdminUser ||
    content?.common?.contentType !== ContentTypes.INTERACTIVE_VIDEO;

  // Caption state
  const [caption, setCaption] = useState<string>('');

  // Sorted content list for interactive videos
  const sortedContentList = useMemo(() => {
    const selectedLanguageContentList = selectedLanguage?.contentList;

    return [...(selectedLanguageContentList ?? [])].sort(
      (a, b) => timeToSeconds(a.time) - timeToSeconds(b.time),
    );
  }, [selectedLanguage]);

  // Custom hooks
  const videoPlayer = useVideoPlayer({ videoUrl, isMpdFile });

  const videoQuality = useVideoQuality(
    videoPlayer.dashPlayerRef,
    videoPlayer.videoRef,
    videoPlayer.isPlaying,
    isMpdFile,
  );
  const interactiveContent = useInteractiveContent(
    sortedContentList,
    videoPlayer.duration,
  );
  const fullscreen = useFullscreen();
  const controls = useVideoControls(videoPlayer.isPlaying);
  const progressBar = useProgressBar();
  const completionTriggeredRef = useRef<boolean>(false);
  const shouldResumeAfterLanguageSwitchRef = useRef<boolean>(false);

  const handleCompletionOnce = useCallback(() => {
    if (completionTriggeredRef.current) return;
    completionTriggeredRef.current = true;

    if (fullscreen.isFullscreen) {
      fullscreen.toggleFullscreen();
    }

    videoPlayer.setIsPlaying(false);

    if (
      content.common.contentType === ContentTypes.VIDEO ||
      content.common.contentType === ContentTypes.ANIMATION ||
      content.common.contentType === ContentTypes.INTERACTIVE_VIDEO
    ) {
      courseComplete?.();
    }
  }, [content.common.contentType, courseComplete, fullscreen, videoPlayer]);

  useEffect(() => {
    completionTriggeredRef.current = false;
  }, [videoUrl]);

  // Event handlers
  const handlePlayPause = (e?: MouseEvent) => {
    if (e) e.stopPropagation();
    if (videoPlayer.isLoading || interactiveContent.activeContent) return;

    if (videoPlayer.isPlaying) {
      videoPlayer.pause();
    } else {
      videoPlayer.play();
    }
  };

  const handleSkip = (seconds: number, e?: MouseEvent) => {
    if (e) e.stopPropagation();
    if (videoPlayer.isLoading || interactiveContent.activeContent) return;

    const newTime = videoPlayer.currentTime + seconds;

    if (seconds < 0) {
      const restrictedTime = Math.max(0, newTime);
      videoPlayer.seek(restrictedTime);
    } else {
      const restrictedTime = canSeekAnywhere
        ? Math.min(videoPlayer.duration, newTime)
        : Math.min(interactiveContent.maxWatchedTime, newTime);
      videoPlayer.seek(restrictedTime);
    }
  };

  const calculateTimeFromProgressEvent = useCallback(
    (e: MouseEvent<HTMLDivElement>) => {
      const progressBarElement = e.currentTarget;
      const rect = progressBarElement.getBoundingClientRect();
      const clickPosition = e.clientX - rect.left;
      const percentage = Math.max(0, Math.min(1, clickPosition / rect.width));
      return percentage * videoPlayer.duration;
    },
    [videoPlayer.duration],
  );

  const handleProgressBarClick = useCallback(
    (e: MouseEvent<HTMLDivElement>) => {
      if (videoPlayer.isLoading || interactiveContent.activeContent) return;

      const newTime = calculateTimeFromProgressEvent(e);

      if (content?.common?.contentType === ContentTypes.INTERACTIVE_VIDEO) {
        if (canSeekAnywhere || newTime <= interactiveContent.maxWatchedTime) {
          videoPlayer.seek(newTime);
        }
      } else {
        videoPlayer.seek(newTime);
      }
    },
    [
      interactiveContent.activeContent,
      interactiveContent.maxWatchedTime,
      calculateTimeFromProgressEvent,
      content?.common?.contentType,
      canSeekAnywhere,
      videoPlayer,
    ],
  );

  const handleProgressBarDrag = useCallback(
    (e: MouseEvent<HTMLDivElement>) => {
      if (
        videoPlayer.isLoading ||
        !canSeekAnywhere ||
        interactiveContent.activeContent
      )
        return;

      const newTime = calculateTimeFromProgressEvent(e);
      videoPlayer.seek(newTime);
    },
    [
      calculateTimeFromProgressEvent,
      canSeekAnywhere,
      interactiveContent.activeContent,
      videoPlayer,
    ],
  );

  const handleContentMarkerClick = (e: MouseEvent, time: string) => {
    e.stopPropagation();
    if (videoPlayer.isLoading || interactiveContent.activeContent) return;

    const timeInSeconds = timeToSeconds(time);
    if (interactiveContent.unlockedMarkers.includes(timeInSeconds)) {
      videoPlayer.seek(timeInSeconds);
    }
  };

  const handleNextButtonClick = () => {
    const currentMarkerTime = timeToSeconds(
      interactiveContent.activeContent?.time as string,
    );
    interactiveContent.unlockMarker(currentMarkerTime);

    // const currentMarkerIndex = sortedContentList.findIndex(
    //   item => timeToSeconds(item.time) === currentMarkerTime,
    // );

    // Note: maxWatchedTime will be updated by the useInteractiveContent hook.

    const newTime = currentMarkerTime + 0.5 + 0.1;

    const lastUnlockedTime = Math.max(...interactiveContent.unlockedMarkers);
    const lastUnlockedIndex = sortedContentList.findIndex(
      item => timeToSeconds(item.time) === lastUnlockedTime,
    );

    if (lastUnlockedIndex === sortedContentList.length - 2) {
      showCompleteButton?.(content.common?.contentType);
    }

    if (videoPlayer.videoRef.current) {
      const targetTime = Math.min(videoPlayer.duration, newTime);
      videoPlayer.seek(targetTime);
      if (videoPlayer.duration - targetTime > 0.05) {
        videoPlayer.play();
      } else {
        handleCompletionOnce();
      }
    }

    interactiveContent.setActiveContent(null);
    controls.setShowControls(true);
  };

  const handleVideoAreaClick = () => {
    if (videoPlayer.isLoading || interactiveContent.activeContent) return;
    handlePlayPause();
  };

  const handleLanguageChange = useCallback(
    (languageCode: string) => {
      if (languageCode === selectedLanguageCode) return;

      shouldResumeAfterLanguageSwitchRef.current = videoPlayer.isPlaying;
      if (videoPlayer.isPlaying) {
        videoPlayer.pause();
      }
      videoPlayer.setIsLoading(true);
      controls.setShowSettings(false);
      setSelectedLanguageCode(languageCode);
    },
    [controls, selectedLanguageCode, videoPlayer],
  );

  const handleFullscreenToggle = (e: MouseEvent) => {
    e.stopPropagation();
    if (videoPlayer.isLoading || interactiveContent.activeContent) return;
    fullscreen.toggleFullscreen();
  };

  const handleToggleCaption = () => {
    if (caption) {
      setCaption('');
    } else {
      setCaption((content.specific as IVideoContent).captionUrl);
    }
  };

  const handleTimeUpdate = () => {
    if (videoPlayer.videoRef.current) {
      const currentVideoTime = videoPlayer.videoRef.current.currentTime;
      videoPlayer.setCurrentTime(currentVideoTime);

      // Check for interactive content and pause if needed
      const shouldPause =
        interactiveContent.checkContentAtTime(currentVideoTime);
      if (shouldPause && videoPlayer.videoRef.current) {
        videoPlayer.pause();
      }

      const END_TOLERANCE_SECONDS = 0.05;
      if (
        !interactiveContent.activeContent &&
        videoPlayer.duration > 0 &&
        currentVideoTime >= videoPlayer.duration - END_TOLERANCE_SECONDS
      ) {
        handleCompletionOnce();
      }
    }
  };

  const handleLoadedMetadata = () => {
    if (videoPlayer.videoRef.current) {
      videoPlayer.setDuration(videoPlayer.videoRef.current.duration);

      if (shouldResumeAfterLanguageSwitchRef.current) {
        shouldResumeAfterLanguageSwitchRef.current = false;
        videoPlayer.play();
      }
    }
  };

  const handleVolumeMouseEnter = () => {
    controls.setShowVolumeControls(true);
    if (controls.volumeTimeoutRef.current) {
      clearTimeout(controls.volumeTimeoutRef.current);
    }
  };

  const handleVolumeMouseLeave = () => {
    if (controls.volumeTimeoutRef.current) {
      clearTimeout(controls.volumeTimeoutRef.current);
    }
    // eslint-disable-next-line react-hooks/immutability
    controls.volumeTimeoutRef.current = setTimeout(() => {
      controls.setShowVolumeControls(false);
    }, 30);
  };

  return (
    <div
      ref={fullscreen.containerRef}
      className="content-relative content-size-full content-overflow-hidden"
      onMouseMove={controls.handleMouseMove}
      onMouseLeave={() =>
        videoPlayer.isPlaying && controls.setShowControls(false)
      }
    >
      <div
        className="content-relative content-size-full content-cursor-pointer"
        onClick={handleVideoAreaClick}
      >
        <div className="content-relative content-size-full">
          <VideoElement
            videoRef={videoPlayer.videoRef}
            caption={caption}
            videoUrl={videoUrl}
            isMpdFile={isMpdFile}
            onTimeUpdate={handleTimeUpdate}
            onLoadedMetadata={handleLoadedMetadata}
            onEnded={handleCompletionOnce}
          />

          {videoPlayer.isLoading && (
            <div
              className="content-absolute content-inset-0 content-z-20 content-flex content-items-center content-justify-center content-bg-black content-bg-opacity-50"
              onClick={e => e.stopPropagation()}
            >
              <div className="content-size-12 content-animate-spin content-rounded-full content-border-4 content-border-white content-border-t-transparent" />
            </div>
          )}

          <VideoControls
            showControls={controls.showControls && !videoPlayer.isLoading}
            activeContent={interactiveContent.activeContent}
            currentTime={videoPlayer.currentTime}
            duration={videoPlayer.duration}
            maxWatchedTime={interactiveContent.maxWatchedTime}
            isPlaying={videoPlayer.isPlaying}
            isMuted={videoPlayer.isMuted}
            volume={videoPlayer.volume}
            showVolumeControls={controls.showVolumeControls}
            isFullscreen={fullscreen.isFullscreen}
            caption={caption}
            contentType={content?.common?.contentType}
            isMpdFile={isMpdFile}
            showSettings={controls.showSettings}
            availableQualities={videoQuality.availableQualities}
            currentQuality={videoQuality.currentQuality}
            sortedContentList={sortedContentList}
            unlockedMarkers={interactiveContent.unlockedMarkers}
            nextUnlockMarker={interactiveContent.nextUnlockMarker}
            progressBarProps={{
              isHovering: progressBar.isHovering,
              hoverPosition: progressBar.hoverPosition,
              isDragging: progressBar.isDragging,
              onHover: (e: MouseEvent<HTMLDivElement>) =>
                progressBar.handleProgressBarHover(
                  e,
                  videoPlayer.duration,
                  interactiveContent.maxWatchedTime,
                  canSeekAnywhere,
                ),
              onLeave: progressBar.handleProgressBarLeave,
              onMouseDown: () => {
                if (canSeekAnywhere) {
                  progressBar.handleProgressBarMouseDown();
                }
              },
              onMouseUp: progressBar.handleProgressBarMouseUp,
            }}
            allowSeekAnywhere={canSeekAnywhere}
            availableLanguages={availableLanguages}
            currentLanguageCode={selectedLanguageCode}
            onPlayPause={handlePlayPause}
            onSkip={handleSkip}
            onToggleMute={videoPlayer.toggleMute}
            onVolumeChange={videoPlayer.setVolumeLevel}
            onVolumeMouseEnter={handleVolumeMouseEnter}
            onVolumeMouseLeave={handleVolumeMouseLeave}
            onToggleCaption={handleToggleCaption}
            onToggleFullscreen={handleFullscreenToggle}
            onToggleSettings={controls.toggleSettings}
            onQualityChange={index =>
              videoQuality.changeQuality(index, videoPlayer.setIsLoading)
            }
            onLanguageChange={handleLanguageChange}
            onProgressBarClick={handleProgressBarClick}
            onProgressBarDrag={handleProgressBarDrag}
            onMarkerClick={handleContentMarkerClick}
          />
        </div>
      </div>

      <InteractiveOverlay
        activeContent={interactiveContent.activeContent}
        contentRenderer={(content, handleNext) =>
          contentRenderer?.(content, handleNext)
        }
        onNext={handleNextButtonClick}
      />
    </div>
  );
};

export default CustomVideoPlayer;
