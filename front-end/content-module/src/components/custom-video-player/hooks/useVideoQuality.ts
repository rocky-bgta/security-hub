import { useEffect, useState, RefObject } from 'react';
import * as dashjs from 'dashjs';

export interface VideoQuality {
  id: string;
  bitrate: number;
  resolution: string;
}

export const useVideoQuality = (
  dashPlayerRef: RefObject<dashjs.MediaPlayerClass | null>,
  videoRef: RefObject<HTMLVideoElement | null>,
  isPlaying: boolean,
  isMpdFile: boolean,
) => {
  const [availableQualities, setAvailableQualities] = useState<VideoQuality[]>(
    [],
  );
  const [currentQuality, setCurrentQuality] = useState<number>(0);

  useEffect(() => {
    if (!dashPlayerRef.current || !isMpdFile) return;

    const player = dashPlayerRef.current;

    const handlePlaybackTimeUpdated = () => {
      try {
        const qualities = player.getRepresentationsByType('video');
        if (qualities && qualities.length > 0) {
          const mappedQualities = qualities.map((quality: any) => {
            let bitrate = quality.bitrate;
            let resolution = 'Auto';

            if (!bitrate || bitrate === 0) {
              const match = quality.id.match(/_(\d+)k$/);
              if (match && match[1]) {
                bitrate = parseInt(match[1]) * 1000;
              }
            }

            if (quality && quality.height && quality.width) {
              resolution = `${quality.height}p`;
            } else {
              const resolutionMatch = quality.id.match(/_(\d+)x(\d+)_/);
              if (resolutionMatch && resolutionMatch[2]) {
                const height = parseInt(resolutionMatch[2]);
                resolution = `${height}p`;
              }
            }

            return { id: quality.id, bitrate: bitrate || 0, resolution };
          });

          const uniqueQualitiesObject: Record<string, VideoQuality> =
            mappedQualities.reduce(
              (acc: Record<string, VideoQuality>, quality) => {
                if (
                  !acc[quality.resolution] ||
                  quality.bitrate > acc[quality.resolution].bitrate
                ) {
                  acc[quality.resolution] = quality;
                }
                return acc;
              },
              {},
            );

          const uniqueQualities = Object.values(uniqueQualitiesObject);
          if (!uniqueQualities.some(q => q.resolution === 'Auto')) {
            uniqueQualities.unshift({
              id: 'auto',
              bitrate: 0,
              resolution: 'Auto',
            });
          }

          setAvailableQualities(uniqueQualities);
        }
      } catch (error) {
        console.error('Error getting video qualities:', error);
      }
    };

    player.on('playbackTimeUpdated', handlePlaybackTimeUpdated);

    return () => {
      player.off('playbackTimeUpdated', handlePlaybackTimeUpdated);
    };
  }, [dashPlayerRef, isMpdFile]);

  const changeQuality = (
    index: number,
    setIsLoading: (loading: boolean) => void,
  ) => {
    if (!isMpdFile || !dashPlayerRef.current || !videoRef.current) return;

    setIsLoading(true);
    try {
      const player = dashPlayerRef.current;
      const qualities: dashjs.Representation[] =
        player.getRepresentationsByType('video');

      setCurrentQuality(index);

      if (qualities.length > 0 && qualities[index]) {
        const currentTime = videoRef.current.currentTime;
        const settings = player.getSettings();

        if (settings.streaming?.abr) {
          settings.streaming.abr.autoSwitchBitrate = { video: index === 0 };
        }

        player.updateSettings(settings);
        player.setRepresentationForTypeById('video', qualities[index].id);

        videoRef.current.currentTime = currentTime;

        if (isPlaying) {
          player.play();
        }
      }
    } catch (error) {
      console.error('Error changing video quality:', error);
    }

    setTimeout(() => setIsLoading(false), 1000);
  };

  return { availableQualities, currentQuality, changeQuality };
};
