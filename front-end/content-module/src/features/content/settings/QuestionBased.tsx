import {
  ChangeEvent,
  forwardRef,
  Fragment,
  useEffect,
  useImperativeHandle,
  useState,
} from 'react';
import { v4 as uuidv4 } from 'uuid';

import { Button } from 'common/Button';
import { Checkbox } from 'common/Checkbox';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import { Tabs, TabsContent, TabsList, TabsTrigger } from 'common/Tab';
import InputCard from 'components/InputCard';
import BackgroundSettings from 'features/content/settings/BackgroundSettings';
import {
  ContentTypes,
  IBackgroundSettings,
  IContentBlockHandle,
  IContentBlockSettings,
  IInputFields,
  IQuestionContent,
  IQuestionOption,
  Score,
} from 'models/Content';
import { TabNames } from 'models/Tab';
import { BackgroundFormattingDefault } from 'utils/ContentDefaultValues';
import { formatContentDataBasedOnType } from 'utils/Formatter';

const TabOptions = [
  { label: 'Setting', value: TabNames.SETTING },
  { label: 'Background', value: TabNames.BACKGROUND },
  { label: 'Scoring', value: TabNames.SCORING },
];

const Dropdowns = {
  difficulty: [
    { id: '1', label: 'Easy', value: Score.EASY },
    { id: '2', label: 'Medium', value: Score.MEDIUM },
    { id: '3', label: 'Hard', value: Score.HARD },
  ],
};

const QuestionBased = forwardRef<
  IContentBlockHandle,
  IContentBlockSettings<IQuestionContent>
