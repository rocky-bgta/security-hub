import { useEffect } from 'react';

import {
  AllContentTypes,
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
  QuizTypes,
  TContent,
} from 'models/Content';
import LinkBlock from 'pages/content/block/Link';
import MarkdownBlock from 'pages/content/block/Markdown';
import PdfBlock from 'pages/content/block/Pdf';
import QuestionBlock from 'pages/content/block/Question';
import UserHotSpot from 'pages/content/block/quiz/HotSpot';
import UserLikerSelect from 'pages/content/block/quiz/LikerSelect';
import UserMatching from 'pages/content/block/quiz/Matching';
import UserOrdering from 'pages/content/block/quiz/Ordering';
import UserPasswordCompliance from 'pages/content/block/quiz/PasswordCompliance';
import UserPhishingDetection from 'pages/content/block/quiz/PhishingDetection';
import UserPictureChoice from 'pages/content/block/quiz/PictureChoice';
import UserRansomwareSimulator from 'pages/content/block/quiz/RansomwareSimulator';
import UserScenario from 'pages/content/block/quiz/Scenario';
import SliderLevelBlock from 'pages/content/block/SliderLevel';
import SliderShowBlock from 'pages/content/block/SliderShow';
import StoryBlockBlock from 'pages/content/block/StoryBlock';
import TabbedBlock from 'pages/content/block/Tabbed';
import TextBlock from 'pages/content/block/Text';
import CustomVideoPlayer from 'components/custom-video-player/CustomVideoPlayer';

interface IProps {
  content: IContent<IInteractiveVideoContent>;
  handleVideoPolling?: (action: IntervalType, id: string) => void;
  showCompleteButton?: (type: AllContentTypes) => void;
  courseComplete?: () => void;
}

export const UserRenderQuizBlock = (
  content: IContent<IQuizContent>,
  handleNextContent: () => void,
  courseComplete?: () => void,
) => {
  switch (content.specific.quizType) {
    case QuizTypes.ORDERING:
      return (
        <UserOrdering
          content={content}
          isInteractiveVideo={true}
          handleNextContent={handleNextContent}
          courseComplete={courseComplete}
        />
      );
    case QuizTypes.MATCHING:
      return (
        <UserMatching
          content={content}
          isInteractiveVideo={true}
          handleNextContent={handleNextContent}
          courseComplete={courseComplete}
        />
      );
    case QuizTypes.PICTURE:
      return (
        <UserPictureChoice
          content={content}
          isInteractiveVideo={true}
          handleNextContent={handleNextContent}
          courseComplete={courseComplete}
        />
      );
    case QuizTypes.SCENARIO:
      return (
        <UserScenario
          content={content}
          isInteractiveVideo={true}
          handleNextContent={handleNextContent}
          courseComplete={courseComplete}
        />
      );
    case QuizTypes.LIKER:
      return (
        <UserLikerSelect
          content={content}
          isInteractiveVideo={true}
          handleNextContent={handleNextContent}
          courseComplete={courseComplete}
        />
      );
    case QuizTypes.HOT_SPOT:
      return (
        <UserHotSpot
          content={content}
          isInteractiveVideo={true}
          handleNextContent={handleNextContent}
          courseComplete={courseComplete}
        />
      );
    case QuizTypes.PASSWORD_COMPLIANCE:
      return (
        <UserPasswordCompliance
          content={content}
          isInteractiveVideo={true}
          handleNextContent={handleNextContent}
          courseComplete={courseComplete}
        />
      );
    case QuizTypes.RANSOMWARE_SIMULATOR:
      return (
        <UserRansomwareSimulator
          content={content}
          isInteractiveVideo={true}
          handleNextContent={handleNextContent}
          courseComplete={courseComplete}
        />
      );
    case QuizTypes.PHISHING_DETECTION:
      return (
        <UserPhishingDetection
          content={content}
          isInteractiveVideo={true}
          handleNextContent={handleNextContent}
          courseComplete={courseComplete}
        />
      );
    default:
      return null;
  }
};

const ContentRenderer = (
  content: IContent<TContent>,
  handleNextContent: () => void,
  courseComplete?: () => void,
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
          courseComplete={courseComplete}
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
          isInteractiveVideo={true}
          handleNextContent={handleNextContent}
          content={content as IContent<IQuestionContent>}
          courseComplete={courseComplete}
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
          courseComplete={courseComplete}
        />
      );
    case ContentTypes.STORY_BLOCK:
      return (
        <StoryBlockBlock
          isInteractive={true}
          handleNextContent={handleNextContent}
          content={content as IContent<IStoryBlockContent>}
          courseComplete={courseComplete}
        />
      );
    case ContentTypes.SLIDER_LEVEL:
      return (
        <SliderLevelBlock
          isInteractive={true}
          handleNextContent={handleNextContent}
          content={content as IContent<ISliderLevelContent>}
          courseComplete={courseComplete}
        />
      );
    case ContentTypes.TABBED:
      return (
        <TabbedBlock
          isInteractive={true}
          handleNextContent={handleNextContent}
          content={content as IContent<ITabbedContent>}
          courseComplete={courseComplete}
        />
      );
    case ContentTypes.QUIZ:
      return UserRenderQuizBlock(
        content as IContent<IQuizContent>,
        handleNextContent,
        courseComplete,
      );
    default:
      return null;
  }
};

const InteractiveVideo = ({
  content,
  handleVideoPolling,
  showCompleteButton,
  courseComplete,
}: IProps) => {
  const primaryLanguageVideo =
    content?.specific?.interactiveVideoByLanguage?.find(
      video => video.default,
    ) || content?.specific?.interactiveVideoByLanguage?.[0];

  const isProcessing =
    content?.specific?.interactiveVideoByLanguage?.some(
      video => video.processingStatus !== ProcessingStatus.PROCESSED,
    ) ?? false;

  const pollingId = content?.id;

  useEffect(() => {
    if (pollingId && handleVideoPolling) {
      handleVideoPolling(
        isProcessing ? IntervalType.START : IntervalType.CLEAR,
        pollingId,
      );

      return () => handleVideoPolling(IntervalType.CLEAR, pollingId);
    }
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
          showCompleteButton={showCompleteButton}
          courseComplete={courseComplete}
        />
      )}
    </>
  );
};

export default InteractiveVideo;
