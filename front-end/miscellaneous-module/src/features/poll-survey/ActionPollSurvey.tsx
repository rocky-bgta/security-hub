import { Button } from 'common/Button';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from 'common/Card';
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
import { Switch } from 'common/Switch';
import { Textarea } from 'common/Textarea';
import { useAPI } from 'hooks/UseAPI';
import { Plus, Trash2 } from 'lucide-react';
import { IAnswer, IQuestion } from 'models/PollsSurvey';
import { useEffect, useState } from 'react';
import { toast } from 'react-toastify';
import { API_END_POINTS } from 'routes/APIEndpoints';
import {
  getTodayISO,
  hasPollSurveyFormErrors,
  POLL_SURVEY_FIELD_MAX_LENGTH,
  TPollSurveyFormErrors,
  TPollSurveyQuestion,
  validatePollSurveyForm,
} from 'schemas/PollSurveySchema';
import { isSuccessResponse } from 'utils/Helper';

interface IProps {
  isOpen: boolean;
  onClose: () => void;
  id?: string;
  onSubmit: () => void;
}

const DEFAULT_QUESTION: TPollSurveyQuestion = {
  id: '1',
  type: 'RADIO',
  question: '',
  options: ['', ''],
};

const ActionPollSurvey = ({ isOpen, onClose, id, onSubmit }: IProps) => {
  const apiClient = useAPI();
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    type: 'POLL',
    status: 'DRAFT',
    targetAudience: '',
    startDate: '',
    endDate: '',
    anonymous: false,
    showResults: false,
    multipleSubmissions: false,
  });
  const [originalStartDate, setOriginalStartDate] = useState('');
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState<TPollSurveyFormErrors>({});
  const [questions, setQuestions] = useState<TPollSurveyQuestion[]>([
    { ...DEFAULT_QUESTION },
  ]);

  useEffect(() => {
    if (id) {
      fetchPollSurvey();
    }
  }, [id]);

  const fetchPollSurvey = async () => {
    try {
      setLoading(true);
      const response = await apiClient.get(
        API_END_POINTS.GET_POLL_SURVEY_DETAILS.replace(':id', id ?? ''),
      );
      if (isSuccessResponse(response.statusCode)) {
        setFormData({
          title: response.data.title,
          description: response.data.description,
          type: response.data.type,
          status: response.data.status,
          targetAudience: response.data.targetAudience,
          startDate: response.data.startDate,
          endDate: response.data.endDate,
          anonymous: response.data.anonymous,
          showResults: response.data.showResults,
          multipleSubmissions: response.data.multipleSubmissions,
        });
        setOriginalStartDate(response.data.startDate ?? '');
        setQuestions(
          response.data.questions.map((question: IQuestion) => ({
            id: question.id,
            type: question.questionType,
            question: question.questionText,
            options: question.answers.map(
              (answer: IAnswer) => answer.answerText,
            ),
          })),
        );
      }
    } catch (error) {
      console.error('Error fetching poll survey:', error);
    } finally {
      setLoading(false);
    }
  };

  const clearFieldError = (field: keyof TPollSurveyFormErrors) => {
    setErrors(prev => ({ ...prev, [field]: undefined }));
  };

  const clearQuestionError = (
    questionId: string,
    field: 'questionText' | 'questionType' | 'options',
    optionIndex?: number,
  ) => {
    setErrors(prev => {
      const questionErrors = prev.questions?.[questionId];
      if (!questionErrors) return prev;

      const updatedQuestionErrors = { ...questionErrors };

      if (field === 'options' && optionIndex !== undefined) {
        const optionErrors = { ...updatedQuestionErrors.optionErrors };
        delete optionErrors[optionIndex];
        updatedQuestionErrors.optionErrors =
          Object.keys(optionErrors).length > 0 ? optionErrors : undefined;
        delete updatedQuestionErrors.options;
      } else {
        delete updatedQuestionErrors[field];
      }

      return {
        ...prev,
        questions: {
          ...prev.questions,
          [questionId]: updatedQuestionErrors,
        },
      };
    });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    const validationErrors = validatePollSurveyForm(
      {
        title: formData.title,
        description: formData.description,
        type: formData.type,
        status: formData.status,
        startDate: formData.startDate,
        endDate: formData.endDate,
        showResults: formData.showResults,
        multipleSubmissions: formData.multipleSubmissions,
        questions,
      },
      {
        isEdit: Boolean(id),
        originalStartDate,
      },
    );

    if (hasPollSurveyFormErrors(validationErrors)) {
      setErrors(validationErrors);
      return;
    }

    const payload = {
      title: formData.title.trim(),
      description: formData.description.trim(),
      type: formData.type,
      status: formData.status,
      startDate: formData.startDate,
      endDate: formData.endDate,
      showResults: formData.showResults,
      multipleSubmissions: formData.multipleSubmissions,
      questions: questions.map(question => ({
        questionText: question.question.trim(),
        questionType: question.type,
        answers: question.options
          .map(option => option.trim())
          .filter(Boolean)
          .map(option => ({
            answerText: option,
          })),
      })),
    };

    try {
      setLoading(true);
      const response = await apiClient[id ? 'put' : 'post'](
        id
          ? API_END_POINTS.UPDATE_POLL_SURVEY.replace(':id', id)
          : API_END_POINTS.CREATE_POLL_SURVEY,
        {
          data: payload,
        },
      );

      if (isSuccessResponse(response.statusCode)) {
        toast.success(
          id
            ? 'Poll/Survey updated successfully.'
            : 'Poll/Survey has been created successfully.',
        );
        onClose();
        onSubmit();
      } else {
        toast.error(response.message);
      }
    } catch (error) {
      console.error('Error creating poll/survey:', error);
      toast.error('An error occurred while creating poll/survey');
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (field: string, value: string | boolean) => {
    if (field === 'title' && typeof value === 'string') {
      if (value.length > POLL_SURVEY_FIELD_MAX_LENGTH.title) {
        setErrors(prev => ({
          ...prev,
          title: 'Title cannot exceed 100 characters.',
        }));
        return;
      }
    }

    if (field === 'description' && typeof value === 'string') {
      if (value.length > POLL_SURVEY_FIELD_MAX_LENGTH.description) {
        setErrors(prev => ({
          ...prev,
          description: 'Description cannot exceed 500 characters.',
        }));
        return;
      }
    }

    setFormData(prev => ({ ...prev, [field]: value }));

    if (field in errors) {
      clearFieldError(field as keyof TPollSurveyFormErrors);
    }
  };

  const removeQuestion = (questionId: string) => {
    setQuestions(prev => prev.filter(q => q.id !== questionId));
    setErrors(prev => {
      if (!prev.questions?.[questionId]) return prev;
      const nextQuestions = { ...prev.questions };
      delete nextQuestions[questionId];
      return {
        ...prev,
        questions:
          Object.keys(nextQuestions).length > 0 ? nextQuestions : undefined,
      };
    });
  };

  const updateQuestion = (
    questionId: string,
    field: keyof TPollSurveyQuestion,
    value: string,
  ) => {
    if (field === 'question' && value.length > POLL_SURVEY_FIELD_MAX_LENGTH.question) {
      setErrors(prev => ({
        ...prev,
        questions: {
          ...prev.questions,
          [questionId]: {
            ...prev.questions?.[questionId],
            questionText: 'Question cannot exceed 500 characters.',
          },
        },
      }));
      return;
    }

    setQuestions(prev =>
      prev.map(q => (q.id === questionId ? { ...q, [field]: value } : q)),
    );

    if (field === 'question') {
      clearQuestionError(questionId, 'questionText');
    } else if (field === 'type') {
      clearQuestionError(questionId, 'questionType');
    }
  };

  const addOption = (questionId: string) => {
    const question = questions.find(q => q.id === questionId);
    if (
      question &&
      question.options.length >= POLL_SURVEY_FIELD_MAX_LENGTH.maxOptions
    ) {
      toast.error('A maximum of 10 answer options is allowed.');
      return;
    }

    setQuestions(prev =>
      prev.map(q =>
        q.id === questionId ? { ...q, options: [...q.options, ''] } : q,
      ),
    );
    clearQuestionError(questionId, 'options');
  };

  const updateOption = (
    questionId: string,
    optionIndex: number,
    value: string,
  ) => {
    if (value.length > POLL_SURVEY_FIELD_MAX_LENGTH.option) {
      setErrors(prev => ({
        ...prev,
        questions: {
          ...prev.questions,
          [questionId]: {
            ...prev.questions?.[questionId],
            optionErrors: {
              ...prev.questions?.[questionId]?.optionErrors,
              [optionIndex]: 'Answer option cannot exceed 100 characters.',
            },
          },
        },
      }));
      return;
    }

    setQuestions(prev =>
      prev.map(q =>
        q.id === questionId
          ? {
              ...q,
              options: q.options.map((opt, index) =>
                index === optionIndex ? value : opt,
              ),
            }
          : q,
      ),
    );
    clearQuestionError(questionId, 'options', optionIndex);
  };

  const removeOption = (questionId: string, optionIndex: number) => {
    setQuestions(prev =>
      prev.map(q =>
        q.id === questionId
          ? {
              ...q,
              options: q.options.filter((_, index) => index !== optionIndex),
            }
          : q,
      ),
    );
    clearQuestionError(questionId, 'options', optionIndex);
  };

  const getQuestionErrors = (questionId: string) =>
    errors.questions?.[questionId];

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="max-h-[90vh] max-w-[65%] overflow-auto">
        <DialogHeader>
          <DialogTitle>
            {id ? 'Edit Poll/Survey' : 'Create Poll/Survey'}
          </DialogTitle>

          <DialogDescription>
            {id
              ? 'Edit the details of your poll or survey'
              : 'Create a new poll or survey'}
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit} className="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle>Basic Information</CardTitle>
              <CardDescription>
                Enter the basic details for your poll or survey
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-6">
              <div className="grid grid-cols-2 gap-4">
                <div className="col-span-2 space-y-2">
                  <Label htmlFor="title">Title *</Label>
                  <p className="text-xs text-muted-foreground">
                    Minimum 10 characters recommended
                  </p>
                  <Input
                    id="title"
                    value={formData.title}
                    onChange={e => handleChange('title', e.target.value)}
                    placeholder="e.g., Customer Feedback Survey"
                  />
                  <div className="flex items-center justify-between">
                    <p className="text-sm text-red-500">{errors.title}</p>
                    <p className="text-sm text-muted-foreground">
                      {formData.title.length}/{POLL_SURVEY_FIELD_MAX_LENGTH.title}{' '}
                      characters
                    </p>
                  </div>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="type">Type *</Label>
                  <Select
                    value={formData.type}
                    onValueChange={value => handleChange('type', value)}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Select type" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="POLL">Poll</SelectItem>
                      <SelectItem value="SURVEY">Survey</SelectItem>
                    </SelectContent>
                  </Select>
                  <p className="text-sm text-red-500">{errors.type}</p>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="status">Status *</Label>
                  <Select
                    value={formData.status}
                    onValueChange={value => handleChange('status', value)}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Select status" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="DRAFT">Draft</SelectItem>
                      <SelectItem value="ACTIVE">Active</SelectItem>
                      <SelectItem value="INACTIVE">Inactive</SelectItem>
                    </SelectContent>
                  </Select>
                  <p className="text-sm text-red-500">{errors.status}</p>
                </div>
              </div>

              <div className="space-y-2">
                <Label htmlFor="description">Description</Label>
                <p className="text-xs text-muted-foreground">
                  Optional, max 500 characters
                </p>
                <Textarea
                  id="description"
                  value={formData.description}
                  onChange={e => handleChange('description', e.target.value)}
                  placeholder="Brief description of the poll/survey"
                  rows={3}
                />
                <div className="flex items-center justify-between">
                  <p className="text-sm text-red-500">{errors.description}</p>
                  <p className="text-sm text-muted-foreground">
                    {formData.description.length}/
                    {POLL_SURVEY_FIELD_MAX_LENGTH.description} characters
                  </p>
                </div>
              </div>

              <div className="grid grid-cols-2 gap-x-4 gap-y-6">
                <div className="space-y-2">
                  <Label htmlFor="startDate">Start Date *</Label>
                  <input
                    className="date-input w-full rounded-md border border-card-border bg-transparent p-2 text-white"
                    id="startDate"
                    type="date"
                    value={formData.startDate}
                    min={id ? undefined : getTodayISO()}
                    onChange={e => handleChange('startDate', e.target.value)}
                    onClick={e => {
                      const input = e.target as HTMLInputElement;
                      input.showPicker();
                    }}
                  />
                  <p className="text-sm text-red-500">{errors.startDate}</p>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="endDate">End Date *</Label>
                  <input
                    className="date-input w-full rounded-md border border-card-border bg-transparent p-2 text-white"
                    id="endDate"
                    type="date"
                    value={formData.endDate}
                    min={formData.startDate || getTodayISO()}
                    onChange={e => handleChange('endDate', e.target.value)}
                    onClick={e => {
                      const input = e.target as HTMLInputElement;
                      input.showPicker();
                    }}
                  />
                  <p className="text-sm text-red-500">{errors.endDate}</p>
                </div>

                <div className="flex items-center space-x-2">
                  <Switch
                    id="showResults"
                    checked={formData.showResults}
                    onCheckedChange={checked =>
                      handleChange('showResults', checked)
                    }
                  />
                  <Label htmlFor="showResults">Show Results to Users</Label>
                </div>

                <div className="flex items-center space-x-2">
                  <Switch
                    id="multipleSubmissions"
                    checked={formData.multipleSubmissions}
                    onCheckedChange={checked =>
                      handleChange('multipleSubmissions', checked)
                    }
                  />
                  <Label htmlFor="multipleSubmissions">
                    Allow Multiple Submissions
                  </Label>
                </div>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Questions</CardTitle>
              <CardDescription>
                Add questions for your poll or survey
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-6">
              {errors.form && (
                <p className="text-sm text-red-500">{errors.form}</p>
              )}

              {questions.map((question, index) => {
                const questionErrors = getQuestionErrors(question.id);

                return (
                  <div
                    key={question.id}
                    className="space-y-4 rounded-lg border border-card-border p-4"
                  >
                    <div className="flex items-center justify-between">
                      <h4 className="font-medium">Question {index + 1}</h4>
                      {questions.length > 1 && (
                        <Button
                          type="button"
                          variant="outline"
                          size="sm"
                          onClick={() => removeQuestion(question.id)}
                        >
                          <Trash2 className="size-4" />
                        </Button>
                      )}
                    </div>

                    <div className="grid grid-cols-1 gap-4">
                      <div className="space-y-2">
                        <Label>Question Type *</Label>
                        <Select
                          value={question.type}
                          onValueChange={value =>
                            updateQuestion(question.id, 'type', value)
                          }
                        >
                          <SelectTrigger>
                            <SelectValue placeholder="Select question type" />
                          </SelectTrigger>
                          <SelectContent>
                            <SelectItem value="RADIO">
                              Single Choice (Radio)
                            </SelectItem>
                            <SelectItem value="MCQ">
                              Multiple Choice (Checkbox)
                            </SelectItem>
                          </SelectContent>
                        </Select>
                        <p className="text-sm text-red-500">
                          {questionErrors?.questionType}
                        </p>
                      </div>

                      <div className="space-y-2">
                        <Label>Question Text *</Label>
                        <Input
                          value={question.question}
                          onChange={e =>
                            updateQuestion(
                              question.id,
                              'question',
                              e.target.value,
                            )
                          }
                          placeholder="Enter your question"
                        />
                        <div className="flex items-center justify-between">
                          <p className="text-sm text-red-500">
                            {questionErrors?.questionText}
                          </p>
                          <p className="text-sm text-muted-foreground">
                            {question.question.length}/
                            {POLL_SURVEY_FIELD_MAX_LENGTH.question} characters
                          </p>
                        </div>
                      </div>
                    </div>

                    {(question.type === 'RADIO' || question.type === 'MCQ') && (
                      <div className="space-y-2">
                        <Label>Answer Options *</Label>
                        <p className="text-xs text-muted-foreground">
                          Minimum 2, maximum 10 options
                        </p>
                        {questionErrors?.options && (
                          <p className="text-sm text-red-500">
                            {questionErrors.options}
                          </p>
                        )}
                        {question.options.map((option, optionIndex) => (
                          <div key={optionIndex} className="flex gap-2">
                            <div className="flex w-full flex-col">
                              <Input
                                value={option}
                                onChange={e =>
                                  updateOption(
                                    question.id,
                                    optionIndex,
                                    e.target.value,
                                  )
                                }
                                placeholder={`Option ${optionIndex + 1}`}
                              />
                              <div className="flex items-center justify-between">
                                <p className="text-sm text-red-500">
                                  {
                                    questionErrors?.optionErrors?.[optionIndex]
                                  }
                                </p>
                                <p className="text-sm text-muted-foreground">
                                  {option.length}/
                                  {POLL_SURVEY_FIELD_MAX_LENGTH.option}{' '}
                                  characters
                                </p>
                              </div>
                            </div>
                            {question.options.length >
                              POLL_SURVEY_FIELD_MAX_LENGTH.minOptions && (
                              <Button
                                type="button"
                                variant="outline"
                                size="sm"
                                onClick={() =>
                                  removeOption(question.id, optionIndex)
                                }
                              >
                                <Trash2 className="size-4" />
                              </Button>
                            )}
                          </div>
                        ))}
                        <Button
                          type="button"
                          variant="outline"
                          size="sm"
                          disabled={
                            question.options.length >=
                            POLL_SURVEY_FIELD_MAX_LENGTH.maxOptions
                          }
                          onClick={() => addOption(question.id)}
                        >
                          <Plus className="mr-2 size-4" />
                          Add Option
                        </Button>
                      </div>
                    )}
                  </div>
                );
              })}
            </CardContent>
          </Card>

          <div className="flex gap-4">
            <Button
              onClick={onClose}
              type="button"
              variant="outline"
              className="flex-1"
            >
              Cancel
            </Button>
            {id ? (
              <Button type="submit" className="flex-1" disabled={loading}>
                {loading
                  ? 'Updating...'
                  : `Update ${formData.type === 'POLL' ? 'Poll' : 'Survey'}`}
              </Button>
            ) : (
              <Button type="submit" className="flex-1" disabled={loading}>
                {loading
                  ? 'Creating...'
                  : `Create ${formData.type === 'POLL' ? 'Poll' : 'Survey'}`}
              </Button>
            )}
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
};

export default ActionPollSurvey;