>(({ content, isPartofInteractiveContent = false }, ref) => {
  const [activeTab, setActiveTab] = useState<TabNames>(TabNames.SETTING);

  const [inputs, setInputs] = useState<Array<IInputFields>>([
    {
      key: 'contentName',
      label: 'Content name',
      value: '',
      placeholder: 'Enter content name',
      error: '',
    },
    {
      key: 'question',
      label: 'Question',
      value: '',
      placeholder: 'Enter question title',
      error: '',
    },
    {
      key: 'option',
      label: 'Option',
      value: '',
      placeholder: 'Enter option',
      error: '',
    },
    {
      key: 'score',
      label: 'Score',
      value: '',
      placeholder: 'Enter difficulty',
      error: '',
    },
  ]);
  const [options, setOptions] = useState<Array<IQuestionOption>>([]);
  const [scorable, setScorable] = useState<boolean>(false);

  const [backgroundSettings, setBackgroundSettings] =
    useState<IBackgroundSettings>({
      ...BackgroundFormattingDefault.backgroundFormatting,
      selectedFile: null,
    });

  useEffect(() => {
    if (content?.specific) {
      const tempInputs = inputs.map(input => {
        switch (input.key) {
          case 'contentName':
            return {
              ...input,
              value: content.common.contentName,
            };
          case 'question':
            return {
              ...input,
              value: content.specific.question,
            };
          case 'score':
            return {
              ...input,
              value: content.specific.score,
            };
          default:
            return input;
        }
      });
      setInputs(tempInputs);

      const tempOptions = content.specific.options.map(item => ({
        ...item,
        id: item.id ?? uuidv4(),
      }));
      setOptions(tempOptions);

      setBackgroundSettings(prev => ({
        ...prev,
        ...content.specific.backgroundFormatting,
        backgroundOpacity:
          content.specific.backgroundFormatting.backgroundOpacity + '',
      }));

      setScorable(content.specific.scorable);
    }
  }, [content]);

  const handleChangeInput = (
    e: ChangeEvent<HTMLInputElement | HTMLSelectElement>,
  ) => {
    const { id, value } = e.target;

    const updatedInput = inputs.map(input => {
      if (input.key === id)
        return {
          ...input,
          value,
          error:
            id !== 'option'
              ? value.trim().length
                ? ''
                : input.label + ' is required'
              : '',
        };
      return input;
    });

    setInputs(updatedInput);
  };

  const handleAddOption = () => {
    const value = inputs.find(input => input.key === 'option')?.value.trim()!;
    if (value.length) {
      setOptions([
        ...options,
        { id: uuidv4(), option: value, isCorrect: false },
      ]);

      const updatedInput = inputs.map(input => {
        if (input.key === 'option') return { ...input, value: '', error: '' };
        return input;
      });

      setInputs(updatedInput);
    }
  };

  const handleToggleCorrect = (id: string) => {
    const updatedOptions = options.map(option =>
      option.id === id ? { ...option, isCorrect: !option.isCorrect } : option,
    );
    setOptions([...updatedOptions]);

    const updatedInput = inputs.map(input => {
      if (input.key === 'option') {
        const correctOption = updatedOptions.find(
          option => option.isCorrect === true,
        );
        return {
          ...input,
          error: correctOption ? '' : 'One correct answer is required',
        };
      }
      return input;
    });
    setInputs(updatedInput);
  };

  const handleRemoveOption = (id: string) =>
    setOptions(options.filter(option => option.id !== id));

  const handleChangeScorable = () => {
    setScorable(!scorable);
    const updatedInputs = inputs.map(input => {
      if (input.key === 'score') {
        return {
          ...input,
          value: scorable ? '' : Dropdowns.difficulty[0].value,
          error: '',
        };
      }
      return input;
    });
    setInputs(updatedInputs);
  };

  const validateFields = () => {
    let isValid = true;
    const updatedTypedInputs = inputs.map(input => {
      if (
        (input.key === 'question' && input.value.length === 0) ||
        (input.key === 'option' && options.length === 0)
      ) {
        isValid = false;
        return { ...input, error: input.label + ' is required' };
      }

      if (input.key === 'option') {
        const correctOption = options.find(option => option.isCorrect === true);
        if (!correctOption) {
          isValid = false;
          return { ...input, error: 'One correct answer is required' };
        }
      }

      if (scorable && input.key === 'score' && input.value.length === 0) {
        isValid = false;
        return { ...input, error: 'Score is required' };
      }

      return { ...input, error: '' };
    });
    setInputs(updatedTypedInputs);

    return isValid;
  };

  useImperativeHandle(ref, () => ({
    validateAndGetData: () => {
      const isValid = validateFields();
      if (!isValid) {
        return { success: false };
      }

      return {
        success: true,
        data: formatContentDataBasedOnType(
          {
            id: content.id!,
            contentName: inputs.find(input => input.key === 'contentName')
              ?.value!,
            updateContent: true,
            backgroundSettings,
            inputs,
            questionContent: {
              scorable,
              options,
            },
          },
          ContentTypes.QUESTION,
        ),
      };
    },
  }));

  return (
    <Tabs
      value={activeTab}
      onValueChange={activeTab => {
        if (validateFields()) setActiveTab(activeTab as TabNames);
      }}
    >
      <TabsList>
        {TabOptions.map(item => (
          <TabsTrigger key={item.value} value={item.value}>
            {item.label}
          </TabsTrigger>
        ))}
      </TabsList>

      <TabsContent value={TabNames.SETTING}>
        {!isPartofInteractiveContent && (
          <InputCard
            title="Content Name"
            inputFields={[
              {
                className:
                  'content-mt-2 content-w-full content-rounded content-border content-px-3 content-py-2',
                type: 'text',
                id: 'contentName',
                value: inputs.find(input => input.key === 'contentName')?.value,
                error: inputs.find(input => input.key === 'contentName')?.error,
                placeholder: 'Enter content name',
                onChange: handleChangeInput,
              },
            ]}
          />
        )}

        <div className="content-my-5">
          <InputCard
            title="Question Title"
            inputFields={[
              {
                className:
                  'content-my-2 content-w-full content-rounded content-border content-px-3 content-py-2',
                type: 'text',
                id: 'question',
                value: inputs.find(input => input.key === 'question')?.value,
                error: inputs.find(input => input.key === 'question')?.error,
                placeholder: inputs.find(input => input.key === 'question')
                  ?.placeholder,
                onChange: handleChangeInput,
              },
            ]}
          />
        </div>

        <div className="content-mt-5">
          <InputCard title="Option" inputFields={[]}>
            <div className="content-flex content-gap-2">
              <Input
                className="content-w-2/3 content-rounded content-border content-px-3 content-py-2"
                id="option"
                value={inputs.find(input => input.key === 'option')?.value}
                placeholder={
                  inputs.find(input => input.key === 'option')?.placeholder
                }
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

            {options.length > 0 && (
              <div>
                <p className="content-mb-3 content-border-b content-p-2">
                  Options
                </p>
                {options.map(item => (
                  <div
                    key={item.id}
                    className="content-mb-1 content-flex content-items-center content-justify-between content-rounded content-bg-slate-100 content-p-2"
                  >
                    <label className="content-flex content-cursor-pointer content-items-center content-gap-2 content-text-base content-font-normal">
                      <Checkbox
                        checked={item.isCorrect}
                        onCheckedChange={() => handleToggleCorrect(item.id!)}
                      />
                      {item.option}
                    </label>
                    <Button
                      className="!content-bg-transparent !content-p-0 content-text-stormy-gray hover:content-underline"
                      onClick={() => handleRemoveOption(item.id!)}
                    >
                      Remove
                    </Button>
                  </div>
                ))}
              </div>
            )}
            <p className="content-text-sm content-text-red-500">
              {inputs.find(input => input.key === 'option')?.error}
            </p>
          </InputCard>
        </div>
      </TabsContent>
      <TabsContent value={TabNames.BACKGROUND}>
        <BackgroundSettings
          backgroundSettings={backgroundSettings}
          updateBackgroundSettings={setBackgroundSettings}
        />
      </TabsContent>
      <TabsContent value={TabNames.SCORING}>
        <div>
          <label className="content-my-5 content-flex content-cursor-pointer content-items-center content-gap-2 content-text-base content-font-normal">
            <Checkbox
              checked={scorable}
              onCheckedChange={handleChangeScorable}
            />
            Scorable ?
          </label>
          {scorable && (
            <Fragment>
              <div className="content-mb-5">
                <Label htmlFor="score">Difficulty input</Label>
                <Select
                  value={
                    inputs.find(input => input.key === 'score')?.value ?? ''
                  }
                  onValueChange={value =>
                    handleChangeInput({
                      target: { id: 'score', value },
                    } as ChangeEvent<HTMLInputElement>)
                  }
                >
                  <SelectTrigger disabled={false}>
                    <SelectValue placeholder="Select Difficulty" />
                  </SelectTrigger>
                  <SelectContent>
                    {Dropdowns.difficulty.map(option => (
                      <SelectItem key={option.id} value={option.value}>
                        {option.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </Fragment>
          )}
        </div>
      </TabsContent>
    </Tabs>
  );
});
QuestionBased.displayName = 'QuestionBased';

export default QuestionBased;
