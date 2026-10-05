import { RefObject, useEffect } from 'react';

import { DeleteIcon, EditIcon } from 'assets/icons';
import { Button } from 'common/Button';
import BlockWrapper from 'components/BlockWrapper';
import InteractiveContent from 'features/content/blocks/InteractiveContent';
import InteractiveVideo from 'features/content/blocks/InteractiveVideo';
import LinkBlock from 'features/content/blocks/Link';
import Markdown from 'features/content/blocks/Markdown';
import PdfBlock from 'features/content/blocks/Pdf';
import QuestionBlock from 'features/content/blocks/Question';
import HotSpotBlock from 'features/content/blocks/quiz/HotSpot';
import LikerSelectBlock from 'features/content/blocks/quiz/LikerSelectBlock';
import MatchingBlock from 'features/content/blocks/quiz/Matching';
import OrderingBlock from 'features/content/blocks/quiz/Ordering';
import PasswordComplianceBlock from 'features/content/blocks/quiz/PasswordCompliance';
import PhishingDetectionBlock from 'features/content/blocks/quiz/PhishingDetection';
import PictureBlock from 'features/content/blocks/quiz/Pictures';
import RansomwareSimulatorBlock from 'features/content/blocks/quiz/RansomwareSimulator';
import ScenarioBlock from 'features/content/blocks/quiz/Scenario';
import SliderLevel from 'features/content/blocks/SliderLevel';
import SliderShow from 'features/content/blocks/SliderShow';
import StoryBlock from 'features/content/blocks/StoryBlock';
import Tabbed from 'features/content/blocks/Tabbed';
import TextBlock from 'features/content/blocks/Text';
import VideoBlock from 'features/content/blocks/Video';
import {
  ContentTypes,
  IContent,
  IInteractiveGeneralContent,
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
  IVideoContent,
  QuizTypes,
  TContent,
} from 'models/Content';

interface IProps {
  sectionRefs: RefObject<Array<HTMLDivElement>>;
  contents: Array<IContent<TContent>>;
  onClickDelete: (id: string) => void;
  onClickEdit: (id: string) => void;
  handleVideoPolling: (action: IntervalType, id: string) => void;
}

export const renderQuizBlock = (
  content: IContent<IQuizContent>,
  isInteractive?: boolean,
  handleNextContent?: () => void,
) => {
  switch (content.specific.quizType) {
    case QuizTypes.ORDERING:
      return (
        <OrderingBlock
          content={content}
          isInteractive={isInteractive}
          handleNextContent={handleNextContent}
        />
      );
    case QuizTypes.MATCHING:
      return (
        <MatchingBlock
          content={content}
          isInteractive={isInteractive}
          handleNextContent={handleNextContent}
        />
      );
    case QuizTypes.PICTURE:
      return (
        <PictureBlock
          content={content}
          isInteractive={isInteractive}
          handleNextContent={handleNextContent}
        />
      );
    case QuizTypes.SCENARIO:
      return (
        <ScenarioBlock
          content={content}
          isInteractiveVideo={isInteractive}
          handleNextContent={handleNextContent}
        />
      );
    case QuizTypes.LIKER:
      return (
        <LikerSelectBlock
          content={content}
          isInteractive={isInteractive}
          handleNextContent={handleNextContent}
        />
      );
    case QuizTypes.HOT_SPOT:
      return (
        <HotSpotBlock
          content={content}
          isInteractive={isInteractive}
          handleNextContent={handleNextContent}
        />
      );
    case QuizTypes.PASSWORD_COMPLIANCE:
      return (
        <PasswordComplianceBlock
          content={content}
          isInteractive={isInteractive}
          handleNextContent={handleNextContent}
        />
      );
    case QuizTypes.RANSOMWARE_SIMULATOR:
      return (
        <RansomwareSimulatorBlock
          content={content}
          isInteractive={isInteractive}
          handleNextContent={handleNextContent}
        />
      );
    case QuizTypes.PHISHING_DETECTION:
      return (
        <PhishingDetectionBlock
          content={content}
          isInteractive={isInteractive}
          handleNextContent={handleNextContent}
        />
      );
    default:
      return null;
  }
};

