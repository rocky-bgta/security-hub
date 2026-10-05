import { Fragment, useCallback, useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { toast } from 'react-toastify';

import { SliderLeftIcon } from 'assets/icons';
import { Button } from 'common/Button';
import CustomCheckbox from 'common/CustomCheckbox';
import ExamMiniModal from 'common/modal/miniModal/ExamMiniModal';
import Border from 'components/UserBorder';
import { useAPI } from 'hooks/UseAPI';
import { useStore } from 'hooks/UseStore';
import { ExamTypes } from 'models/Exam';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { routes } from 'routes/Routes';
import { isSuccessResponse } from 'utils/Helper';

interface IQuestionResponse {
  examId: string;
  questionId: string;
  currentQuestionNumber: number;
  totalQuestions: number;
  questionText: string;
  options: string[];
  questionType: 'YES_NO' | 'MULTIPLE_CHOICE' | 'SINGLE_CHOICE';
  hasNext: boolean;
  hasPrevious: boolean;
  nextQuestionNumber: number | null;
  previousQuestionNumber: number | null;
}

interface IProps {
  examId: string;
}

const ExamPage = ({ examId }: IProps) => {
  const navigate = useNavigate();
  const { slug } = useParams();
  const [loading, setLoading] = useState<boolean>(false);
  const [finalLoading, setFinalLoading] = useState<boolean>(false);
  const [selectedOption, setSelectedOption] = useState<string[]>([]);
  const [showModal, setShowModal] = useState<boolean>(false);
  const [isSelectedAnsCorrect, setIsSelectedAnsCorrect] =
    useState<boolean>(false);
  const [currentQuestion, setCurrentQuestion] =
    useState<IQuestionResponse | null>(null);
  const [questionLoading, setQuestionLoading] = useState<boolean>(false);

  const apiClient = useAPI();
  const { userInfo } = useStore();

  const getQuestion = useCallback(
    async (questionNumber: number) => {
      setQuestionLoading(true);
      try {
        const response = await apiClient.get(
          API_END_POINTS.USER_EXAM_QUESTIONS.replace(':examId', examId).replace(
            ':questionNumber',
            questionNumber.toString(),
          ),
        );
        if (!isSuccessResponse(response.statusCode)) {
          throw new Error(response.message || 'Failed to fetch question');
        }

        setCurrentQuestion(response.data as IQuestionResponse);
        setSelectedOption([]);
      } catch (error) {
        console.error('Error fetching question:', error);
        toast.error((error as Error).message);
      } finally {
        setQuestionLoading(false);
      }
    },
    [apiClient, examId],
  );

  useEffect(() => {
    console.debug('Exam started with examId:', examId);
    getQuestion(1);
  }, [examId, getQuestion]);

  const handleNextQuestion = () => {
    if (currentQuestion?.hasNext && currentQuestion.nextQuestionNumber) {
      getQuestion(currentQuestion.nextQuestionNumber);
    } else {
      handleFinalSubmit();
    }
  };

  const totalQuestionProgress = () => {
    if (!currentQuestion || currentQuestion.totalQuestions <= 0) return 0;
    return Math.round(
      ((currentQuestion.currentQuestionNumber - 1) /
        currentQuestion.totalQuestions) *
        100,
    );
  };

  const handleQuestionSubmit = async () => {
    if (!currentQuestion) return;

    setLoading(true);
    const payload = {
      examId: examId,
      userId: userInfo.userId,
      questionId: currentQuestion.questionId,
      submittedAnswers: selectedOption,
    };
    try {
      const response = await apiClient.post(
        API_END_POINTS.USER_QUESTION_SUBMIT,
        { data: payload },
      );

      if (!isSuccessResponse(response.statusCode)) {
        throw new Error(response.message || 'Failed to submit answer');
      }

      setIsSelectedAnsCorrect(response.data.correct);
      setShowModal(true);
    } catch (error) {
      console.error('Error validating answer:', error);
      toast.error((error as Error).message);
    } finally {
      setLoading(false);
    }
  };

  const handleFinalSubmit = async () => {
    setFinalLoading(true);

    try {
      const response = await apiClient.post(
        API_END_POINTS.USER_FINIAL_EXAM_SUBMIT,
        {
          data: {
            userId: userInfo.userId,
            examId: examId,
          },
        },
      );

      if (!isSuccessResponse(response.statusCode)) {
        throw new Error(response.message || 'Failed to submit exam');
      }
      navigate(routes.examResult.path.replace(':slug', slug as string), {
        state: { data: response.data },
      });
    } catch (error) {
      console.error('Error submitting exam:', error);
      toast.error((error as Error).message);
    } finally {
      setFinalLoading(false);
    }
  };

  const handleOptionChange = (option: string) => {
    if (!currentQuestion) return;

    if (currentQuestion.questionType === ExamTypes.MULTIPLE_CHOICE) {
      setSelectedOption(prev =>
        prev.includes(option)
          ? prev.filter(o => o !== option)
          : [...prev, option],
      );
    } else {
      setSelectedOption([option]);
    }
  };

  const isMultipleChoice =
    currentQuestion?.questionType === ExamTypes.MULTIPLE_CHOICE;

  if (questionLoading && !currentQuestion) {
    return (
      <div className="content-mx-auto content-w-3/5 content-py-20 content-text-center content-text-white">
        Loading question...
      </div>
    );
  }

  if (!currentQuestion) {
    return null;
  }

  return (
    <Fragment>
      <div className="content-mx-auto content-w-3/5 content-text-white">
        <div className="content-mb-6 content-flex content-w-full content-items-center content-justify-between content-rounded content-bg-white content-bg-opacity-25 content-px-6 content-py-4">
          <Button
            onClick={() =>
              navigate(routes.courseList.path.replace(':slug', slug as string))
            }
            className="content-flex content-items-center content-gap-2 content-px-0 content-py-2 content-text-white"
          >
            <SliderLeftIcon stroke="#ffffff" /> Back to Course
          </Button>
          <span>
            Question {currentQuestion.currentQuestionNumber} of{' '}
            {currentQuestion.totalQuestions}
          </span>
        </div>

        <div className="content-mb-8 content-mt-14 content-w-full">
          <div className="content-mt-3.5 content-h-2.5 content-w-full content-rounded-full content-bg-white content-bg-opacity-25">
            <div
              className="content-h-2.5 content-rounded-full content-bg-primary"
              style={{
                width: `${totalQuestionProgress()}%`,
              }}
            ></div>
          </div>
          <p className="content-mt-4">
            Assessment Progress: {totalQuestionProgress()}% Complete
          </p>
        </div>

        <Border className="content-p-6">
          <p className="content-mb-2 content-text-xl content-font-semibold">
            Question {currentQuestion.currentQuestionNumber}
          </p>
          <p className="content-mb-6 content-text-white content-text-opacity-75">
            {currentQuestion.questionText}
          </p>

          <div className="content-mt-4 content-space-y-4 content-border-t content-border-card-border content-pt-7">
            {currentQuestion.options.map((option: string, index: number) => (
              <label
                key={index}
                htmlFor={`option-${index}`}
                className="content-relative content-flex content-cursor-pointer content-items-center content-rounded content-border content-border-card-border content-px-4 content-py-3 hover:content-border-primary"
              >
                <CustomCheckbox
                  variant={isMultipleChoice ? 'default' : 'exam'}
                  id={`option-${index}`}
                  checked={selectedOption.includes(option)}
                  onChange={() => handleOptionChange(option)}
                />
                <span className="content-ml-2 content-text-base">{option}</span>
              </label>
            ))}
          </div>
        </Border>

        <Button
          className="content-mx-auto content-mt-6 content-rounded-full content-px-8"
          onClick={handleQuestionSubmit}
          disabled={selectedOption.length === 0 || loading}
        >
          {loading ? 'Submitting Answer...' : 'Submit Answer'}
        </Button>
      </div>

      {showModal && (
        <ExamMiniModal
          isCorrect={isSelectedAnsCorrect}
          isLastStep={!currentQuestion.hasNext}
          isOpen={showModal}
          loading={finalLoading}
          onClose={() => setShowModal(false)}
          handleNextQuestion={handleNextQuestion}
        />
      )}
    </Fragment>
  );
};

export default ExamPage;
