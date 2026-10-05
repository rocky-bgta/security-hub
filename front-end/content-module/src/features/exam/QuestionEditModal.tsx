import { Button } from 'common/Button';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from 'common/Card';
import CustomCheckbox from 'common/CustomCheckbox';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from 'common/Dialog';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import { Textarea } from 'common/Textarea';
import { useAPI } from 'hooks/UseAPI';
import { Save } from 'lucide-react';
import { ExamTypes, IQuestion, IQuestionOption } from 'models/Exam';
import { Fragment, useEffect, useState } from 'react';
import { toast } from 'react-toastify';
import { API_END_POINTS } from 'routes/APIEndpoints';

interface IProps {
  topicId: string;
  questionId: string;
  isOpen: boolean;
  onClose: () => void;
  onSave: () => void; // Callback after successful save
}

// Question types configuration
const questionTypes = [
  { value: ExamTypes.MULTIPLE_CHOICE, label: 'Multiple Choice' },
  { value: ExamTypes.SINGLE_CHOICE, label: 'Single Choice' },
  { value: ExamTypes.TRUE_FALSE, label: 'True/False' },
  { value: ExamTypes.YES_NO, label: 'Yes/No' },
];

const QuestionEditModal = ({
  topicId,
  questionId,
  isOpen,
  onClose,
  onSave,
}: IProps) => {
  // Current question state
  const [currentQuestion, setCurrentQuestion] = useState<IQuestion>({
    id: '',
    topicId: '',
    questionText: '',
    questionType: '' as ExamTypes,
    status: 'ACTIVE',
    options: [{ optionText: '', isCorrect: false }],
  });

  const [isSubmitting, setIsSubmitting] = useState(false);
  const apiClient = useAPI();

  const getQuestion = async () => {
    try {
      const response = await apiClient.get(
        API_END_POINTS.QUESTION_DETAILS + questionId,
      );

      setCurrentQuestion(response.data);
    } catch (error) {
      console.error('Error fetching question:', error);
    }
  };

  // Initialize form with question data when modal opens
  useEffect(() => {
    if (questionId && isOpen) {
      getQuestion();
    }
  }, [questionId, isOpen]);

  // Add option to current question
  const addOption = () => {
    setCurrentQuestion(prev => ({
      ...prev,
      options: [...prev.options, { optionText: '', isCorrect: false }],
    }));
  };

  // Update specific option text
  const updateOptionText = (index: number, value: string) => {
    setCurrentQuestion(prev => {
      const opts = [...prev.options];
      opts[index] = { ...opts[index], optionText: value };
      return { ...prev, options: opts };
    });
  };

  // Toggle option correctness for multiple choice
  const toggleOptionCorrectness = (index: number) => {
    setCurrentQuestion(prev => {
      const opts = [...prev.options];
      opts[index] = { ...opts[index], isCorrect: !opts[index].isCorrect };
      return { ...prev, options: opts };
    });
  };

  // Set single correct answer for single choice, true/false, yes/no
  const setSingleCorrectAnswer = (index: number) => {
    setCurrentQuestion(prev => {
      const opts = prev.options.map((opt, i) => ({
        ...opt,
        isCorrect: i === index,
      }));
      return { ...prev, options: opts };
    });
  };

  // Set correct answer for true/false questions
  const setTrueFalseAnswer = (value: string) => {
    setCurrentQuestion(prev => ({
      ...prev,
      options: [
        { optionText: 'True', isCorrect: value === 'True' },
        { optionText: 'False', isCorrect: value === 'False' },
      ],
    }));
  };

  // Set correct answer for yes/no questions
  const setYesNoAnswer = (value: string) => {
    setCurrentQuestion(prev => ({
      ...prev,
      options: [
        { optionText: 'Yes', isCorrect: value === 'Yes' },
        { optionText: 'No', isCorrect: value === 'No' },
      ],
    }));
  };

  // Remove option from current question
  const removeOption = (index: number) => {
    setCurrentQuestion(prev => {
      const opts = prev.options.filter((_, i) => i !== index);
      return { ...prev, options: opts };
    });
  };

  // Toggle question status
  const toggleStatus = () => {
    setCurrentQuestion(prev => ({
      ...prev,
      status: prev.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE',
    }));
  };

  // Handle question type change
  const handleQuestionTypeChange = (value: ExamTypes) => {
    let newOptions: IQuestionOption[] = [{ optionText: '', isCorrect: false }];

    if (value === ExamTypes.TRUE_FALSE) {
      newOptions = [
        { optionText: 'True', isCorrect: false },
        { optionText: 'False', isCorrect: false },
      ];
    } else if (value === ExamTypes.YES_NO) {
      newOptions = [
        { optionText: 'Yes', isCorrect: false },
        { optionText: 'No', isCorrect: false },
      ];
    }

    setCurrentQuestion(prev => ({
      ...prev,
      questionType: value,
      options: newOptions,
    }));
  };

  // Save question
  const handleSave = async () => {
    if (!currentQuestion.questionText.trim() || !currentQuestion.questionType) {
      toast.error('Please fill in all required fields');
      return;
    }

    // Filter out empty options
    const validOptions = currentQuestion.options.filter(opt =>
      opt.optionText.trim(),
    );

    if (validOptions.length === 0) {
      toast.error('Please add at least one option');
      return;
    }

    // Validate that at least one option is correct
    const hasCorrectAnswer = validOptions.some(opt => opt.isCorrect);
    if (!hasCorrectAnswer) {
      toast.error('Please select at least one correct answer');
      return;
    }

    setIsSubmitting(true);

    try {
      const payload = {
        topicId,
        questionType: currentQuestion.questionType,
        questionText: currentQuestion.questionText,
        status: currentQuestion.status,
        options: validOptions,
      };

      const response = await apiClient.put(
        API_END_POINTS.QUESTION_UPDATE + questionId,
        {
          data: payload,
        },
      );

      toast.success('Question updated successfully');
      onSave();
      handleClose();
    } catch (error) {
      console.error('Error updating question:', error);
      toast.error('Failed to update question. Please try again.');
    } finally {
      setIsSubmitting(false);
    }
  };

  // Handle modal close
  const handleClose = () => {
    if (!isSubmitting) {
      onClose();
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={handleClose}>
      <DialogContent className="!content-max-h-[80vh] !content-w-2/3 !content-overflow-y-auto content-text-white">
        <DialogHeader>
          <DialogTitle className="content-text-white">
            Edit Question
          </DialogTitle>
          <DialogDescription>
            Modify the question details and save changes.
          </DialogDescription>
        </DialogHeader>

        <Card>
          <CardHeader>
            <div className="content-flex content-items-center content-justify-between">
              <div>
                <CardTitle>Question Details</CardTitle>
                <CardDescription>
                  Update question information and settings
                </CardDescription>
              </div>
            </div>
          </CardHeader>

          <CardContent className="content-space-y-4">
            {/* Question Type */}
            <div>
              <Label htmlFor="question-type">Type *</Label>
              <Select
                value={currentQuestion.questionType}
                onValueChange={handleQuestionTypeChange}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Select type" />
                </SelectTrigger>
                <SelectContent>
                  {questionTypes.map(t => (
                    <SelectItem key={t.value} value={t.value}>
                      {t.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            {/* Question Status */}

            <div>
              <Label htmlFor="question-status">Status *</Label>
              <Select
                value={currentQuestion.status}
                onValueChange={toggleStatus}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Select status" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="ACTIVE">Active</SelectItem>
                  <SelectItem value="INACTIVE">Inactive</SelectItem>
                </SelectContent>
              </Select>
            </div>

            {/* Question Text */}
            <div>
              <Label htmlFor="question-text">Question Text *</Label>
              <Textarea
                id="question-text"
                value={currentQuestion.questionText}
                onChange={e =>
                  setCurrentQuestion(prev => ({
                    ...prev,
                    questionText: e.target.value,
                  }))
                }
                placeholder="Enter your question"
                rows={3}
              />
            </div>

            {/* Multiple Choice Questions */}
            {currentQuestion.questionType === ExamTypes.MULTIPLE_CHOICE && (
              <Fragment>
                <div className="content-mb-2 content-flex content-items-center content-justify-between">
                  <Label>Options *</Label>
                  <Button variant="outline" size="sm" onClick={addOption}>
                    Add Option
                  </Button>
                </div>

                {currentQuestion.options.map((opt, idx) => (
                  <div key={idx} className="content-space-y-2">
                    <div className="content-flex content-items-center content-gap-2">
                      <Input
                        value={opt.optionText}
                        onChange={e => updateOptionText(idx, e.target.value)}
                        placeholder={`Option ${idx + 1}`}
                      />
                      {currentQuestion.options.length > 1 && (
                        <Button
                          variant="secondary"
                          size="sm"
                          onClick={() => removeOption(idx)}
                        >
                          Remove
                        </Button>
                      )}
                    </div>
                  </div>
                ))}
              </Fragment>
            )}
            {currentQuestion.questionType === ExamTypes.MULTIPLE_CHOICE && (
              <Fragment>
                <div className="content-mb-2 content-flex content-items-center content-justify-between">
                  <Label>Correct Answer *</Label>
                </div>
                {currentQuestion.options.map((opt, idx) => (
                  <div key={idx}>
                    {opt.optionText.trim() && (
                      <div className="content-relative content-flex content-items-center content-gap-2">
                        <CustomCheckbox
                          checked={opt.isCorrect}
                          onChange={() => toggleOptionCorrectness(idx)}
                        />
                        <span className="content-text-sm content-text-gray-300">
                          {opt.optionText}
                        </span>
                      </div>
                    )}
                  </div>
                ))}
              </Fragment>
            )}

            {/* Single Choice Questions */}
            {currentQuestion.questionType === ExamTypes.SINGLE_CHOICE && (
              <Fragment>
                <div className="content-mb-2 content-flex content-items-center content-justify-between">
                  <Label>Options *</Label>
                  <Button variant="outline" size="sm" onClick={addOption}>
                    Add Option
                  </Button>
                </div>

                {currentQuestion.options.map((opt, idx) => (
                  <div key={idx} className="content-space-y-2">
                    <div className="content-flex content-items-center content-gap-2">
                      <Input
                        value={opt.optionText}
                        onChange={e => updateOptionText(idx, e.target.value)}
                        placeholder={`Option ${idx + 1}`}
                      />
                      {currentQuestion.options.length > 1 && (
                        <Button
                          variant="secondary"
                          size="sm"
                          onClick={() => removeOption(idx)}
                        >
                          Remove
                        </Button>
                      )}
                    </div>
                  </div>
                ))}
              </Fragment>
            )}
            {currentQuestion.questionType === ExamTypes.SINGLE_CHOICE && (
              <Fragment>
                {currentQuestion.options.map((opt, idx) => (
                  <div key={idx} className="content-space-y-2">
                    {opt.optionText.trim() && (
                      <div className="content-relative content-flex content-items-center content-gap-2">
                        <CustomCheckbox
                          variant="exam"
                          checked={opt.isCorrect}
                          onChange={() => setSingleCorrectAnswer(idx)}
                        />
                        <span className="content-ml-2 content-text-sm content-text-gray-300">
                          {opt.optionText}
                        </span>
                      </div>
                    )}
                  </div>
                ))}
              </Fragment>
            )}

            {/* True/False Questions */}
            {currentQuestion.questionType === ExamTypes.TRUE_FALSE && (
              <div>
                <Label>Correct Answer *</Label>
                <Select
                  value={
                    currentQuestion.options.find(opt => opt.isCorrect)
                      ?.optionText || ''
                  }
                  onValueChange={setTrueFalseAnswer}
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Select correct answer" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="True">True</SelectItem>
                    <SelectItem value="False">False</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            )}

            {/* Yes/No Questions */}
            {currentQuestion.questionType === ExamTypes.YES_NO && (
              <div>
                <Label>Correct Answer *</Label>
                <Select
                  value={
                    currentQuestion.options.find(opt => opt.isCorrect)
                      ?.optionText || ''
                  }
                  onValueChange={setYesNoAnswer}
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Select correct answer" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="Yes">Yes</SelectItem>
                    <SelectItem value="No">No</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            )}
          </CardContent>
        </Card>

        {/* Modal Actions */}
        <div className="content-flex content-justify-end content-gap-2 content-pt-4">
          <Button
            variant="outline"
            onClick={handleClose}
            disabled={isSubmitting}
          >
            Cancel
          </Button>
          <Button onClick={handleSave} disabled={isSubmitting}>
            <Save className="content-mr-2 content-size-4" />
            {isSubmitting ? 'Saving...' : 'Save Changes'}
          </Button>
        </div>
      </DialogContent>
    </Dialog>
  );
};

export default QuestionEditModal;
