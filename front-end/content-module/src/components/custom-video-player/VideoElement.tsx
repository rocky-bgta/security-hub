import { memo, RefObject } from 'react';

interface VideoElementProps {
  videoRef: RefObject<HTMLVideoElement | null>;
  caption: string;
  videoUrl: string;
  isMpdFile: boolean;
  onTimeUpdate: () => void;
  onLoadedMetadata: () => void;
  onEnded: () => void;
}

const VideoElement = ({
  videoRef,
  caption,
  videoUrl,
  isMpdFile,
  onTimeUpdate,
  onLoadedMetadata,
  onEnded,
}: VideoElementProps) => {
  // For MPD files dash.js attaches the source to the element itself, so we
  // only set the native `src` for regular files (mp4/webm/ogg/etc.).
  const nativeSrc = !isMpdFile && videoUrl ? videoUrl : undefined;

  return (
    <video
      ref={videoRef}
      key={nativeSrc}
      src={nativeSrc}
      className="content-size-full content-bg-black content-object-contain"
      playsInline
      onTimeUpdate={onTimeUpdate}
      onLoadedMetadata={onLoadedMetadata}
      onEnded={onEnded}
    >
      <track src={caption} label="English" kind="subtitles" default />
    </video>
  );
};

export default memo(VideoElement);
