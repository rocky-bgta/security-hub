import { DeleteIcon } from 'assets/icons';
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
import ConfirmDialog from 'components/ConfirmDialog';
import { useAPI } from 'hooks/UseAPI';
import { Edit, Save } from 'lucide-react';
import { ExamTypes } from 'models/Exam';
import { Fragment, useState } from 'react';
import { IoIosArrowDown } from 'react-icons/io';
import { toast } from 'react-toastify';
import { API_END_POINTS } from 'routes/APIEndpoints';

interface QuestionOption {
  optionText: string;
  isCorrect: boolean;
}

interface Question {
  topicId: string;
  questionType: ExamTypes;
  questionText: string;
  options: QuestionOption[];
}

interface ExamData {
  questions: Question[];
}

interface IProps {
  topicId: string; // Add topicId as a required prop
  isOpen: boolean;
  onClose: () => void;
  onSave: () => void;
}

// Question types configuration
const questionTypes = [
  { value: ExamTypes.MULTIPLE_CHOICE, label: 'Multiple Choice' },
  { value: ExamTypes.SINGLE_CHOICE, label: 'Single Choice' },
  { value: ExamTypes.TRUE_FALSE, label: 'True/False' },
  { value: ExamTypes.YES_NO, label: 'Yes/No' },
];

