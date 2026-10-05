import { useState } from 'react';
import { toast } from 'react-toastify';

import { Button } from 'common/Button';
import PhishingDetection from 'features/content/interactive-modules/phishing-detection/PhishingDetection';
import {
  AllContentTypes,
  DEFAULT_PHISHING_TIME_LIMIT,
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

const PhishingDetectionBlock = ({
  content,
  courseComplete,
  isInteractiveVideo = false,
  isInteractiveContent = false,
  handleNextContent,
  showCompleteButton,
}: IProps) => {
  const [hasCompleted, setHasCompleted] = useState(false);
  const [showNextButton, setShowNextButton] = useState(false);

  const handleContinue = () => {
    if (!hasCompleted) {
      toast.warning('Please finish the phishing detection game to continue');
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
        <PhishingDetection
          className={
            isInteractiveVideo ? 'content-min-h-0 content-flex-1' : undefined
          }
          description={
            content?.specific?.question ||
            'Inspect each email, hover suspicious links, and decide whether it is phishing or safe.'
          }
          emails={content?.specific?.emails}
          timeLimitSeconds={
            content?.specific?.timeLimitSeconds ?? DEFAULT_PHISHING_TIME_LIMIT
          }
          onGameOver={() => setHasCompleted(true)}
        />

        {!isInteractiveContent && (
          <div
            className={cn(
              'content-mt-3 content-flex content-justify-center content-px-4 content-pb-3',
              isInteractiveVideo && 'content-shrink-0',
            )}
          >
            <Button
              onClick={handleContinue}
              disabled={!hasCompleted}
              aria-disabled={!hasCompleted}
            >
              Continue
            </Button>
          </div>
        )}
      </div>
    </>
  );
};

export default PhishingDetectionBlock;
