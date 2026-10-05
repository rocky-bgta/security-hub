import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from 'common/Dialog';
import Border from 'components/UserBorder';
import { useAPI } from 'hooks/UseAPI';
import { useEffect, useState } from 'react';
import { API_END_POINTS } from 'routes/APIEndpoints';

interface IOption {
  optionText: string;
  isCorrect: boolean;
}

interface IQuestion {
  id: string;
  topicId: string;
  questionType: 'MULTIPLE_CHOICE' | string;
  questionText: string;
  options: IOption[];
  status: 'ACTIVE' | string;
  createdBy: string;
  updatedBy: string;
  createdAt: string;
  updatedAt: string;
}

interface IProps {
  isOpen: boolean;
  onClose: () => void;
  questionId: string;
}

const QuestionView = ({ isOpen, onClose, questionId }: IProps) => {
  const [currentQuestion, setCurrentQuestion] = useState<IQuestion | null>(
    null,
  );
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  const apiClient = useAPI();

  const getQuestion = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await apiClient.get(
        API_END_POINTS.QUESTION_DETAILS + questionId,
      );

      setCurrentQuestion(response.data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'An error occurred');
    } finally {
      setLoading(false);
    }
  };

  // Initialize form with question data when modal opens
  useEffect(() => {
    if (questionId && isOpen) {
      getQuestion();
    }
  }, [questionId, isOpen]);

  // Reset state when modal closes
  useEffect(() => {
    if (!isOpen) {
      setCurrentQuestion(null);
      setError(null);
    }
  }, [isOpen]);

  const formatQuestionType = (type: string) => {
    return type
      .replace(/_/g, ' ')
      .toLowerCase()
      .replace(/\b\w/g, l => l.toUpperCase());
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="!content-w-2/3">
        <DialogHeader>
          <DialogTitle>View Question</DialogTitle>
          <DialogDescription>Question Details</DialogDescription>
        </DialogHeader>
        <Border className="content-px-6 content-py-4">
          {loading && (
            <div className="content-flex content-justify-center content-py-8">
              <div className="content-text-muted-foreground">
                Loading question...
              </div>
            </div>
          )}

          {error && (
            <div className="content-py-4">
              <div className="content-font-medium content-text-red-600">
                Error: {error}
              </div>
            </div>
          )}

          {currentQuestion && (
            <div className="content-space-y-4">
              <div className="content-text-muted-foreground">
                <strong className="content-text-foreground">Status:</strong>{' '}
                <span
                  className={`content-rounded content-px-2 content-py-1 content-text-xs content-font-medium ${
                    currentQuestion.status === 'ACTIVE'
                      ? 'content-bg-primary content-text-white'
                      : 'content-bg-vibrant-red content-text-white'
                  }`}
                >
                  {currentQuestion.status}
                </span>
              </div>

              <div className="content-text-muted-foreground">
                <strong className="content-text-foreground">
                  Question Type:
                </strong>{' '}
                {formatQuestionType(currentQuestion.questionType)}
              </div>

              <div className="content-mt-4">
                <strong className="content-text-foreground">Question:</strong>
                <Border className="content-mt-2 content-p-4">
                  <div className="content-mb-4 content-text-lg content-font-semibold content-text-foreground">
                    {currentQuestion.questionText}
                  </div>

                  <div className="content-space-y-2">
                    <strong className="content-text-sm content-text-foreground">
                      Options:
                    </strong>
                    <div className="content-space-y-1 content-pl-4">
                      {currentQuestion.options.map((option, index) => (
                        <div
                          key={index}
                          className={`content-flex content-items-center content-space-x-2 ${
                            option.isCorrect
                              ? 'content-font-medium content-text-primary'
                              : 'content-text-muted-foreground'
                          }`}
                        >
                          <span className="content-font-medium">
                            {String.fromCharCode(65 + index)}.
                          </span>
                          <span>{option.optionText}</span>
                          {option.isCorrect && (
                            <span className="content-rounded content-bg-primary content-px-2 content-py-1 content-text-xs content-text-white">
                              Correct
                            </span>
                          )}
                        </div>
                      ))}
                    </div>
                  </div>
                </Border>
              </div>

              <div className="content-mt-4 content-space-y-2">
                <div className="content-text-sm content-text-muted-foreground">
                  <strong className="content-text-foreground">
                    Created at:
                  </strong>{' '}
                  {formatDate(currentQuestion.createdAt)}
                </div>

                <div className="content-text-sm content-text-muted-foreground">
                  <strong className="content-text-foreground">
                    Updated at:
                  </strong>{' '}
                  {formatDate(currentQuestion.updatedAt)}
                </div>
              </div>
            </div>
          )}
        </Border>
      </DialogContent>
    </Dialog>
  );
};

export default QuestionView;
