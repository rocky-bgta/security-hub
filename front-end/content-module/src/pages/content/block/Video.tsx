import { useEffect, useState } from 'react';

import CustomVideoPlayer from 'components/custom-video-player/CustomVideoPlayer';
import { useAPI } from 'hooks/UseAPI';
import {
  IContent,
  IntervalType,
  IVideoContent,
  ProcessingStatus,
} from 'models/Content';

interface IProps {
  content: IContent<IVideoContent>;
  handleVideoPolling?: (action: IntervalType, id: string) => void;
  courseComplete?: () => void;
}

const VideoBlock = ({
  content,
  handleVideoPolling,
  courseComplete,
}: IProps) => {
  const apiClient = useAPI();
  const isProcessing =
    content?.specific?.interactiveVideo?.processingStatus !==
    ProcessingStatus.PROCESSED;
  const pollingId = content?.specific?.interactiveVideo?.id;

  useEffect(() => {
    if (handleVideoPolling) {
      handleVideoPolling(
        isProcessing ? IntervalType.START : IntervalType.CLEAR,
        pollingId,
      );

      return () => handleVideoPolling(IntervalType.CLEAR, pollingId);
    }
  }, [isProcessing]);

  const [captions, setCaptions] = useState('');

  useEffect(() => {
    if (!content.specific?.captionUrl) return;

    const fetchCaptions = async () => {
      try {
        const response: string = await apiClient.get(
          content.specific.captionUrl,
        );
        const cleaned = response
          .split('\n')
          .filter(
            line =>
              line.trim() !== 'WEBVTT' &&
              !line.match(/^[0-9]+$/) &&
              !line.includes('-->') &&
              line.trim() !== '',
          )
          .join('\n');
        setCaptions(cleaned);
      } catch (error) {
        console.error('Error fetching captions:', error);
      }
    };

    fetchCaptions();
  }, [content.specific?.captionUrl, apiClient]);

  return (
    <>
      {!content?.specific?.interactiveVideo?.videoUrl ? null : isProcessing ? (
        <div className="content-flex content-items-center content-justify-center">
          <p className="content-text-white">Video is processing...</p>
        </div>
      ) : (
        <div className="content-w-full">
          <div className="content-aspect-video content-w-full content-overflow-hidden">
            <CustomVideoPlayer
              content={content}
              courseComplete={courseComplete}
            />
          </div>
          <div className="content-px-4 sm:content-px-6">
            <h1 className="content-mb-4 content-break-words content-text-xl content-font-semibold content-text-white sm:content-text-2xl">
              {content.specific?.metadata.additionalProp1.videoText}
            </h1>
            {content.specific?.captionUrl && (
              <>
                <div className="content-mb-2 content-border-b content-border-white content-pb-2">
                  <span className="content-border-b-4 content-border-primary content-pb-2 content-text-white">
                    Transcript
                  </span>
                </div>
                <p className="content-text-sm content-text-white">{captions}</p>
              </>
            )}
          </div>
        </div>
      )}
    </>
  );
};

export default VideoBlock;
