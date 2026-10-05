import { useEffect, useState } from 'react';
import { toast } from 'react-toastify';

import { Button } from 'common/Button';
import {
  AllContentTypes,
  IContent,
  IQuizContent,
  IQuizOption,
  LikerSelectOptionIconMapper,
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

const LikerSelectBlock = ({
  content,
  courseComplete,
  isInteractiveVideo = false,
  isInteractiveContent = false,
  handleNextContent,
  showCompleteButton,
  isCompleted,
}: IProps) => {
  const [options, setOptions] = useState<Array<IQuizOption>>([]);
  const [selectedAnswers, setSelectedAnswers] = useState<string[]>([]);
  const [showNextButton, setShowNextButton] = useState(false);

  useEffect(() => {
    setTimeout(() => {
      setOptions(content?.specific?.options || []);
    }, 0);
  }, [content]);

  const handleSelectAnswer = (option: any) => {
    if (selectedAnswers.includes(option.id)) {
      setSelectedAnswers([]);
    } else {
      setSelectedAnswers([option.id]);
    }
  };

  const handleSubmit = () => {
    if (!selectedAnswers.length) {
      toast.warning('Please select an option');
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

      <div className="content-min-h-[600px] content-p-16">
        <h1
          className="content-text-center content-text-[32px] content-font-bold"
          style={{ color: content.specific.backgroundFormatting.textColor }}
        >
          {content?.specific?.question}
        </h1>
        <div className="content-mt-10 content-grid content-w-full content-grid-cols-5 content-justify-center content-gap-8">
          {options.map((option, index) => {
            const IconComponent = option.optionImageLink
              ? LikerSelectOptionIconMapper[
              option.optionImageLink as keyof typeof LikerSelectOptionIconMapper
              ]
              : null;

            return (
              <div
                key={index}
                className={cn(
                  'content-flex content-cursor-pointer content-flex-col content-items-center content-gap-4',
                  selectedAnswers.includes(option.id)
                    ? '!content-text-primary'
                    : '',
                )}
                style={{
                  color: content.specific.backgroundFormatting.textColor,
                }}
                onClick={() => handleSelectAnswer(option)}
              >
                {IconComponent && <IconComponent />}
                <div className="content-text-center content-text-sm content-leading-tight">
                  {option.optionText}
                </div>
              </div>
            );
          })}
        </div>
        <Button
          className="content-mx-auto content-mt-16 !content-text-base !content-py-7"
          onClick={handleSubmit}
          disabled={isCompleted}
        >
          Submit
        </Button>
      </div>
    </>
  );
};

export default LikerSelectBlock;
