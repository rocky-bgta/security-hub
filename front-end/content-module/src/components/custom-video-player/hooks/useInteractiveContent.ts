import { useEffect, useState } from 'react';

import { IInteractiveContent } from 'models/Content';
import { timeToSeconds } from 'utils/Helper';

export const useInteractiveContent = (
  sortedContentList: IInteractiveContent[],
  duration: number,
) => {
  const [maxWatchedTime, setMaxWatchedTime] = useState<number>(0);
  const [unlockedMarkers, setUnlockedMarkers] = useState<number[]>([]);
  const [nextUnlockMarker, setNextUnlockMarker] = useState<number | null>(null);
  const [activeContent, setActiveContent] =
    useState<IInteractiveContent | null>(null);

  // Initialize first marker
  useEffect(() => {
    if (sortedContentList.length > 0) {
      const firstMarkerTime = timeToSeconds(sortedContentList[0].time);
      setNextUnlockMarker(firstMarkerTime);
      setMaxWatchedTime(firstMarkerTime);
    }
  }, [sortedContentList]);

  // Update next unlock marker
  useEffect(() => {
    if (unlockedMarkers.length > 0 && sortedContentList.length > 0) {
      const lastUnlockedTime = Math.max(...unlockedMarkers);
      const lastUnlockedIndex = sortedContentList.findIndex(
        item => timeToSeconds(item.time) === lastUnlockedTime,
      );

      if (lastUnlockedIndex < sortedContentList.length - 1) {
        const nextMarkerTime = timeToSeconds(
          sortedContentList[lastUnlockedIndex + 1].time,
        );
        setNextUnlockMarker(nextMarkerTime);
        setMaxWatchedTime(nextMarkerTime);
      } else {
        setNextUnlockMarker(null);
        setMaxWatchedTime(duration);
      }
    }
  }, [unlockedMarkers, sortedContentList, duration]);

  // Function to check and update content at current time
  const checkContentAtTime = (currentTime: number) => {
    if (nextUnlockMarker !== null && currentTime >= nextUnlockMarker) {
      setMaxWatchedTime(nextUnlockMarker);
    }

    const CONTENT_PREMATCH_TOLERANCE = 0.1;
    const CONTENT_POSTMATCH_TOLERANCE = 0.5;
    const CONTENT_CLEAR_TOLERANCE = 0.5;

    // Find content at current time
    const currentContent = sortedContentList.find(item => {
      try {
        const itemTimeInSeconds = timeToSeconds(item.time);
        if (unlockedMarkers.includes(itemTimeInSeconds)) {
          return false;
        }

        return (
          currentTime >= itemTimeInSeconds - CONTENT_PREMATCH_TOLERANCE &&
          currentTime <= itemTimeInSeconds + CONTENT_POSTMATCH_TOLERANCE
        );
      } catch (error) {
        console.error('Error processing content time:', error);
        return false;
      }
    });

    // Handle content display
    if (currentContent && !activeContent) {
      setActiveContent(currentContent);
      return true; // Indicates we should pause
    } else if (!currentContent && activeContent) {
      // Check if we're near any content before clearing
      const isNearAnyContent = sortedContentList.some(item => {
        try {
          const itemTimeInSeconds = timeToSeconds(item.time);
          return (
            Math.abs(currentTime - itemTimeInSeconds) < CONTENT_CLEAR_TOLERANCE
          );
        } catch (error) {
          console.error('Error checking nearby content:', error);
          return false;
        }
      });

      if (!isNearAnyContent) {
        setActiveContent(null);
      }
    }

    return false;
  };

  const unlockMarker = (markerTime: number) => {
    if (!unlockedMarkers.includes(markerTime)) {
      setUnlockedMarkers(prev => [...prev, markerTime]);
    }
  };

  return {
    maxWatchedTime,
    unlockedMarkers,
    nextUnlockMarker,
    activeContent,
    setActiveContent,
    unlockMarker,
    checkContentAtTime,
  };
};
