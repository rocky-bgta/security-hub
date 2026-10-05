import { useState, useRef, useEffect } from 'react';

export const useVideoControls = (isPlaying: boolean) => {
  const [showControls, setShowControls] = useState<boolean>(true);
  const [showSettings, setShowSettings] = useState<boolean>(false);
  const [showVolumeControls, setShowVolumeControls] = useState<boolean>(false);
  const controlsTimeoutRef = useRef<NodeJS.Timeout | null>(null);
  const settingsTimeoutRef = useRef<NodeJS.Timeout | null>(null);
  const volumeTimeoutRef = useRef<NodeJS.Timeout | null>(null);

  const handleMouseMove = () => {
    setShowControls(true);

    if (controlsTimeoutRef.current) {
      clearTimeout(controlsTimeoutRef.current);
    }

    controlsTimeoutRef.current = setTimeout(() => {
      if (isPlaying) {
        setShowControls(false);
      }
    }, 3000);
  };

  const toggleSettings = () => {
    setShowSettings(!showSettings);

    if (settingsTimeoutRef.current) {
      clearTimeout(settingsTimeoutRef.current);
    }

    settingsTimeoutRef.current = setTimeout(() => {
      setShowSettings(false);
    }, 3000);
  };

  useEffect(() => {
    return () => {
      if (controlsTimeoutRef.current) clearTimeout(controlsTimeoutRef.current);
      if (settingsTimeoutRef.current) clearTimeout(settingsTimeoutRef.current);
      if (volumeTimeoutRef.current) clearTimeout(volumeTimeoutRef.current);
    };
  }, []);

  return {
    showControls,
    showSettings,
    showVolumeControls,
    setShowControls,
    setShowSettings,
    setShowVolumeControls,
    volumeTimeoutRef,
    handleMouseMove,
    toggleSettings,
  };
};
