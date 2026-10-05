import * as dashjs from 'dashjs';
import { useEffect, useRef, useState } from 'react';

interface UseVideoPlayerProps {
  videoUrl: string;
  isMpdFile: boolean;
}

export const useVideoPlayer = ({
  videoUrl,
  isMpdFile,
}: UseVideoPlayerProps) => {
  const videoRef = useRef<HTMLVideoElement>(null);
  const dashPlayerRef = useRef<dashjs.MediaPlayerClass | null>(null);
  const [isPlaying, setIsPlaying] = useState<boolean>(false);
  const [currentTime, setCurrentTime] = useState<number>(0);
  const [duration, setDuration] = useState<number>(0);
  const [isMuted, setIsMuted] = useState<boolean>(false);
  const [volume, setVolume] = useState<number>(1);
  const [isLoading, setIsLoading] = useState<boolean>(false);

  // Show loader whenever the source changes (e.g. language switch).
  useEffect(() => {
    if (!videoUrl) return;
    setIsLoading(true);
    setCurrentTime(0);
    setDuration(0);
  }, [videoUrl]);

  // Native video sources: clear loader once enough data is ready.
  useEffect(() => {
    const video = videoRef.current;
    if (!video || isMpdFile || !videoUrl) return;

    const handleCanPlay = () => setIsLoading(false);
    const handleError = () => setIsLoading(false);

    if (video.readyState >= HTMLMediaElement.HAVE_FUTURE_DATA) {
      setIsLoading(false);
    }

    video.addEventListener('canplay', handleCanPlay);
    video.addEventListener('error', handleError);

    return () => {
      video.removeEventListener('canplay', handleCanPlay);
      video.removeEventListener('error', handleError);
    };
  }, [videoUrl, isMpdFile]);

  // Initialize dash.js player
  useEffect(() => {
    if (!videoUrl || !videoRef.current || !isMpdFile) return;

    const player = dashjs.MediaPlayer().create() as dashjs.MediaPlayerClass;
    dashPlayerRef.current = player;

    player.setXHRWithCredentialsForType('MPD', true);
    player.setXHRWithCredentialsForType('MediaSegment', true);
    player.setXHRWithCredentialsForType('InitializationSegment', true);
    player.initialize(videoRef.current, videoUrl, false);

    const handleCanPlay = () => setIsLoading(false);
    const handleError = (error: unknown) => {
      console.error('DASH player error:', error);
      setIsLoading(false);
    };

    player.on('timeupdate', () => {
      if (videoRef.current) {
        setCurrentTime(videoRef.current.currentTime);
      }
    });

    player.on('durationchange', () => {
      if (videoRef.current) {
        setDuration(videoRef.current.duration);
      }
    });

    player.on(dashjs.MediaPlayer.events.CAN_PLAY, handleCanPlay);
    player.on('ended', () => setIsPlaying(false));
    player.on('error', handleError);

    return () => {
      if (dashPlayerRef.current) {
        dashPlayerRef.current.off(
          dashjs.MediaPlayer.events.CAN_PLAY,
          handleCanPlay,
        );
        dashPlayerRef.current.off('error', handleError);
        dashPlayerRef.current.destroy();
        dashPlayerRef.current = null;
      }
    };
  }, [videoUrl, isMpdFile]);

  const play = () => {
    if (!videoRef.current) return;

    if (isMpdFile && dashPlayerRef.current) {
      dashPlayerRef.current.play();
    } else {
      videoRef.current.play();
    }
    setIsPlaying(true);
  };

  const pause = () => {
    if (!videoRef.current) return;

    if (isMpdFile && dashPlayerRef.current) {
      dashPlayerRef.current.pause();
    } else {
      videoRef.current.pause();
    }
    setIsPlaying(false);
  };

  const seek = (time: number) => {
    if (!videoRef.current) return;

    if (isMpdFile && dashPlayerRef.current) {
      dashPlayerRef.current.seek(time);
    } else {
      videoRef.current.currentTime = time;
    }
    setCurrentTime(time);
  };

  const toggleMute = () => {
    if (!videoRef.current) return;

    const newMutedState = !isMuted;
    const newVolume = newMutedState ? 0 : volume;

    if (isMpdFile && dashPlayerRef.current) {
      dashPlayerRef.current.setVolume(newVolume);
    } else {
      videoRef.current.volume = newVolume;
    }
    setIsMuted(newMutedState);
  };

  const setVolumeLevel = (newVolume: number) => {
    if (!videoRef.current) return;

    const clampedVolume = Math.max(0, Math.min(1, newVolume));

    if (isMpdFile && dashPlayerRef.current) {
      dashPlayerRef.current.setVolume(clampedVolume);
    } else {
      videoRef.current.volume = clampedVolume;
    }

    setVolume(clampedVolume);
    setIsMuted(clampedVolume === 0);
  };

  return {
    videoRef,
    dashPlayerRef,
    isPlaying,
    currentTime,
    duration,
    isMuted,
    volume,
    isLoading,
    setIsLoading,
    setCurrentTime,
    setDuration,
    setIsPlaying,
    play,
    pause,
    seek,
    toggleMute,
    setVolumeLevel,
  };
};
