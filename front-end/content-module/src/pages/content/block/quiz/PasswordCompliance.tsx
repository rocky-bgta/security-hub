import { useState } from 'react';
import { toast } from 'react-toastify';

import { Button } from 'common/Button';
import PasswordCompliance from 'features/content/interactive-modules/password-compliance/PasswordCompliance';
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

const PasswordComplianceBlock = ({
  content,
  courseComplete,
  isInteractiveVideo = false,
  isInteractiveContent = false,
  handleNextContent,
  showCompleteButton,
}: IProps) => {
  const [hasEngaged, setHasEngaged] = useState(false);
  const [showNextButton, setShowNextButton] = useState(false);

  const handleContinue = () => {
    if (!hasEngaged) {
      toast.warning('Please enter a password to explore the compliance checker');
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
        <div
          className={
            isInteractiveVideo
              ? 'content-min-h-0 content-flex-1 content-overflow-y-auto'
              : undefined
          }
          onInput={e => {
            const target = e.target as HTMLInputElement;
            if (
              target?.id === 'password-compliance-input' &&
              target.value.trim().length > 0
            ) {
              setHasEngaged(true);
            }
          }}
        >
          <PasswordCompliance
            description={
              content?.specific?.question ||
              'Validate your password against multiple security and privacy frameworks'
            }
          />
        </div>

        {!isInteractiveContent && (
          <div
            className={cn(
              'content-mt-3 content-flex content-justify-center content-px-4 content-pb-3',
              isInteractiveVideo && 'content-shrink-0',
            )}
          >
            <Button
              onClick={handleContinue}
              disabled={!hasEngaged}
              aria-disabled={!hasEngaged}
            >
              Continue
            </Button>
          </div>
        )}
      </div>
    </>
  );
};

export default PasswordComplianceBlock;
