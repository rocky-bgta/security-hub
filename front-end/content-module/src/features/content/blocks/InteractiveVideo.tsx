import { useEffect } from 'react';

import CustomVideoPlayer from 'components/custom-video-player/CustomVideoPlayer';
import LinkBlock from 'features/content/blocks/Link';
import MarkdownBlock from 'features/content/blocks/Markdown';
import PdfBlock from 'features/content/blocks/Pdf';
import QuestionBlock from 'features/content/blocks/Question';
import SliderLevelBlock from 'features/content/blocks/SliderLevel';
import SliderShowBlock from 'features/content/blocks/SliderShow';
import StoryBlockBlock from 'features/content/blocks/StoryBlock';
import TabbedBlock from 'features/content/blocks/Tabbed';
import TextBlock from 'features/content/blocks/Text';
import { renderQuizBlock } from 'features/content/ContentBlocks';
import {
  ContentTypes,
  IContent,
  IInteractiveVideoContent,
  ILinkContent,
  IMarkdownContent,
  IntervalType,
  IPdfContent,
  IQuestionContent,
  IQuizContent,
  ISliderLevelContent,
  ISliderShowContent,
  IStoryBlockContent,
  ITabbedContent,
  ITextContent,
  ProcessingStatus,
  TContent,
} from 'models/Content';

interface IProps {
  content: IContent<IInteractiveVideoContent>;
  handleVideoPolling: (action: IntervalType, id: string) => void;
}

const ContentRenderer = (
  content: IContent<TContent>,
  handleNextContent: () => void,
) => {
  if (!content) return null;

  switch (content.common?.contentType) {
    case ContentTypes.TEXT:
      return (
        <TextBlock
          isInteractive={true}
          handleNextContent={handleNextContent}
          content={content as IContent<ITextContent>}
        />
      );
    case ContentTypes.LINK:
      return (
        <LinkBlock
          isInteractive={true}
          handleNextContent={handleNextContent}
          content={content as IContent<ILinkContent>}
        />
      );
    case ContentTypes.PDF:
      return (
        <PdfBlock
          isInteractive={true}
          handleNextContent={handleNextContent}
          content={content as IContent<IPdfContent>}
        />
      );
    case ContentTypes.QUESTION:
      return (
        <QuestionBlock
          isInteractive={true}
          handleNextContent={handleNextContent}
          content={content as IContent<IQuestionContent>}
        />
      );
    case ContentTypes.MARKDOWN:
      return (
        <MarkdownBlock
          isInteractive={true}
          handleNextContent={handleNextContent}
          content={content as IContent<IMarkdownContent>}
        />
      );
    case ContentTypes.SLIDER_SHOW:
      return (
        <SliderShowBlock
          isInteractive={true}
          handleNextContent={handleNextContent}
          content={content as IContent<ISliderShowContent>}
        />
      );
    case ContentTypes.STORY_BLOCK:
      return (
        <StoryBlockBlock
          isInteractive={true}
          handleNextContent={handleNextContent}
          content={content as IContent<IStoryBlockContent>}
        />
      );
    case ContentTypes.SLIDER_LEVEL:
      return (
        <SliderLevelBlock
          isInteractive={true}
          handleNextContent={handleNextContent}
          content={content as IContent<ISliderLevelContent>}
        />
      );
    case ContentTypes.TABBED:
      return (
        <TabbedBlock
          isInteractive={true}
          handleNextContent={handleNextContent}
          content={content as IContent<ITabbedContent>}
        />
      );
    case ContentTypes.QUIZ:
      return renderQuizBlock(
        content as IContent<IQuizContent>,
        true,
        handleNextContent,
      );
    default:
      return (
        <div className="content-p-4">
          <h3 className="content-mb-2 content-text-lg content-font-bold">
            {content.common?.contentName || 'Content'}
          </h3>
          <p className="content-text-sm content-text-gray-600">
            {content.common?.contentType || 'Unknown'} content will be displayed
            here
          </p>
        </div>
      );
  }
};

const InteractiveVideo = ({ content, handleVideoPolling }: IProps) => {
  const primaryLanguageVideo =
    content?.specific?.interactiveVideoByLanguage?.find(
      video => video.default,
    ) || content?.specific?.interactiveVideoByLanguage?.[0];

  const isProcessing =
    content?.specific?.interactiveVideoByLanguage?.some(
      video => video.processingStatus !== ProcessingStatus.PROCESSED,
    ) ?? false;

  const pollingId = primaryLanguageVideo?.id || content?.id;

  useEffect(() => {
    if (pollingId) {
      handleVideoPolling(
        isProcessing ? IntervalType.START : IntervalType.CLEAR,
        pollingId,
      );
    }

    return () => {
      if (pollingId) handleVideoPolling(IntervalType.CLEAR, pollingId);
    };
  }, [isProcessing, pollingId]);

  return (
    <>
      {!primaryLanguageVideo?.videoUrl ? (
        <div className="content-flex content-h-full content-items-center content-justify-center">
          <p className="content-text-white">No Video URL found</p>
        </div>
      ) : isProcessing ? (
        <div className="content-flex content-h-full content-items-center content-justify-center">
          <p className="content-text-white">Video is processing...</p>
        </div>
      ) : (
        <CustomVideoPlayer
          content={content}
          contentRenderer={ContentRenderer}
        />
      )}
    </>
  );
};

export default InteractiveVideo;
