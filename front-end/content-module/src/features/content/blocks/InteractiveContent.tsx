import { useEffect, useState } from 'react';

import { NextAngelIcon } from 'assets/icons';
import LinkBlock from 'features/content/blocks/Link';
import MarkdownBlock from 'features/content/blocks/Markdown';
import PdfBlock from 'features/content/blocks/Pdf';
import QuestionBlock from 'features/content/blocks/Question';
import SliderLevelBlock from 'features/content/blocks/SliderLevel';
import SliderShowBlock from 'features/content/blocks/SliderShow';
import StoryBlockBlock from 'features/content/blocks/StoryBlock';
import TabbedBlock from 'features/content/blocks/Tabbed';
import TextBlock from 'features/content/blocks/Text';
import VideoBlock from 'features/content/blocks/Video';
import { renderQuizBlock } from 'features/content/ContentBlocks';
import {
  ContentTypes,
  IContent,
  IInteractiveGeneralContent,
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
  IVideoContent,
  ProcessingStatus,
  TContent,
} from 'models/Content';
import { formatTime, timeToSeconds } from 'utils/Helper';

interface IProps {
  content: IContent<IInteractiveGeneralContent>;
  handleVideoPolling: (action: IntervalType, id: string) => void;
}

const ContentRenderer = (
  content: IContent<TContent>,
  handleVideoPolling: (action: IntervalType, id: string) => void,
) => {
  if (!content) return null;

  switch (content.common?.contentType) {
    case ContentTypes.TEXT:
      return <TextBlock content={content as IContent<ITextContent>} />;
    case ContentTypes.LINK:
      return <LinkBlock content={content as IContent<ILinkContent>} />;
    case ContentTypes.PDF:
      return <PdfBlock content={content as IContent<IPdfContent>} />;
    case ContentTypes.QUESTION:
      return <QuestionBlock content={content as IContent<IQuestionContent>} />;
    case ContentTypes.MARKDOWN:
      return <MarkdownBlock content={content as IContent<IMarkdownContent>} />;
    case ContentTypes.SLIDER_SHOW:
      return (
        <SliderShowBlock content={content as IContent<ISliderShowContent>} />
      );
    case ContentTypes.STORY_BLOCK:
      return (
        <StoryBlockBlock content={content as IContent<IStoryBlockContent>} />
      );
    case ContentTypes.SLIDER_LEVEL:
      return (
        <SliderLevelBlock content={content as IContent<ISliderLevelContent>} />
      );
    case ContentTypes.TABBED:
      return <TabbedBlock content={content as IContent<ITabbedContent>} />;
    case ContentTypes.VIDEO:
    case ContentTypes.ANIMATION:
      return (
        <VideoBlock
          content={content as IContent<IVideoContent>}
          handleVideoPolling={handleVideoPolling}
        />
      );
    case ContentTypes.QUIZ:
      return renderQuizBlock(content as IContent<IQuizContent>);
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

const InteractiveContent = ({ content, handleVideoPolling }: IProps) => {
  const [currentStepIndex, setCurrentStepIndex] = useState<number>(0);
  const [completedSteps, setCompletedSteps] = useState<Set<number>>(new Set());
  const currentContent = content?.specific?.contentList[currentStepIndex];
  const initialTime = currentContent?.time ?? '';
  const totalSeconds = timeToSeconds(initialTime);
  const [remainingSeconds, setRemainingSeconds] =
    useState<number>(totalSeconds);
  const [isRunning, setIsRunning] = useState<boolean>(true);
  const [remainingMilliseconds, setRemainingMilliseconds] = useState<number>(
    totalSeconds * 1000,
  );
  const progressPercentage = completedSteps.has(currentStepIndex)
    ? 100
    : ((totalSeconds * 1000 - remainingMilliseconds) / (totalSeconds * 1000)) *
      100;
  const secondsList = content?.specific?.contentList.map(
    content => content.time,
  );
  const totalContentItems = content?.specific?.contentList.length ?? 0;
  const totalContentSeconds = secondsList.reduce(
    (total, currentValue) => total + timeToSeconds(currentValue),
    0,
  );
  const isProcessing = content?.specific?.contentList.some(content => {
    const specific = content?.contentBody?.specific as any;

    if (Array.isArray(specific?.interactiveVideoByLanguage)) {
      return specific.interactiveVideoByLanguage.some(
        (video: { processingStatus: ProcessingStatus }) =>
          video.processingStatus !== ProcessingStatus.PROCESSED,
      );
    }

    return (
      !!specific?.interactiveVideo?.processingStatus &&
      specific.interactiveVideo.processingStatus !== ProcessingStatus.PROCESSED
    );
  });
  const pollingId = content?.id ?? '';

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

  useEffect(() => {
    if (!isRunning || remainingMilliseconds <= 0) return;

    const timerId = setInterval(() => {
      setRemainingMilliseconds(prev => {
        if (prev <= 100) {
          setIsRunning(false);
          setRemainingSeconds(0);
          setCompletedSteps(
            prevCompleted => new Set([...prevCompleted, currentStepIndex]),
          );
          if (
            currentContent?.canSkip &&
            currentStepIndex < totalContentItems - 1
          ) {
            handleNextContent();
          }

          return 0;
        }
        return prev - 100;
      });

      setRemainingSeconds(Math.ceil(remainingMilliseconds / 1000));
    }, 100);

    return () => {
      clearInterval(timerId);
    };
  }, [
    isRunning,
    remainingMilliseconds,
    currentStepIndex,
    currentContent?.canSkip,
    totalContentItems,
  ]);

  // Reset timer when content changes
  useEffect(() => {
    if (completedSteps.has(currentStepIndex)) {
      setRemainingSeconds(0);
      setRemainingMilliseconds(0);
      setIsRunning(false);
    } else {
      const newTime = currentContent?.time;
      if (newTime) {
        const newTotalSeconds = timeToSeconds(newTime);
        setRemainingSeconds(newTotalSeconds);
        setRemainingMilliseconds(newTotalSeconds * 1000);
        setIsRunning(true);
      }
    }
  }, [currentStepIndex, currentContent?.time, completedSteps]);

  const handleNextContent = () => {
    setCurrentStepIndex(prev => prev + 1);
  };

  return (
    <>
      {isProcessing ? (
        <div className="content-flex content-h-full content-items-center content-justify-center">
          <p className="content-text-white">Video is processing...</p>
        </div>
      ) : (
        currentContent && (
          <>
            <div className="content-mb-2 content-h-[550px]">
              {ContentRenderer(currentContent?.contentBody, handleVideoPolling)}
            </div>
            <div className="content-absolute content-right-2 content-top-2 content-z-10 content-text-sm content-text-white">
              {formatTime(totalContentSeconds)}
            </div>
            <div className="content-absolute content-inset-x-2 content-bottom-0 content-z-10">
              <div className="content-flex content-items-center content-justify-between content-gap-2">
                <div className="content-relative content-h-[3px] content-w-full content-overflow-hidden content-rounded-full content-bg-gray-200">
                  <div
                    className="content-h-full content-bg-blue-500 content-transition-all content-duration-100 content-ease-linear"
                    style={{ width: `${progressPercentage}%` }}
                  />
                </div>
                <div className="content-font-mono content-text-sm content-text-white">
                  {formatTime(remainingSeconds)}
                </div>
                <div className="content-flex content-items-center content-gap-2">
                  <button
                    onClick={handleNextContent}
                    className="content-mb-2 content-flex content-size-8 content-items-center content-justify-center content-rounded-full content-bg-[#4a4949] content-text-white content-transition-all content-duration-200 hover:enabled:content-bg-[#3a3a3a] disabled:content-opacity-50"
                    disabled={
                      (!completedSteps.has(currentStepIndex) &&
                        remainingSeconds !== 0) ||
                      currentStepIndex ===
                        content?.specific?.contentList.length - 1
                    }
                  >
                    <NextAngelIcon />
                  </button>
                </div>
              </div>
            </div>
          </>
        )
      )}
    </>
  );
};

export default InteractiveContent;
