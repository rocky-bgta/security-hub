import { useState } from 'react';
import { toast } from 'react-toastify';

import { Button } from 'common/Button';
import CustomCheckbox from 'common/CustomCheckbox';
import { Label } from 'common/Label';
import ContentCompleteModal from 'common/modal/miniModal/ContentComplete';
import {
  AllContentTypes,
  IContent,
  IQuestionContent,
  IQuestionOption,
} from 'models/Content';

interface IProps {
  content: IContent<IQuestionContent>;
  isInteractiveVideo?: boolean;
  isInteractiveContent?: boolean;
  handleNextContent?: () => void;
  showCompleteButton?: (type: AllContentTypes) => void;
  courseComplete?: () => void;
  isCompleted?: boolean;
}

const QuestionBased = ({
  content,
  isInteractiveVideo = false,
  isInteractiveContent = false,
  handleNextContent,
  showCompleteButton,
  courseComplete,
  isCompleted,
}: IProps) => {
  const [selectedAnswers, setSelectedAnswers] = useState<IQuestionOption[]>([]);
  const [showNextButton, setShowNextButton] = useState<boolean>(false);
  const [isSubmittedDisabled, setIsSubmittedDisabled] =
    useState<boolean>(false);
  const [showModal, setShowModal] = useState<boolean>(false);

  const handleHideModal = () => {
    setShowModal(false);
  };

  const handleSelectAnswer = (answer: IQuestionOption) => {
    if (selectedAnswers.includes(answer)) {
      setSelectedAnswers(prev => prev.filter(a => a !== answer));
    } else {
      setSelectedAnswers(prev => [...prev, answer]);
    }
  };

  const handleSubmit = () => {
    if (selectedAnswers.length === 0) {
      toast.warning('Please select at least one answer.');
      return;
    }

    const isCorrect = selectedAnswers.every(answer => answer.isCorrect);

    content?.specific?.options.find(option => option.isCorrect);

    if (isCorrect) {
      showCompleteButton?.(content.common.contentType);
      setShowNextButton(true);
      courseComplete?.();

      setIsSubmittedDisabled(true);
      setSelectedAnswers([]);
    } else {
      setShowModal(true);
      setSelectedAnswers([]);
    }
  };

  return (
    <>
      {isInteractiveContent && showNextButton && (
        <div className="content-absolute content-bottom-4 content-right-4 content-z-[60]">
          <Button onClick={handleNextContent} size="sm">
            Next
          </Button>
        </div>
      )}

      <div className="content-absolute content-z-50 content-w-full content-px-4">
        <div className="content-min-h-[600px] content-overflow-y-auto">
          {content?.specific?.question && (
            <p
              className="content-text-[32px] content-font-semibold"
              style={{
                color: content?.specific?.backgroundFormatting?.textColor,
              }}
            >
              {content?.specific?.question}
            </p>
          )}

          {content?.specific?.options && (
            <div className="content-mt-5 content-grid content-grid-cols-2 content-gap-4">
              {content?.specific?.options.map((option, idx) => (
                <Label
                  key={idx}
                  htmlFor={`option-${idx}`}
                  className="content-relative content-flex content-cursor-pointer content-items-center content-gap-2 content-rounded content-border content-border-cloudy-white content-px-4 content-py-3"
                  style={{
                    color: content?.specific?.backgroundFormatting?.textColor,
                  }}
                >
                  <CustomCheckbox
                    id={`option-${idx}`}
                    checked={selectedAnswers.includes(option)}
                    onChange={() => handleSelectAnswer(option)}
                  />
                  {option.option}
                </Label>
              ))}
            </div>
          )}

          <Button
            onClick={handleSubmit}
            className="content-mt-6 content-rounded-full content-px-8"
            disabled={isSubmittedDisabled || isCompleted}
          >
            Submit
          </Button>
        </div>
      </div>

      {!isInteractiveVideo && (
        <ContentCompleteModal
          isCorrect={false}
          isOpen={showModal}
          correctAnswer={content?.specific?.options.filter(
            option => option.isCorrect,
          )}
          onClose={handleHideModal}
          onClick={handleHideModal}
        />
      )}
    </>
  );
};

export default QuestionBased;