const QuestionModal = ({ isOpen, onClose, topicId, onSave }: IProps) => {
  // Main exam data state
  const [examData, setExamData] = useState<ExamData>({
    questions: [],
  });

  // Current question being created/edited
  const [currentQuestion, setCurrentQuestion] = useState({
    questionText: '',
    questionType: '' as ExamTypes,
    options: [{ optionText: '', isCorrect: false }],
  });

  // UI state
  const [selectedQuestion, setSelectedQuestion] = useState('');
  const [editingQuestionIndex, setEditingQuestionIndex] = useState<
    number | null
  >(null);

  // Confirmation dialog state
  const [openConfirmDialog, setOpenConfirmDialog] = useState(false);
  const [questionToDelete, setQuestionToDelete] = useState<number | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [submittingExam, setSubmittingExam] = useState(false);

  const apiClient = useAPI();

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

  // Set correct answer for true/false and yes/no questions
  const setCorrectAnswer = (value: string) => {
    const answerValue = value === 'True' || value === 'Yes';
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

  // Save current question to exam
  const saveQuestion = () => {
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

    const newQuestion: Question = {
      topicId,
      questionText: currentQuestion.questionText,
      questionType: currentQuestion.questionType,
      options: validOptions,
    };

    if (editingQuestionIndex !== null) {
      // Update existing question
      setExamData(prev => ({
        ...prev,
        questions: prev.questions.map((q, index) =>
          index === editingQuestionIndex ? newQuestion : q,
        ),
      }));
      setEditingQuestionIndex(null);
      toast.success('Question updated in local storage successfully');
    } else {
      // Add new question
      setExamData(prev => ({
        ...prev,
        questions: [...prev.questions, newQuestion],
      }));
      toast.success('Question added in local storage successfully');
    }

    // Reset current question
    setCurrentQuestion({
      questionText: '',
      questionType: '' as ExamTypes,
      options: [{ optionText: '', isCorrect: false }],
    });
  };

  // Handle CSV template download
  const handleDownloadTemplate = () => {
    const link = document.createElement('a');
    link.href =
      'https://aspiretss.s3.us-east-1.amazonaws.com/a-sat-v2.0/Sample_questions_Template.csv';
    link.download = 'sample_exam_questions.csv';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  // Handle CSV import
  const handleImportCSV = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    const formData = new FormData();
    formData.append('file', file);

    try {
      toast.info('Importing questions...');
      const response = await apiClient.post(
        API_END_POINTS.PRODUCT_EXAM_CSV_UPLOAD + `topicId=${topicId}`,
        {
          data: formData,
          headers: { 'Content-Type': 'multipart/form-data' },
        },
      );

      const importedQuestions: Question[] = response.data.questions.map(
        (q: any) => ({
          topicId,
          questionText: q.questionText || q.text,
          questionType: q.questionType,
          status: q.status,
          options:
            q.options?.map((opt: any) => ({
              optionText: opt.optionText,
              isCorrect: opt.isCorrect,
            })) || [],
        }),
      );

      setExamData(prev => ({
        ...prev,
        questions: [...prev.questions, ...importedQuestions],
      }));

      toast.success(
        `Successfully imported ${importedQuestions.length} questions`,
      );
      e.target.value = '';
    } catch (error) {
      console.error('Error uploading CSV:', error);
      toast.error('Failed to import CSV. Please try again.');
    }
  };

  // Delete a question - show confirmation dialog
  const handleDeleteQuestion = (questionIndex: number) => {
    setQuestionToDelete(questionIndex);
    setOpenConfirmDialog(true);
  };

  // Confirm delete question
  const handleDeleteConfirm = async () => {
    if (questionToDelete === null) return;

    setSubmitting(true);

    try {
      // Simulate async operation (you can add actual API call here if needed)
      await new Promise(resolve => setTimeout(resolve, 1000));

      setExamData(prev => ({
        ...prev,
        questions: prev.questions.filter(
          (_, index) => index !== questionToDelete,
        ),
      }));

      toast.success('Question deleted in local storage successfully');
    } catch (error) {
      toast.error('Failed to delete question');
    } finally {
      setSubmitting(false);
      setOpenConfirmDialog(false);
      setQuestionToDelete(null);
    }
  };

  // Handle close confirmation dialog
  const handleCloseConfirmDialog = () => {
    if (!submitting) {
      setOpenConfirmDialog(false);
      setQuestionToDelete(null);
    }
  };

  // Edit a question
  const handleEditQuestion = (questionIndex: number) => {
    const question = examData.questions[questionIndex];
    if (question) {
      setCurrentQuestion({
        questionText: question.questionText,
        questionType: question.questionType,
        options: question.options,
      });
      setEditingQuestionIndex(questionIndex);
      toast.info('Question loaded for editing in local storage');
    }
  };

  // Toggle question details visibility
  const handleSelectQuestion = (questionIndex: number) => {
    const questionId = questionIndex.toString();
    setSelectedQuestion(prev => (prev === questionId ? '' : questionId));
  };

  // Cancel editing
  const cancelEdit = () => {
    setEditingQuestionIndex(null);
    setCurrentQuestion({
      questionText: '',
      questionType: '' as ExamTypes,
      options: [{ optionText: '', isCorrect: false }],
    });
  };

  // Handle modal save
  const handleSave = async () => {
    setSubmittingExam(true);
    if (examData.questions.length === 0) {
      toast.error('Please add at least one question before saving');
      return;
    }

    try {
      await apiClient.post(API_END_POINTS.QUESTION_CREATE, {
        data: examData,
      });

      onClose();
      onSave();
      toast.success('Exam saved successfully');
      setSelectedQuestion('');
      setEditingQuestionIndex(null);
      setSubmittingExam(false);
      setExamData({
        questions: [],
      });
    } catch (error) {
      console.error('Error ', error);
      toast.error('Failed to save exam. Please try again.');
    } finally {
      setSubmittingExam(false);
    }
  };

  // Handle modal close
  const handleClose = () => {
    setCurrentQuestion({
      questionText: '',
      questionType: '' as ExamTypes,
      options: [{ optionText: '', isCorrect: false }],
    });
    setSelectedQuestion('');
    setEditingQuestionIndex(null);
    onClose();
  };

  // Handle question type change
  const handleQuestionTypeChange = (value: ExamTypes) => {
    let newOptions: QuestionOption[] = [{ optionText: '', isCorrect: false }];

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

  return (
    <Fragment>
      <Dialog open={isOpen} onOpenChange={handleClose}>
        <DialogContent className="!content-flex !content-max-h-[80vh] !content-w-2/3 content-flex-col !content-overflow-y-auto content-text-white">
          <DialogHeader>
            <DialogTitle className="content-text-white">
              Add Questions to Exam
            </DialogTitle>
            <DialogDescription>
              The questions will be added to the exam.
            </DialogDescription>
          </DialogHeader>

          {/* Add Questions Card */}
          <Card>
            <div className="content-flex content-items-center content-justify-between">
              <CardHeader>
                <CardTitle>
                  {editingQuestionIndex !== null
                    ? 'Edit Question'
                    : 'Add Questions to Exam'}
                </CardTitle>
                <CardDescription>
                  {editingQuestionIndex !== null
                    ? 'Modify existing question'
                    : 'Define your questions to add to the exam'}
                </CardDescription>
              </CardHeader>
              <div className="content-space-x-3 content-p-6">
                <Button
                  onClick={handleDownloadTemplate}
                  size="sm"
                  variant="secondary"
                >
                  Download Template
                </Button>
                <Label htmlFor="file">
                  <span className="content-cursor-pointer content-rounded-md content-bg-primary content-px-3 content-py-2.5 content-text-sm content-text-white">
                    Import CSV
                  </span>
                  <Input
                    id="file"
                    type="file"
                    accept=".csv"
                    className="content-hidden"
                    onChange={handleImportCSV}
                  />
                </Label>
              </div>
            </div>
            <CardContent className="content-space-y-4">
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

              <div>
                <Label htmlFor="question-text">Text *</Label>
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
                />
              </div>

              {/* Multiple Choice Questions */}
              {currentQuestion.questionType === ExamTypes.MULTIPLE_CHOICE && (
                <Fragment>
                  <div className="content-mb-2 content-flex content-items-center content-justify-between">
                    <Label>Options *</Label>
                    <Button variant="default" size="sm" onClick={addOption}>
                      Add Option
                    </Button>
                  </div>
                  {currentQuestion.options.map((opt, idx) => (
                    <div
                      key={idx}
                      className="content-flex content-items-center content-gap-2"
                    >
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
                  ))}

                  <div className="content-text-cloudy-white">
                    Correct Answer *
                  </div>
                  {currentQuestion.options
                    .filter(o => o.optionText.trim())
                    .map((opt, idx) => (
                      <div
                        key={idx}
                        className="content-relative content-flex content-items-center content-gap-2"
                      >
                        <CustomCheckbox
                          checked={opt.isCorrect}
                          onChange={() => toggleOptionCorrectness(idx)}
                        />
                        <span className="content-ml-2 content-text-white">
                          {opt.optionText}
                        </span>
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
                    <div
                      key={idx}
                      className="content-flex content-items-center content-gap-2"
                    >
                      <Input
                        value={opt.optionText}
                        onChange={e => updateOptionText(idx, e.target.value)}
                        placeholder={`Option ${idx + 1}`}
                      />
                      {currentQuestion.options.length > 1 && (
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => removeOption(idx)}
                        >
                          Remove
                        </Button>
                      )}
                    </div>
                  ))}

                  <div className="content-text-cloudy-white">
                    Correct Answer *
                  </div>
                  {currentQuestion.options
                    .filter(o => o.optionText.trim())
                    .map((opt, idx) => (
                      <div
                        key={idx}
                        className="content-relative content-flex content-items-center content-gap-2"
                      >
                        <input
                          type="radio"
                          name="SINGLE_CHOICE"
                          className="content-mr-2 content-size-5"
                          value={opt.optionText}
                          checked={opt.isCorrect}
                          onChange={() => setSingleCorrectAnswer(idx)}
                        />
                        <span className="content-text-white">
                          {opt.optionText}
                        </span>
                      </div>
                    ))}
                </Fragment>
              )}

              {/* True/False Questions */}
              {currentQuestion.questionType === ExamTypes.TRUE_FALSE && (
                <div>
                  <div className="content-text-cloudy-white">
                    Correct Answer *
                  </div>
                  <Select
                    value={
                      currentQuestion.options.find(opt => opt.isCorrect)
                        ?.optionText || ''
                    }
                    onValueChange={setCorrectAnswer}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Select answer" />
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
                  <div className="content-text-cloudy-white">
                    Correct Answer *
                  </div>
                  <Select
                    value={
                      currentQuestion.options.find(opt => opt.isCorrect)
                        ?.optionText || ''
                    }
                    onValueChange={setYesNoAnswer}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Select answer" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="Yes">Yes</SelectItem>
                      <SelectItem value="No">No</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
              )}

              <div className="content-flex content-gap-2">
                <Button onClick={saveQuestion}>
                  <Save className="content-mr-2 content-size-4" />
                  {editingQuestionIndex !== null
                    ? 'Update Question'
                    : 'Save Question'}
                </Button>
                {editingQuestionIndex !== null && (
                  <Button variant="outline" onClick={cancelEdit}>
                    Cancel Edit
                  </Button>
                )}
              </div>
            </CardContent>
          </Card>

          {/* Questions List */}
          {examData.questions.length > 0 && (
            <Card>
              <CardHeader>
                <CardTitle>Questions ({examData.questions.length})</CardTitle>
                <CardDescription>Review added questions</CardDescription>
              </CardHeader>
              <CardContent className="content-space-y-4">
                {examData.questions.map((q, index) => (
                  <div
                    key={index}
                    className="content-rounded content-border content-border-card-border content-p-3"
                  >
                    <div className="content-flex content-items-center content-justify-between">
                      <div className="content-flex content-items-center content-gap-3">
                        <h4 className="content-font-medium content-text-white">
                          {q.questionText}
                        </h4>
                      </div>
                      <div className="content-flex content-items-center content-gap-4">
                        <Edit
                          onClick={() => handleEditQuestion(index)}
                          className="content-size-5 content-cursor-pointer content-text-ash-gray hover:content-text-white"
                        />
                        <DeleteIcon
                          onClick={() => handleDeleteQuestion(index)}
                          fill="#9a9a9a"
                          className="content-size-4 content-cursor-pointer content-text-ash-gray hover:content-text-vibrant-red"
                        />
                        <IoIosArrowDown
                          onClick={() => handleSelectQuestion(index)}
                          className={`content-cursor-pointer content-text-xl content-text-ash-gray content-transition-transform ${
                            selectedQuestion === index.toString()
                              ? 'content-rotate-180'
                              : ''
                          }`}
                        />
                      </div>
                    </div>
                    {selectedQuestion === index.toString() && (
                      <Fragment>
                        {q.options.length > 0 && (
                          <>
                            <p className="content-mt-2 content-font-medium content-text-ash-gray">
                              Options:
                            </p>
                            <ul className="content-list-inside content-list-disc content-text-muted-foreground">
                              {q.options.map((opt, idx) => (
                                <li
                                  key={idx}
                                  className={
                                    opt.isCorrect
                                      ? 'content-text-green-600'
                                      : ''
                                  }
                                >
                                  {opt.optionText}{' '}
                                  {opt.isCorrect && '(Correct)'}
                                </li>
                              ))}
                            </ul>
                          </>
                        )}
                        <p className="content-mt-1 content-text-green-600">
                          Correct:{' '}
                          {q.options
                            .filter(opt => opt.isCorrect)
                            .map(opt => opt.optionText)
                            .join(', ')}
                        </p>
                        <p className="content-mt-1 content-text-sm content-text-gray-400">
                          Type:{' '}
                          {
                            questionTypes.find(t => t.value === q.questionType)
                              ?.label
                          }
                        </p>
                      </Fragment>
                    )}
                  </div>
                ))}
              </CardContent>
            </Card>
          )}

          {/* Modal Actions */}
          <div className="content-flex content-justify-end content-gap-2 content-pt-4">
            <Button variant="outline" onClick={handleClose}>
              Cancel
            </Button>
            <Button onClick={handleSave} disabled={submittingExam}>
              {submittingExam ? 'Saving...' : 'Save Exam'}
            </Button>
          </div>
        </DialogContent>
      </Dialog>
      {/* Delete Confirmation Dialog */}
      <ConfirmDialog
        isOpen={openConfirmDialog}
        message="Are you sure you want to delete this Question?"
        loading={submitting}
        loadingText="Deleting..."
        onClose={handleCloseConfirmDialog}
        onConfirm={handleDeleteConfirm}
      />
    </Fragment>
  );
};

export default QuestionModal;
