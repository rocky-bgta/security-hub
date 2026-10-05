import { useCallback, useEffect, useState } from 'react';
import { FaAngleLeft, FaAngleRight } from 'react-icons/fa';

import ContentCompleteModal from 'common/modal/miniModal/ContentComplete';
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
import LinkBlock from 'pages/content/block/Link';
import MarkdownBlock from 'pages/content/block/Markdown';
import PdfBlock from 'pages/content/block/Pdf';
import QuestionBlock from 'pages/content/block/Question';
import SliderLevelBlock from 'pages/content/block/SliderLevel';
import SliderShowBlock from 'pages/content/block/SliderShow';
import StoryBlockBlock from 'pages/content/block/StoryBlock';
import TabbedBlock from 'pages/content/block/Tabbed';
import TextBlock from 'pages/content/block/Text';
import VideoBlock from 'pages/content/block/Video';
import { renderQuizBlock } from 'pages/content/ContentView';
import { formatTime, timeToSeconds } from 'utils/Helper';

interface IProps {
  content: IContent<IInteractiveGeneralContent>;
  handleVideoPolling?: (action: IntervalType, id: string) => void;
  courseComplete?: () => void;
}

const ContentRenderer = (
  content: IContent<TContent>,
  enableNextButton: () => void,
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
      return (
        <QuestionBlock
          content={content as IContent<IQuestionContent>}
          courseComplete={enableNextButton}
        />
      );
    case ContentTypes.MARKDOWN:
      return <MarkdownBlock content={content as IContent<IMarkdownContent>} />;
    case ContentTypes.SLIDER_SHOW:
      return (
        <SliderShowBlock
          content={content as IContent<ISliderShowContent>}
          courseComplete={enableNextButton}
        />
      );
    case ContentTypes.STORY_BLOCK:
      return (
        <StoryBlockBlock
          content={content as IContent<IStoryBlockContent>}
          courseComplete={enableNextButton}
        />
      );
    case ContentTypes.SLIDER_LEVEL:
      return (
        <SliderLevelBlock
          content={content as IContent<ISliderLevelContent>}
          courseComplete={enableNextButton}
        />
      );
    case ContentTypes.TABBED:
      return (
        <TabbedBlock
          content={content as IContent<ITabbedContent>}
          courseComplete={enableNextButton}
        />
      );
    case ContentTypes.VIDEO:
    case ContentTypes.ANIMATION:
      return (
        <VideoBlock
          content={content as IContent<IVideoContent>}
          courseComplete={enableNextButton}
        />
      );
    case ContentTypes.QUIZ:
      return renderQuizBlock(
        content as IContent<IQuizContent>,
        enableNextButton,
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

const showDefaultCompleteButton = [
  ContentTypes.TEXT,
  ContentTypes.LINK,
  ContentTypes.PDF,
  ContentTypes.MARKDOWN,
  // ContentTypes.VIDEO,
  // ContentTypes.ANIMATION,
];

const InteractiveContent = ({
  content,
  handleVideoPolling,
  courseComplete,
}: IProps) => {
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
  const [enabledNextButton, setEnableNextButton] = useState<string[]>([]);

  const [showContentCompleteModal, setShowContentCompleteModal] =
    useState<boolean>(false);
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
  const isProcessing =
    content?.specific?.interactiveVideo?.processingStatus !==
    ProcessingStatus.PROCESSED;

  const pollingId = content?.id ?? '';

  const contentTypeOrQuizType =
    currentContent?.contentBody?.common?.contentType ?? '';

  const isEnabledType =
    enabledNextButton.includes(currentContent?.id || '') ||
    showDefaultCompleteButton.includes(contentTypeOrQuizType);
  const shouldDisable =
    (!completedSteps.has(currentStepIndex) && remainingSeconds !== 0) ||
    !isEnabledType;
  const isLastStep =
    currentStepIndex === content?.specific?.contentList.length - 1;

  const disabled =
    (isLastStep &&
      !showDefaultCompleteButton.includes(contentTypeOrQuizType)) ||
    (isEnabledType ? shouldDisable : true);

  const isVideoOrAnimation =
    currentContent.contentType === ContentTypes.VIDEO ||
    currentContent.contentType === ContentTypes.ANIMATION;

  useEffect(() => {
    if (pollingId && handleVideoPolling) {
      handleVideoPolling(
        isProcessing ? IntervalType.START : IntervalType.CLEAR,
        pollingId,
      );

      return () => {
        if (pollingId && handleVideoPolling) {
          handleVideoPolling(IntervalType.CLEAR, pollingId);
        }
      };
    }
  }, [isProcessing, handleVideoPolling, pollingId]);

  const handleNextContent = useCallback(() => {
    if (isLastStep) {
      courseComplete?.();
    } else {
      setCurrentStepIndex(prev => prev + 1);
    }
  }, [courseComplete, isLastStep]);

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
    handleNextContent,
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

  const handlePrevContent = useCallback(() => {
    setCurrentStepIndex(prev => Math.max(0, prev - 1));
  }, []);

  const handleCompleteButton = () => {
    if (enabledNextButton.includes(currentContent.id || '')) return;

    setEnableNextButton(prev => [...prev, currentContent?.id || '']);

    if (!isVideoOrAnimation && !isLastStep) {
      setShowContentCompleteModal(true);
    }
    if (isLastStep) {
      courseComplete?.();
    }
  };

  return (
    <>
      {isProcessing ? (
        <div className="content-flex content-h-full content-items-center content-justify-center">
          <p className="content-text-white">Video is processing...</p>
        </div>
      ) : (
        currentContent && (
          <div className="content-relative content-flex content-flex-col content-justify-between content-px-6 content-pt-6">
            <div className="content-mb-2 content-mt-1 content-h-full">
              {ContentRenderer(
                currentContent?.contentBody,
                handleCompleteButton,
              )}
            </div>
            <div className="content-absolute content-right-2 content-top-0 content-z-10 content-text-sm content-text-white">
              {formatTime(totalContentSeconds)}
            </div>
            <div className="content-px-6 content-pt-4">
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
                    onClick={handlePrevContent}
                    className="content-mb-2 content-flex content-size-8 content-cursor-pointer content-items-center content-justify-center content-rounded-full content-bg-primary content-text-white content-transition-all content-duration-200 hover:enabled:content-bg-primary/90 disabled:content-bg-[#4a4949]/50 disabled:content-text-white/50"
                    disabled={currentStepIndex === 0}
                  >
                    <FaAngleLeft className="content-text-lg" />
                  </button>
                  <button
                    onClick={handleNextContent}
                    className="content-mb-2 content-flex content-size-8 content-cursor-pointer content-items-center content-justify-center content-rounded-full content-bg-primary content-text-white content-transition-all content-duration-200 hover:enabled:content-bg-primary/90 disabled:content-bg-[#4a4949]/50 disabled:content-text-white/50"
                    disabled={disabled}
                  >
                    <FaAngleRight className="content-text-lg" />
                  </button>
                </div>
              </div>
            </div>
            {showContentCompleteModal && (
              <ContentCompleteModal
                isCorrect={true}
                isOpen={showContentCompleteModal}
                onClose={() => setShowContentCompleteModal(false)}
                onClick={() => {
                  handleNextContent();
                  if (!completedSteps.has(currentStepIndex)) {
                    setCompletedSteps(
                      prevCompleted =>
                        new Set([...prevCompleted, currentStepIndex]),
                    );
                  }
                }}
              />
            )}
          </div>
        )
      )}
    </>
  );
};

export default InteractiveContent;
