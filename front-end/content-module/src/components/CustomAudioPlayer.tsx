import { RefObject, useEffect, useState } from 'react';
import { AiFillSound } from 'react-icons/ai';
import { FaPause, FaPlay } from 'react-icons/fa';

interface CustomAudioPlayerProps {
  audioUrl: string;
  audioRef: RefObject<HTMLAudioElement | null>;
  autoPlay?: boolean;
}

const CustomAudioPlayer = ({
  audioUrl,
  audioRef,
  autoPlay,
}: CustomAudioPlayerProps) => {
  const [isPlaying, setIsPlaying] = useState<boolean>(false);
  const [duration, setDuration] = useState<number>(0);
  const [currentTime, setCurrentTime] = useState<number>(0);

  useEffect(() => {
    setIsPlaying(false);
  }, [audioUrl]);

  const handlePlayPause = () => {
    if (audioRef.current) {
      if (isPlaying) {
        audioRef.current.pause();
      } else {
        audioRef.current.play();
      }
      setIsPlaying(!isPlaying);
    }
  };

  const handleTimeUpdate = () => {
    if (audioRef.current) {
      setCurrentTime(audioRef.current.currentTime);
      setDuration(audioRef.current.duration || 0);
    }
  };

  return (
    <div className="content-flex content-w-full content-items-center content-justify-center">
      <audio
        ref={audioRef}
        src={audioUrl}
        autoPlay={autoPlay}
        onTimeUpdate={handleTimeUpdate}
        onEnded={() => setIsPlaying(false)}
      >
        Your browser does not support the audio element.
      </audio>

      <div className="content-flex content-w-full content-items-center content-space-x-4">
        <button
          onClick={handlePlayPause}
          className="content-text-3xl content-text-white"
        >
          {isPlaying ? (
            <FaPause className="content-text-sm content-text-white" />
          ) : (
            <FaPlay className="content-text-sm content-text-white" />
          )}
        </button>

        <div className="content-flex content-w-full content-items-center content-space-x-2">
          <input
            type="range"
            min="0"
            max={duration}
            step="0.01"
            value={currentTime}
            onChange={e => {
              if (audioRef.current) {
                audioRef.current.currentTime = parseFloat(e.target.value);
              }
            }}
            className="content-secondary-primary content-h-1 content-w-full"
          />
          <p className="content-w-10 content-text-xs content-text-white">
            {Math.floor(currentTime)} / {Math.floor(duration)}
          </p>
        </div>

        <div className="content-flex content-items-center content-space-x-2">
          <AiFillSound className="content-text-sm content-text-white" />
          <input
            type="range"
            min="0"
            max="1"
            step="0.1"
            onChange={e => {
              if (audioRef.current) {
                audioRef.current.volume = parseFloat(e.target.value);
              }
            }}
            className="content-secondary-white content-h-1 content-w-24"
          />
        </div>
      </div>
    </div>
  );
};

export default CustomAudioPlayer;
