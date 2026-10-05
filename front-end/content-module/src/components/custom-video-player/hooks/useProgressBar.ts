import { MouseEvent, useEffect, useState } from 'react';

export const useProgressBar = () => {
  const [isHovering, setIsHovering] = useState<boolean>(false);
  const [hoverPosition, setHoverPosition] = useState<number>(0);
  const [isDragging, setIsDragging] = useState<boolean>(false);

  const handleProgressBarHover = (
    e: MouseEvent<HTMLDivElement>,
    duration: number,
    maxWatchedTime: number,
    allowSeekAnywhere = false,
  ) => {
    const progressBar = e.currentTarget;
    const rect = progressBar.getBoundingClientRect();
    const hoverPositionX = e.clientX - rect.left;
    const hoverPercent = hoverPositionX / rect.width;
    const hoverTime = hoverPercent * duration;

    if (allowSeekAnywhere || hoverTime <= maxWatchedTime) {
      setHoverPosition(hoverPercent);
      setIsHovering(true);
    } else {
      setIsHovering(false);
    }
  };

  const handleProgressBarLeave = () => {
    setIsHovering(false);
  };

  const handleProgressBarMouseDown = () => {
    setIsDragging(true);
  };

  const handleProgressBarMouseUp = () => {
    setIsDragging(false);
  };

  useEffect(() => {
    if (!isDragging) return;

    const handleMouseUp = () => setIsDragging(false);
    document.addEventListener('mouseup', handleMouseUp);

    return () => {
      document.removeEventListener('mouseup', handleMouseUp);
    };
  }, [isDragging]);

  return {
    isHovering,
    hoverPosition,
    isDragging,
    handleProgressBarHover,
    handleProgressBarLeave,
    handleProgressBarMouseDown,
    handleProgressBarMouseUp,
  };
};
