import { sanitizeHtml } from 'home-module/security';
import { ReloadIcon } from 'assets/icons';
import { Button } from 'common/Button';
import UserMiniPopup from 'components/UserMiniPopup';
import {
  AllContentTypes,
  CorrectSituation,
  IContent,
  IStoryBlockContent,
} from 'models/Content';
import { Fragment, useEffect, useState } from 'react';
import { FILE_PATH_PREFIX } from 'utils/Constants';

interface IProps {
  content: IContent<IStoryBlockContent>;
  isInteractive?: boolean;
  handleNextContent?: () => void;
  showCompleteButton?: (type: AllContentTypes) => void;
  courseComplete?: () => void;
}

const StoryBlock = ({
  content,
  isInteractive,
  handleNextContent,
  showCompleteButton,
  courseComplete,
}: IProps) => {
  const [currentStep, setCurrentStep] = useState<number>(0);
  const [currentStory, setCurrentStory] = useState<number>(-1);
  const [showStepPopup, setShowStepPopup] = useState<boolean>(false);
  const [storyEmojis, setStoryEmojis] = useState<{ [key: number]: string }>({});
  const [showNextButton, setShowNextButton] = useState<boolean>(false);

  useEffect(() => {
    setStoryEmojis({});
    setCurrentStory(-1);
    setShowStepPopup(!!currentStepData?.openPopup);
  }, [currentStep, content]);

  const currentStepData =
    content?.specific?.steps?.[currentStep]?.additionalProperties;
  const currentStepStories = content?.specific?.steps?.[currentStep]?.stories;
  const currentStoryData =
    currentStory !== -1
      ? currentStepStories?.[currentStory]?.additionalProperties
      : null;
  const totalStoriesInCurrentStep = currentStepStories?.length ?? 0;
  const correctCount = Object.values(storyEmojis).filter(
    item => item === 'happy',
  ).length;
  const progressPercentage =
    totalStoriesInCurrentStep > 0
      ? (correctCount / totalStoriesInCurrentStep) * 100
      : 0;

  const totalSteps = content?.specific?.steps?.length ?? 0;
  const isLastStep = currentStep === totalSteps - 1;
  const isProgressBarFull = correctCount === totalStoriesInCurrentStep;

  useEffect(() => {
    if (isProgressBarFull && isLastStep) {
      showCompleteButton?.(content.common.contentType);
      courseComplete?.();
      setShowNextButton(true);
    }
  }, [isProgressBarFull, isLastStep]);

  const goNextStep = () => {
    if (currentStep < totalSteps - 1) {
      setCurrentStep(currentStep + 1);
    }
  };

  const handleClickStory = (index: number) => setCurrentStory(index);

  const handleClickActionButton = (action: CorrectSituation) => {
    const updateStoryEmojis = {
      ...storyEmojis,
      [currentStory]:
        currentStoryData?.correctSituation === action ? 'happy' : 'sad',
    };
    setStoryEmojis(updateStoryEmojis);
    const updatedCount = Object.values(updateStoryEmojis).filter(
      item => item === 'happy',
    ).length;
    setCurrentStory(-1);

    if (updatedCount === totalStoriesInCurrentStep) {
      setTimeout(() => {
        goNextStep();
      }, 1000);
    }
  };

  const handleStepPopupClose = () => setShowStepPopup(false);

  return (
    <Fragment>
      {totalSteps > 0 && (
        <Fragment>
          {isInteractive &&
            isLastStep &&
            isProgressBarFull &&
            showNextButton && (
              <div className="content-absolute content-bottom-1 content-right-4 content-z-10">
                <Button onClick={handleNextContent} size="sm">
                  Next
                </Button>
              </div>
            )}

          {isLastStep && isProgressBarFull && (
            <Button
              // onClick={handleReload}
              className="!content-bg-transparent !content-p-0 content-text-gray-700 content-transition-colors content-duration-200 content-ease-in-out hover:content-text-gray-500"
              aria-label="Reload content"
            >
              <ReloadIcon />
            </Button>
          )}

          <div className="content-p-6">
            <div className="content-flex content-items-center content-justify-between">
              {currentStepData?.title && (
                <h1
                  className="content-mb-6 content-text-[32px] content-font-semibold content-text-white"
                  style={{
                    color: currentStepData.titleColor,
                  }}
                >
                  {currentStepData.title}
                </h1>
              )}
              {totalSteps > 0 && (
                <div className="content-rounded-full content-bg-white content-bg-opacity-25 content-px-4 content-py-2 content-text-sm content-text-white">
                  {currentStep + 1}/{totalSteps}
                </div>
              )}
            </div>

            <div className="content-flex content-gap-3 sm:content-justify-between">
              <div className="content-min-w-0 content-flex-1">
                {currentStepData?.featureImage && (
                  <img
                    src={FILE_PATH_PREFIX + currentStepData.featureImage}
                    alt="preview"
                    className="content-mb-6 content-w-full"
                  />
                )}
                {currentStepData?.description && (
                  <div
                    className="content-text-white"
                    style={{
                      color: currentStepData.descriptionColor,
                    }}
                    dangerouslySetInnerHTML={{
                      __html: sanitizeHtml(currentStepData.description),
                    }}
                  />
                )}
              </div>
              <div className="content-flex content-w-8 content-shrink-0 content-flex-col content-items-center content-gap-4 sm:content-items-end">
                <p className="content-text-sm content-font-semibold content-text-primary">
                  Goal
                </p>
                <div className="content-flex content-h-96 content-w-[32px] content-items-end content-justify-center content-rounded-lg content-bg-white content-bg-opacity-25 content-p-1">
                  <div
                    className="content-w-full content-rounded-b content-bg-primary"
                    style={{ height: `${progressPercentage}%` }}
                  ></div>
                </div>
              </div>
            </div>
          </div>
          {currentStory === -1 ? (
            <div className="content-relative content-mt-10 content-h-24 content-bg-[#D9D9D959]">
              <div className="content-absolute -content-top-1/2 content-flex content-w-full content-items-center content-justify-around content-gap-2">
                {currentStepStories?.map(
                  ({ additionalProperties: story }, index) => (
                    <Button
                      className="content-relative content-p-0"
                      key={index}
                      onClick={() => handleClickStory(index)}
                    >
                      {story?.featureImage && (
                        <Fragment>
                          <img
                            src={FILE_PATH_PREFIX + story.featureImage}
                            alt={`story ${index + 1}`}
                            className="content-size-28 content-rounded-full content-object-cover content-transition-transform hover:content-scale-105"
                          />
                          {index in storyEmojis && (
                            <span className="content-absolute content--right-5 content--top-2 content-text-4xl">
                              {storyEmojis[index] === 'happy' ? (
                                <>😊</>
                              ) : storyEmojis[index] === 'sad' ? (
                                <>😢</>
                              ) : null}
                            </span>
                          )}
                        </Fragment>
                      )}
                    </Button>
                  ),
                )}
              </div>
            </div>
          ) : (
            <div className="content-mt-20 content-flex content-justify-between content-gap-5 content-bg-[#D9D9D959] content-px-6 content-py-4">
              <div className="content-flex content-w-2/3 content-items-center content-gap-10">
                {currentStoryData?.featureImage && (
                  <img
                    src={FILE_PATH_PREFIX + currentStoryData.featureImage}
                    alt="preview"
                    className="content-size-32 content-rounded-full content-bg-white content-object-cover content-p-1"
                  />
                )}
                {currentStoryData?.description && (
                  <div
                    className="content-text-sm content-text-white"
                    dangerouslySetInnerHTML={{
                      __html: sanitizeHtml(currentStoryData?.description || ''),
                    }}
                    style={{
                      color: currentStoryData?.descriptionColor,
                    }}
                  />
                )}
              </div>
              <div className="content-w-1/3">
                <p className="content-text-white">
                  {currentStoryData?.actionButtonTitle}
                </p>
                <div className="content-mt-2 content-flex content-gap-4">
                  <Button
                    onClick={() =>
                      handleClickActionButton(CorrectSituation.AGREE)
                    }
                  >
                    Accept
                  </Button>
                  <Button
                    variant="outline"
                    className="content-border-ash-gray content-text-cloudy-white hover:content-bg-transparent"
                    onClick={() =>
                      handleClickActionButton(CorrectSituation.IGNORE)
                    }
                  >
                    Ignore
                  </Button>
                </div>
              </div>
            </div>
          )}

          {showStepPopup && currentStepData && (
            <UserMiniPopup
              text={currentStepData.popupText!}
              textColor={currentStepData.popupTextColor!}
              buttonText={currentStepData.popupButtonText!}
              buttonColor={currentStepData.popupButtonColor!}
              buttonTextColor={currentStepData.popupButtonTextColor!}
              onClose={handleStepPopupClose}
            />
          )}
        </Fragment>
      )}
    </Fragment>
  );
};

export default StoryBlock;
