import { useState } from 'react';
import { toast } from 'react-toastify';

import { Button } from 'common/Button';
import RansomwareSimulator from 'features/content/interactive-modules/ransomware-simulator/RansomwareSimulator';
import {
  AllContentTypes,
  IContent,
  IQuizContent,
  QuizTypes,
} from 'models/Content';
import { cn } from 'utils/Helper';

interface IProps {
  content: IContent<IQuizContent>;
  showCompleteButton?: (type: AllContentTypes) => void;
  courseComplete?: () => void;
  isInteractiveVideo?: boolean;
  isInteractiveContent?: boolean;
  handleNextContent?: () => void;
  isCompleted?: boolean;
}

const RansomwareSimulatorBlock = ({
  content,
  courseComplete,
  isInteractiveVideo = false,
  isInteractiveContent = false,
  handleNextContent,
  showCompleteButton,
}: IProps) => {
  const [guideCompleted, setGuideCompleted] = useState(false);
  const [showNextButton, setShowNextButton] = useState(false);

  const handleContinue = () => {
    if (!guideCompleted) {
      toast.warning('Please complete the ransomware walkthrough to continue');
      return;
    }

    if (isInteractiveVideo) {
      handleNextContent?.();
      return;
    }

    setShowNextButton(true);
    showCompleteButton?.(content?.specific?.quizType as QuizTypes);
    courseComplete?.();
  };

  return (
    <>
      {isInteractiveContent && showNextButton && (
        <div className="content-absolute content-bottom-1 content-right-4 content-z-10">
          <Button onClick={handleNextContent} size="sm">
            Next
          </Button>
        </div>
      )}

      <div
        className={cn(
          'content-relative content-min-h-[60px]',
          isInteractiveVideo
            ? 'content-flex content-h-full content-min-h-0 content-flex-col'
            : 'content-overflow-y-auto',
        )}
      >
        <RansomwareSimulator
          className={
            isInteractiveVideo ? 'content-min-h-0 content-flex-1' : undefined
          }
          description={
            content?.specific?.question ||
            'Learn how ransomware attacks unfold — and why backups beat ransom payments'
          }
          onGuideComplete={() => setGuideCompleted(true)}
          onGuideRestart={() => setGuideCompleted(false)}
        />

        {!isInteractiveContent && guideCompleted ? (
          <div
            className={cn(
              'content-mt-3 content-flex content-justify-center content-px-4 content-pb-3',
              isInteractiveVideo && 'content-shrink-0',
            )}
          >
            <Button onClick={handleContinue}>Continue</Button>
          </div>
        ) : null}
      </div>
    </>
  );
};

export default RansomwareSimulatorBlock;
