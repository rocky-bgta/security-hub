import { useEffect } from 'react';

import {
  IContent,
  IntervalType,
  IVideoContent,
  ProcessingStatus,
} from 'models/Content';
import CustomVideoPlayer from 'components/custom-video-player/CustomVideoPlayer';

interface IProps {
  content: IContent<IVideoContent>;
  handleVideoPolling: (action: IntervalType, id: string) => void;
}

const VideoBlock = ({ content, handleVideoPolling }: IProps) => {
  const isProcessing =
    content?.specific?.interactiveVideo?.processingStatus !==
    ProcessingStatus.PROCESSED;
  const pollingId = content?.specific?.interactiveVideo?.id;

  useEffect(() => {
    handleVideoPolling(
      isProcessing ? IntervalType.START : IntervalType.CLEAR,
      pollingId,
    );

    return () => handleVideoPolling(IntervalType.CLEAR, pollingId);
  }, [isProcessing]);

  return (
    <>
      {!content?.specific?.interactiveVideo?.videoUrl ? null : isProcessing ? (
        <div className="content-flex content-h-full content-items-center content-justify-center">
          <p className="content-text-white">Video is processing...</p>
        </div>
      ) : (
        <CustomVideoPlayer content={content} />
      )}
    </>
  );
};

export default VideoBlock;
