import {
  ChangeEvent,
  forwardRef,
  Fragment,
  useImperativeHandle,
  useState,
} from 'react';
import { v4 as uuidv4 } from 'uuid';

import { DeleteIcon } from 'assets/icons';
import { Button } from 'common/Button';
import { Checkbox } from 'common/Checkbox';
import { Input } from 'common/Input';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import TextEditor from 'common/TextEditor';
import InputCard from 'components/InputCard';
import { IDataValidationHandle, IQuizFields } from 'models/Content';

interface IProps {
  data: IQuizFields;
  updateData: (data: IQuizFields) => void;
}

const ScenarioQuiz = forwardRef<IDataValidationHandle, IProps>(
  ({ data, updateData }, ref) => {
    const [option, setOption] = useState<string>('');
    const [selectedQuestionId, setSelectedQuestionId] = useState<string>(
      data?.questions?.[0]?.id ?? '',
    );
    const [errors, setErrors] = useState<{
      question: string;
      scenarioText: string;
      questions: string;
      options: string;
    }>({
      question: '',
      scenarioText: '',
      questions: '',
      options: '',
    });

    const scenarioText = data?.scenarioText || '';
    const questions = data?.questions || [];
    const selectedQuestion = questions?.find(
      question => question.id === selectedQuestionId,
    );

    const handleUpdateEditor = (value: string) => {
      if (value === '<p><br></p>') return;

      updateData({
        ...data,
        scenarioText: value,
      });

      setErrors({
        ...errors,
        scenarioText:
          value.trim().length === 0 ? 'Scenario text is required' : '',
      });
    };

    const handleChangeInput = (
      e: ChangeEvent<HTMLInputElement | HTMLSelectElement>,
    ) => {
      const { id, value } = e.target;

      if (id === 'selectedQuestionId') {
        if (validateFields()) {
          setSelectedQuestionId(value);
        }

        return;
      }

      if (id === 'question') {
        updateData({
          ...data,
          questions: questions.map(question =>
            question.id === selectedQuestionId
              ? { ...question, question: value }
              : question,
          ),
        });
        setErrors({
          ...errors,
          question: value.trim().length === 0 ? 'Question is required' : '',
        });
        return;
      }

      setOption(value);
    };

    const handleAddOption = () => {
      if (option.trim().length) {
        updateData({
          ...data,
          questions: questions.map(question =>
            question.id === selectedQuestionId
              ? {
                  ...question,
                  options: [
                    ...question.options,
                    {
                      id: uuidv4(),
                      option: option,
                      isCorrect: false,
                    },
                  ],
                }
              : question,
          ),
        });
        setOption('');
        setErrors({
          ...errors,
          options: '',
        });
      }
    };

    const handleRemoveOption = (id: string) => {
      const updatedOptions = selectedQuestion?.options.filter(
        option => option.id !== id,
      )!;

      updateData({
        ...data,
        questions: questions.map(question =>
          question.id === selectedQuestionId
            ? {
                ...question,
                options: updatedOptions,
              }
            : question,
        ),
      });

      setErrors({
        ...errors,
        options:
          updatedOptions.length === 0
            ? 'At least two options are required'
            : '',
      });
    };

    const handleToggleCorrect = (id: string) => {
      const updatedQuestions = questions.map(question =>
        question.id === selectedQuestionId
          ? {
              ...question,
              options: question.options.map(option =>
                option.id === id
                  ? { ...option, isCorrect: !option.isCorrect }
                  : option,
              ),
            }
          : question,
      );
      updateData({
        ...data,
        questions: updatedQuestions,
      });

      const correctOptions = updatedQuestions
        .find(question => question.id === selectedQuestionId)
        ?.options.some(option => option.isCorrect);
      setErrors({
        ...errors,
        options: !correctOptions
          ? 'At least one correct option is required'
          : '',
      });
    };

    const handleAddQuestion = () => {
      const newQuestion = {
        id: uuidv4(),
        question: '',
        options: [],
      };
      updateData({
        ...data,
        questions: [...questions, newQuestion],
      });
      setSelectedQuestionId(newQuestion.id);
      setErrors({
        ...errors,
        questions: '',
      });
    };

    const handleDeleteQuestion = () => {
      const updatedQuestions = questions.filter(
        question => question.id !== selectedQuestionId,
      );
      updateData({
        ...data,
        questions: updatedQuestions,
      });
      setSelectedQuestionId(questions[0]?.id ?? '');
      setErrors({
        question: '',
        scenarioText: '',
        options: '',
        questions:
          updatedQuestions.length > 0
            ? ''
            : 'At least one question is required',
      });
    };

    const validateFields = () => {
      let isValid = true,
        updatedErrors = { ...errors };

      if (scenarioText.trim().length === 0) {
        isValid = false;
        updatedErrors.scenarioText = 'Scenario text is required';
      }

      if (questions.length < 1) {
        isValid = false;
        updatedErrors.questions = 'At least one question is required';
      }

      if (selectedQuestion?.question.trim().length === 0) {
        isValid = false;
        updatedErrors.question = 'Question is required';
      }

      if (selectedQuestion!.options.length < 2) {
        isValid = false;
        updatedErrors.options = 'At least two options are required';
      } else {
        const correctOptions = selectedQuestion?.options.some(
          option => option.isCorrect,
        );
        if (!correctOptions) {
          isValid = false;
          updatedErrors.options = 'At least one correct option is required';
        }
      }

      setErrors(updatedErrors);

      return isValid;
    };

    useImperativeHandle(ref, () => ({
      validateData: () => {
        return { success: validateFields() };
      },
    }));

    return (
      <Fragment>
        <div className="content-mt-5">
          <TextEditor value={scenarioText} onChange={handleUpdateEditor} />
          {errors.scenarioText && (
            <p className="content-mt-1 content-text-sm content-text-red-500">
              {errors.scenarioText}
            </p>
          )}
        </div>
        <div className="content-mt-5 content-flex content-gap-2">
          <Select
            value={selectedQuestionId}
            onValueChange={value =>
              handleChangeInput({
                target: { id: 'selectedQuestionId', value },
              } as any)
            }
          >
            <SelectTrigger disabled={false}>
              <SelectValue placeholder="Select Question" />
            </SelectTrigger>
            <SelectContent>
              {questions
                ?.map((question, index) => ({
                  id: question.id,
                  label: `Question ${index + 1}`,
                  value: question.id,
                }))
                .map(option => (
                  <SelectItem key={option.id} value={option.value}>
                    {option.label}
                  </SelectItem>
                ))}
            </SelectContent>
          </Select>

          <Button
            onClick={handleAddQuestion}
            className="content-w-1/3"
            size="sm"
          >
            Add Question
          </Button>
          {questions?.length > 0 && (
            <Button
              onClick={handleDeleteQuestion}
              className="!content-bg-transparent content-p-0"
            >
              <DeleteIcon />
            </Button>
          )}
        </div>

        {selectedQuestion && (
          <div className="content-mt-5">
            <InputCard
              title="Question Title"
              inputFields={[
                {
                  className:
                    'content-my-2 content-w-full content-rounded content-border content-px-3 content-py-2',
                  type: 'text',
                  id: 'question',
                  value: selectedQuestion.question,
                  error: errors.question,
                  placeholder: 'Enter Question',
                  onChange: handleChangeInput,
                },
              ]}
            />

            <div className="content-mt-5">
              <InputCard title="Option" inputFields={[]}>
                <div className="content-flex content-gap-2">
                  <Input
                    className="content-w-2/3 content-rounded content-border content-px-3 content-py-2"
                    id="option"
                    value={option}
                    placeholder="Enter option"
                    onChange={handleChangeInput}
                  />
                  <Button
                    className="content-w-1/3"
                    onClick={handleAddOption}
                    size="sm"
                  >
                    Add Option
                  </Button>
                </div>

                {selectedQuestion.options.length > 0 && (
                  <div className="content-space-y-3">
                    <p className="content-mb-3 content-border-b content-p-2">
                      Options
                    </p>
                    {selectedQuestion.options.map(item => (
                      <div
                        key={item.id}
                        className="content-mb-1 content-flex content-items-center content-justify-between content-rounded content-border content-border-slate-100 content-p-2"
                      >
                        <label className="content-flex content-cursor-pointer content-items-center content-gap-2 content-text-base content-font-normal">
                          <Checkbox
                            checked={item.isCorrect}
                            onCheckedChange={() =>
                              handleToggleCorrect(item.id!)
                            }
                          />
                          {item.option}
                        </label>
                        <Button
                          className="!content-bg-transparent !content-p-0 content-text-red-900"
                          onClick={() => handleRemoveOption(item.id!)}
                        >
                          <DeleteIcon fill="currentColor" />
                        </Button>
                      </div>
                    ))}
                  </div>
                )}
                {errors.options && (
                  <p className="content-mt-1 content-text-sm content-text-red-500">
                    {errors.options}
                  </p>
                )}
              </InputCard>
            </div>
          </div>
        )}
        {errors.questions && (
          <p className="content-mt-1 content-text-sm content-text-red-500">
            {errors.questions}
          </p>
        )}
      </Fragment>
    );
  },
);

ScenarioQuiz.displayName = 'ScenarioQuiz';

export default ScenarioQuiz;
