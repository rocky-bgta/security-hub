import { sanitizeHtml } from 'home-module/security';
import { Fragment, useEffect, useState } from 'react';

import { Button } from 'common/Button';
import UserMiniPopup from 'components/UserMiniPopup';
import {
  AllContentTypes,
  CorrectAcceptance,
  IContent,
  ISliderLevelButtonAddProp,
  ISliderLevelContent,
} from 'models/Content';
import { FILE_PATH_PREFIX } from 'utils/Constants';
import { cn } from 'utils/Helper';

interface IProps {
  content: IContent<ISliderLevelContent>;
  isInteractive?: boolean;
  handleNextContent?: () => void;
  showCompleteButton?: (type: AllContentTypes) => void;
  courseComplete?: () => void;
}

const SliderLevel = ({
  content,
  isInteractive,
  handleNextContent,
  showCompleteButton,
  courseComplete,
}: IProps) => {
  const [currentStepIndex, setCurrentStepIndex] = useState<number>(0);
  const [selectedButton, setSelectedButton] =
    useState<ISliderLevelButtonAddProp | null>(null);
  const [visibleSteps, setVisibleSteps] = useState<Set<number>>(new Set([0]));
  const [stepStatuses, setStepStatuses] = useState<
    Array<{
      status: CorrectAcceptance;
      acceptanceText: string;
      acceptanceTextColor: string;
    }>
  >([]);
  const [showResults, setShowResults] = useState<boolean>(false);
  const [showStepPopup, setShowStepPopup] = useState<boolean>(false);
  const [showButtonPopup, setShowButtonPopup] = useState<boolean>(false);
  const [showNextButton, setShowNextButton] = useState<boolean>(false);
  const currentStep =
    content?.specific?.steps[currentStepIndex]?.additionalProperties;
  const currentStepButtons =
    content?.specific?.steps[currentStepIndex]?.buttons;

  useEffect(() => {
    if (currentStep?.openPopup) {
      setShowStepPopup(true);
    }
  }, [currentStepIndex]);

  const handleClickStep = (index: number) => {
    if (index === currentStepIndex) return;
    setCurrentStepIndex(index);
    setShowResults(false);
  };

  const handleClickButton = (button: ISliderLevelButtonAddProp) => {
    setSelectedButton(button);
    if (button.openPopup) {
      setShowButtonPopup(true);
    } else {
      proceedToNextStep(button);
    }
  };

  const proceedToNextStep = (button = selectedButton) => {
    const updatedStatuses = [...stepStatuses];
    if (stepStatuses.length === currentStepIndex) {
      updatedStatuses.push({
        status: button?.correctAcceptanceText!,
        acceptanceText: button?.acceptanceText!,
        acceptanceTextColor: button?.acceptanceTextColor!,
      });
    } else {
      updatedStatuses[currentStepIndex] = {
        status: button?.correctAcceptanceText!,
        acceptanceText: button?.acceptanceText!,
        acceptanceTextColor: button?.acceptanceTextColor!,
      };
    }
    setStepStatuses(updatedStatuses);
    setSelectedButton(null);

    if (currentStepIndex < content?.specific?.steps?.length - 1) {
      setVisibleSteps(prev => new Set([...prev, currentStepIndex + 1]));
      setCurrentStepIndex(currentStepIndex + 1);
    } else {
      setShowResults(true);
      showCompleteButton?.(content.common.contentType);
      courseComplete?.();
      setShowNextButton(true);
    }
  };

  const handleCloseStepPopup = () => setShowStepPopup(false);

  const handleCloseButtonPopup = () => {
    setShowButtonPopup(false);
    proceedToNextStep();
  };

  return (
    <div>
      {isInteractive && showResults && showNextButton && (
        <div className="content-absolute content-bottom-4 content-right-4 content-z-[60]">
          <Button onClick={handleNextContent} size="sm">
            Next
          </Button>
        </div>
      )}

      {showStepPopup && currentStep && (
        <UserMiniPopup
          text={currentStep.popupText!}
          textColor={currentStep.popupTextColor!}
          buttonText={currentStep.popupButtonText!}
          buttonColor={currentStep.popupButtonColor!}
          buttonTextColor={currentStep.popupButtonTextColor!}
          onClose={handleCloseStepPopup}
        />
      )}

      {showButtonPopup && (
        <UserMiniPopup
          text={selectedButton?.popupText!}
          textColor={selectedButton?.popupTextColor!}
          buttonText={selectedButton?.popupButtonText!}
          buttonColor={selectedButton?.popupButtonColor!}
          buttonTextColor={selectedButton?.popupButtonTextColor!}
          onClose={handleCloseButtonPopup}
        />
      )}

      {content?.specific?.steps?.length > 0 && (
        <div>
          <div className="content-relative content-z-10 content-flex content-w-full">
            <div className="content-relative content-w-8/12 content-bg-white content-bg-opacity-25 content-py-4">
              <div className="content-relative content-min-h-[530px]">
                <div
                  className="content-absolute content-left-1/2 content-top-8 content-w-[2px] -content-translate-x-1/2 content-transform content-bg-white"
                  style={{
                    bottom: visibleSteps.size > 6 ? '-1.25rem' : '0',
                  }}
                />

                <div className="content-flex content-flex-col content-items-center">
                  <p className="content-mb-2 content-text-base content-font-medium content-text-white">
                    Top
                  </p>
                  <p className="content-h-[2px] content-w-8 content-bg-white" />
                </div>

                <div className="content-my-5 content-flex content-flex-col content-justify-between content-gap-y-5">
                  {content?.specific?.steps?.map(
                    ({ additionalProperties: step }, index) =>
                      visibleSteps.has(index) && (
                        <div
                          key={step.id}
                          className={cn(
                            'content-relative content-flex content-items-center content-justify-between',
                            index % 2 === 0
                              ? 'content-flex-row'
                              : 'content-flex-row-reverse',
                          )}
                        >
                          {!!step.description.length && (
                            <div
                              className={cn(
                                'content-w-6/12 content-px-2 content-text-sm content-text-white content-text-opacity-75',
                                index % 2 === 0
                                  ? 'content-border-l-4 content-border-white content-border-opacity-75 content-text-left'
                                  : 'content-border-r-4 content-border-white content-border-opacity-75 content-text-left',
                              )}
                              dangerouslySetInnerHTML={{
                                __html: sanitizeHtml(step.description),
                              }}
                            />
                          )}

                          <div className="content-relative">
                            <Button
                              className={cn(
                                'content-aspect-square content-h-10 content-w-10 content-rounded-full content-border !content-bg-gray-800 !content-p-0 content-transition-all content-duration-200',
                                currentStepIndex === index
                                  ? 'content-scale-110 content-border-2 content-border-primary'
                                  : 'content-border content-border-gray-600 hover:content-border-primary',
                              )}
                              onClick={() => handleClickStep(index)}
                            >
                              {step.featureImage && (
                                <img
                                  src={FILE_PATH_PREFIX + step.featureImage}
                                  alt={step.title}
                                  className="content-size-full content-rounded-full content-object-cover"
                                />
                              )}
                            </Button>
                            {showResults ? (
                              stepStatuses[index]?.status ===
                              CorrectAcceptance.WARNING ? (
                                <span className="content-absolute content--left-1 content--top-1 content-text-yellow-500">
                                  ⚠️
                                </span>
                              ) : stepStatuses[index]?.status ===
                                CorrectAcceptance.UNACCEPTABLE ? (
                                <span className="content-absolute content--left-1 content--top-1 content-text-red-500">
                                  🚫
                                </span>
                              ) : null
                            ) : null}
                          </div>

                          <span className="content-w-6/12" />
                        </div>
                      ),
                  )}
                </div>
              </div>

              <div className="content-flex content-flex-col content-items-center">
                <p className="content-mb-2 content-h-[2px] content-w-8 content-bg-white" />
                <p className="content-text-base content-font-medium content-text-white">
                  Bottom
                </p>
              </div>

              {showResults && (
                <p className="content-absolute content-bottom-4 content-right-5 content-text-sm content-text-white">
                  <p className="content-text-right">
                    <span className="content-mr-2">⚠️</span>
                    <span>Warning</span>
                  </p>
                  <p className="content-text-right">
                    <span className="content-mr-2">🚫</span>
                    <span>Unacceptable</span>
                  </p>
                </p>
              )}
            </div>

            <div className="content-w-4/12">
              <div className="content-flex content-h-full content-flex-col content-items-center content-justify-center content-p-4">
                {!showResults ? (
                  <Fragment>
                    <h2
                      className="content-mb-6 content-text-center content-text-xl content-font-medium content-text-white content-opacity-75"
                      style={{
                        color: currentStep?.titleColor,
                      }}
                    >
                      {currentStep?.title}
                    </h2>

                    <p
                      className="content-mb-4 content-text-sm content-text-white"
                      style={{
                        color: currentStep?.navigationButtonTextColor,
                      }}
                    >
                      {currentStep?.navigationButtonText}
                    </p>

                    <div className="content-flex content-flex-col content-items-center content-justify-center content-gap-4">
                      {currentStepButtons?.map(
                        ({ additionalProperties: button }) =>
                          !!button.navigationButtonText.length && (
                            <Button
                              key={button.id}
                              className="content-w-full content-px-4"
                              style={{
                                backgroundColor: button.navigationButtonColor,
                                color: button.navigationButtonTextColor,
                              }}
                              onClick={() => handleClickButton(button)}
                            >
                              {button.navigationButtonText}
                            </Button>
                          ),
                      )}
                    </div>
                  </Fragment>
                ) : (
                  <div>
                    <h3 className="content-mb-4 content-text-lg content-font-medium content-text-white">
                      Let's look at the timeline of events.
                    </h3>
                    {stepStatuses.map((data, index) => (
                      <p
                        key={index}
                        className="content-mb-3"
                        style={{ color: data.acceptanceTextColor }}
                      >
                        <strong className="content-pr-2 content-text-sky-400">
                          {index + 1}
                        </strong>
                        <span>: {data.acceptanceText}</span>
                      </p>
                    ))}
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default SliderLevel;
