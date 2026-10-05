import { useState } from 'react';
import { toast } from 'react-toastify';

import { sanitizeHtml } from 'home-module/security';
import { Button } from 'common/Button';
import ContentCompleteModal from 'common/modal/miniModal/ContentComplete';
import {
  AllContentTypes,
  IContent,
  IQuestionOption,
  IQuizContent,
  QuizTypes,
} from 'models/Content';
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

const ScenarioBlock = ({
  content,
  showCompleteButton,
  isInteractiveVideo = false,
  isInteractiveContent = false,
  handleNextContent,
  courseComplete,
  isCompleted,
}: IProps) => {
  const [selectedAnswers, setSelectedAnswers] = useState<
    Array<IQuestionOption>
  >([]);
  const [showNextButton, setShowNextButton] = useState(false);
  const [showModal, setShowModal] = useState<boolean>(false);

  const correctAnswers = (content?.specific?.questions || [])
    .map(question => question.options.filter(option => option.isCorrect))
    .flat();

  const isCorrect = selectedAnswers.every(answer =>
    correctAnswers.some(correct => correct.id === answer.id),
  );

  const handleSelectAnswer = (answer: IQuestionOption) => {
    if (selectedAnswers.includes(answer)) {
      setSelectedAnswers(prev => prev.filter(a => a !== answer));
    } else {
      setSelectedAnswers(prev => [...prev, answer]);
    }
  };

  // Function to handle the submission of answers
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
        <div
          className="content-text-white"
          dangerouslySetInnerHTML={{
            __html: sanitizeHtml(content?.specific?.scenarioText || ''),
          }}
        />

        {content?.specific?.questions?.map((option, index) => (
          <div key={index} className="content-mt-5">
            <p
              className="content-text-xl"
              style={{ color: content.specific.backgroundFormatting.textColor }}
            >
              <span className="content-mr-2">{index + 1}.</span>{' '}
              {option.question}
            </p>
            <div className="content-mt-5 content-grid content-grid-cols-2 content-gap-4">
              {option.options.map((option, index) => (
                <div key={index}>
                  <p
                    onClick={() => handleSelectAnswer(option)}
                    className={cn(
                      'content-relative content-flex content-cursor-pointer content-items-center content-gap-2 content-rounded content-border content-border-cloudy-white content-px-4 content-py-3 content-text-center',
                      selectedAnswers.includes(option)
                        ? 'content-border-success content-text-success'
                        : 'content-border-gray-200',
                    )}
                    style={{
                      color: content.specific.backgroundFormatting.textColor,
                    }}
                  >
                    {option.option}
                  </p>
                </div>
              ))}
            </div>
          </div>
        ))}
        <div className="content-flex content-justify-end">
          <Button
            className="content-float-right content-mt-5 !content-text-base !content-py-7"
            size="sm"
            onClick={handleSubmit}
            disabled={isCompleted}
          >
            <FlaskConical /> Check Answer
          </Button>
        </div>
      </div>

      <ContentCompleteModal
        isCorrect={isCorrect}
        isOpen={showModal}
        correctAnswer={correctAnswers}
        isInteractive={isInteractiveVideo || isInteractiveContent}
        onClose={() => {
          setShowModal(false);
          setSelectedAnswers([]);
        }}
        onClick={() => {
          setShowModal(false);

          if (isInteractiveVideo) {
            handleNextContent?.();
            return;
          }

          if (isCorrect && selectedAnswers.length === correctAnswers.length) {
            showCompleteButton?.(content?.specific?.quizType as QuizTypes);
            courseComplete?.();
            setShowNextButton(true);
          }
        }}
      />
    </>
  );
};

export default ScenarioBlock;
