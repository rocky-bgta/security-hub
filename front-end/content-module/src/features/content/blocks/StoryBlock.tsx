import { sanitizeHtml } from 'home-module/security';
import { useEffect, useState } from 'react';

import { LeftArrowIcon, ReloadIcon } from 'assets/icons';
import { Button } from 'common/Button';
import MiniPopup from 'components/MiniPopup';
import { CorrectSituation, IContent, IStoryBlockContent } from 'models/Content';
import { FILE_PATH_PREFIX } from 'utils/Constants';

interface IProps {
  content: IContent<IStoryBlockContent>;
  isInteractive?: boolean;
  handleNextContent?: () => void;
}

const StoryBlock = ({ content, isInteractive, handleNextContent }: IProps) => {
  const [currentStep, setCurrentStep] = useState<number>(0);
  const [currentStory, setCurrentStory] = useState<number>(-1);
  const [showStepPopup, setShowStepPopup] = useState<boolean>(false);
  const [storyEmojis, setStoryEmojis] = useState<{ [key: number]: string }>({});

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

  const handleReload = () => setCurrentStep(0);

  const handleStepPopupClose = () => setShowStepPopup(false);

  return (
    <>
      {totalSteps > 0 && (
        <>
          {isInteractive && isLastStep && isProgressBarFull && (
            <div className="content-absolute content-bottom-4 content-right-4 content-z-10">
              <Button onClick={handleNextContent} size="sm">
                Next
              </Button>
            </div>
          )}

          <div className="content-absolute content-right-10 content-top-4 content-z-10 content-flex content-items-center content-gap-4">
            {isLastStep && isProgressBarFull && (
              <Button
                onClick={handleReload}
                className="!content-bg-transparent !content-p-0 content-text-gray-700 content-transition-colors content-duration-200 content-ease-in-out hover:content-text-gray-500"
                aria-label="Reload content"
              >
                <ReloadIcon />
              </Button>
            )}
            {totalSteps > 0 && (
              <div className="content-rounded-full content-bg-gray-800 content-bg-opacity-20 content-px-4 content-py-2 content-text-black">
                {currentStep + 1}/{totalSteps}
              </div>
            )}
          </div>

          <div className="content-relative content-flex content-h-3/4 content-flex-col content-overflow-y-auto">
            <div className="content-flex content-items-center content-justify-center content-p-5 content-pr-8">
              {currentStepData?.featureImage && (
                <div className="content-mb-4 content-w-1/2">
                  <img
                    src={FILE_PATH_PREFIX + currentStepData.featureImage}
                    alt="step feature"
                    className="content-m-auto content-max-h-[300px] content-max-w-[300px] content-rounded content-object-cover"
                  />
                </div>
              )}

              <div className="content-flex content-w-1/2 content-flex-col content-items-center">
                {currentStepData?.title && (
                  <h2
                    className="content-mb-4"
                    style={{
                      color: currentStepData.titleColor,
                    }}
                  >
                    {currentStepData.title}
                  </h2>
                )}

                {currentStepData?.description && (
                  <div
                    className="content-text-lg"
                    style={{
                      color: currentStepData.descriptionColor,
                    }}
                    dangerouslySetInnerHTML={{
                      __html: sanitizeHtml(currentStepData.description),
                    }}
                  />
                )}
              </div>
            </div>

            <div className="content-absolute content-right-0 content-top-0 content-h-full content-w-6 content-overflow-hidden content-bg-gray-300">
              <div
                className="content-transition-height content-absolute content-bottom-0 content-left-0 content-w-full content-bg-green-500 content-duration-300 content-ease-in-out"
                style={{ height: `${progressPercentage}%` }}
              />
            </div>

            {currentStepData?.advisorInstructionText && (
              <div className="content-mt-4 content-flex content-items-start content-justify-center content-gap-x-4 content-pl-5 content-pr-8">
                <LeftArrowIcon className="content-h-8 content-w-20 content-text-blue-500" />
                <p
                  className="content-text-lg"
                  style={{
                    color: currentStepData.advisorInstructionTextColor,
                  }}
                >
                  {currentStepData?.advisorInstructionText}
                </p>
              </div>
            )}
          </div>

          <div className="content-flex-items-center content-relative content-h-1/4 content-bg-gray-900 content-bg-opacity-50 content-px-4 content-py-2.5">
            <div className="content-w-full content-overflow-x-auto [&::-webkit-scrollbar-thumb]:content-rounded-full [&::-webkit-scrollbar-thumb]:content-bg-white/30 hover:[&::-webkit-scrollbar-thumb]:content-bg-white/50 [&::-webkit-scrollbar-track]:content-bg-transparent [&::-webkit-scrollbar]:content-h-[6px]">
              {currentStory === -1 ? (
                <div className="content-absolute content--top-[50px] content-flex content-max-w-[95%] content-justify-between content-gap-4 content-px-4 content-py-2">
                  {currentStepStories?.map(
                    ({ additionalProperties: story }, index) => (
                      <Button
                        key={index}
                        className="!content-bg-transparent !content-p-0 content-transition-transform hover:content-scale-105"
                        onClick={() => handleClickStory(index)}
                      >
                        <div className="content-size-24 content-rounded-full content-border-2 content-border-white content-bg-gray-600">
                          {story?.featureImage && (
                            <div className="content-relative content-flex content-size-full content-items-center content-justify-center">
                              <img
                                src={FILE_PATH_PREFIX + story.featureImage}
                                alt={`story ${index + 1}`}
                                className="content-size-full content-rounded-full content-object-cover"
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
                            </div>
                          )}
                        </div>
                      </Button>
                    ),
                  )}
                </div>
              ) : (
                <div className="content-flex content-items-center content-justify-center content-gap-x-6">
                  {currentStoryData?.featureImage && (
                    <div className="content-flex content-w-1/4 content-justify-center">
                      <img
                        src={FILE_PATH_PREFIX + currentStoryData.featureImage}
                        alt="selected story"
                        className="content-size-24 content-rounded-full content-border-2 content-border-white content-object-cover"
                      />
                    </div>
                  )}

                  {currentStoryData?.description && (
                    <div
                      className="content-w-1/2 content-text-lg content-text-white"
                      dangerouslySetInnerHTML={{
                        __html: sanitizeHtml(currentStoryData?.description || ''),
                      }}
                      style={{
                        color: currentStoryData?.descriptionColor,
                      }}
                    />
                  )}

                  <div className="content-flex content-w-1/4 content-flex-col content-items-center content-gap-4">
                    <p>{currentStoryData?.actionButtonTitle}</p>

                    <div className="content-flex content-gap-x-4">
                      <Button
                        onClick={() =>
                          handleClickActionButton(CorrectSituation.AGREE)
                        }
                        variant="outline"
                        size="sm"
                      >
                        Agree
                      </Button>
                      <Button
                        onClick={() =>
                          handleClickActionButton(CorrectSituation.IGNORE)
                        }
                        variant="outline"
                        size="sm"
                      >
                        Ignore
                      </Button>
                    </div>
                  </div>
                </div>
              )}
            </div>
          </div>
        </>
      )}

      {showStepPopup && currentStepData && (
        <MiniPopup
          text={currentStepData.popupText!}
          textColor={currentStepData.popupTextColor!}
          buttonText={currentStepData.popupButtonText!}
          buttonColor={currentStepData.popupButtonColor!}
          buttonTextColor={currentStepData.popupButtonTextColor!}
          onClose={handleStepPopupClose}
        />
      )}
    </>
  );
};

export default StoryBlock;
