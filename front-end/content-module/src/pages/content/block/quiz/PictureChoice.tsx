import { useState } from 'react';
import { toast } from 'react-toastify';

import { Button } from 'common/Button';
import ContentCompleteModal from 'common/modal/miniModal/ContentComplete';
import {
  AllContentTypes,
  IContent,
  IQuizContent,
  QuizTypes,
} from 'models/Content';
import { FILE_PATH_PREFIX } from 'utils/Constants';
import { cn } from 'utils/Helper';
import { FlaskConical } from 'lucide-react';

interface IProps {
  content: IContent<IQuizContent>;
  showCompleteButton?: (type: AllContentTypes) => void;
  isInteractiveVideo?: boolean;
  isInteractiveContent?: boolean;
  handleNextContent?: () => void;
  courseComplete?: () => void;
  isCompleted?: boolean;
}

const PictureChoiceBlock = ({
  content,
  showCompleteButton,
  isInteractiveVideo = false,
  isInteractiveContent = false,
  handleNextContent,
  courseComplete,
  isCompleted,
}: IProps) => {
  const [selectedAnswers, setSelectedAnswers] = useState<string[]>([]);
  const [showNextButton, setShowNextButton] = useState(false);
  const [showModal, setShowModal] = useState<boolean>(false);

  const isCorrectAnswer = selectedAnswers.every(
    id =>
      content?.specific?.options?.find(option => option.id === id)?.isCorrect,
  );

  const handleSelectAnswer = (option: any) => {
    if (selectedAnswers.includes(option.id)) {
      setSelectedAnswers(prev => prev.filter(a => a !== option.id));
    } else {
      setSelectedAnswers(prev => [...prev, option.id]);
    }
  };

  const handleSubmit = () => {
    if (selectedAnswers.length === 0) {
      toast.warning('Please select at least one answer');
      return;
    }

    setShowModal(true);
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
        <div className="content-mx-auto content-w-3/4">
          <h1
            className="content-text-[32px] content-font-bold"
            style={{ color: content.specific.backgroundFormatting.textColor }}
          >
            {content?.specific?.question}
          </h1>
          <div className="content-mt-5 content-grid content-grid-cols-2 content-gap-5">
            {content?.specific?.options?.map(option => (
              <div
                onClick={() => handleSelectAnswer(option)}
                key={option.id}
                className={cn(
                  'content-w-full content-cursor-pointer content-rounded content-border content-p-3',
                  selectedAnswers.includes(option.id) &&
                    'content-border-primary',
                )}
              >
                <img
                  src={FILE_PATH_PREFIX + option.optionImageLink}
                  alt={option.optionText}
                  className="content-size-full"
                />
              </div>
            ))}
          </div>
        </div>
        <div className="content-flex content-justify-end">
          <Button
            className="content-float-right content-mt-5 !content-text-base !content-py-7"
            size="sm"
            onClick={() => handleSubmit()}
            disabled={isCompleted}
          >
            <FlaskConical /> Check Answer
          </Button>
        </div>
      </div>

      <ContentCompleteModal
        isCorrect={isCorrectAnswer}
        isOpen={showModal}
        isInteractive={isInteractiveVideo || isInteractiveContent}
        onClose={() => setShowModal(false)}
        onClick={() => {
          setShowModal(false);

          if (isInteractiveVideo) {
            handleNextContent?.();
            return;
          }

          if (isCorrectAnswer) {
            showCompleteButton?.(content.specific?.quizType as QuizTypes);
            courseComplete?.();
            setShowNextButton(true);
          }
        }}
      />
    </>
  );
};

export default PictureChoiceBlock;