const ContentBlocks = ({
  sectionRefs,
  contents,
  onClickDelete,
  onClickEdit,
  handleVideoPolling,
}: IProps) => {
  useEffect(() => {
    sectionRefs.current[0]?.scrollIntoView({
      behavior: 'smooth',
    });
  }, [contents.length, sectionRefs]);

  const renderContentBlock = (content: IContent<TContent>) => {
    switch (content.common.contentType) {
      case ContentTypes.TEXT:
        return <TextBlock content={content as IContent<ITextContent>} />;
      case ContentTypes.LINK:
        return <LinkBlock content={content as IContent<ILinkContent>} />;
      case ContentTypes.PDF:
        return <PdfBlock content={content as IContent<IPdfContent>} />;
      case ContentTypes.SLIDER_SHOW:
        return <SliderShow content={content as IContent<ISliderShowContent>} />;
      case ContentTypes.QUESTION:
        return (
          <QuestionBlock content={content as IContent<IQuestionContent>} />
        );
      case ContentTypes.MARKDOWN:
        return <Markdown content={content as IContent<IMarkdownContent>} />;
      case ContentTypes.STORY_BLOCK:
        return <StoryBlock content={content as IContent<IStoryBlockContent>} />;
      case ContentTypes.TABBED:
        return <Tabbed content={content as IContent<ITabbedContent>} />;
      case ContentTypes.SLIDER_LEVEL:
        return (
          <SliderLevel content={content as IContent<ISliderLevelContent>} />
        );
      case ContentTypes.VIDEO:
      case ContentTypes.ANIMATION:
        return (
          <VideoBlock
            content={content as IContent<IVideoContent>}
            handleVideoPolling={handleVideoPolling}
          />
        );
      case ContentTypes.INTERACTIVE_VIDEO:
        return (
          <InteractiveVideo
            content={content as IContent<IInteractiveVideoContent>}
            handleVideoPolling={handleVideoPolling}
          />
        );
      case ContentTypes.INTERACTIVE_CONTENT:
        return (
          <InteractiveContent
            content={content as IContent<IInteractiveGeneralContent>}
            handleVideoPolling={handleVideoPolling}
          />
        );
      case ContentTypes.QUIZ:
        return renderQuizBlock(content as IContent<IQuizContent>);
      default:
        return null;
    }
  };

  return (
    <div className="content-mx-auto content-w-3/4 content-pb-10">
      {contents.map((content, index) => (
        <div
          key={content?.id}
          ref={el => {
            sectionRefs.current[index] = el!;
          }}
        >
          <div className="content-flex content-items-center content-justify-between content-pb-4 content-pt-24">
            <p className="content-ml-16 content-flex-1 content-text-center content-text-white">
              {contents.length - index}. {content.common.contentName}
            </p>
            <p className="content-flex content-gap-x-7">
              <Button
                className="!content-bg-transparent content-p-0"
                onClick={() => onClickDelete(content?.id as string)}
              >
                <DeleteIcon fill="#fff" />
              </Button>
              <Button
                className="!content-bg-transparent content-p-0"
                onClick={() => onClickEdit(content?.id as string)}
              >
                <EditIcon stroke="#fff" width={20} height={20} />
              </Button>
            </p>
          </div>

          <BlockWrapper content={content}>
            {!content?.specific?.updateContent && (
              <div className="content-flex content-size-full content-items-center content-justify-center">
                <Button
                  onClick={() => onClickEdit(content?.id as string)}
                  className="content-cursor-pointer"
                >
                  Add Content
                </Button>
              </div>
            )}

            {renderContentBlock(content)}
          </BlockWrapper>
        </div>
      ))}
    </div>
  );
};

export default ContentBlocks;
