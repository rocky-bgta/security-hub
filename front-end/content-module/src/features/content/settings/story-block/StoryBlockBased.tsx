import {
  ChangeEvent,
  forwardRef,
  Fragment,
  useEffect,
  useImperativeHandle,
  useRef,
  useState,
} from 'react';
import { v4 as uuidv4 } from 'uuid';

import { DeleteIcon } from 'assets/icons';
import { Button } from 'common/Button';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import { Tabs, TabsContent, TabsList, TabsTrigger } from 'common/Tab';
import ConfirmDialog from 'components/ConfirmDialog';
import InputCard from 'components/InputCard';
import BackgroundSettings from 'features/content/settings/BackgroundSettings';
import StepSection from 'features/content/settings/story-block/StepSection';
import StorySection from 'features/content/settings/story-block/StorySection';
import {
  ContentTypes,
  IBackgroundSettings,
  IContentBlockHandle,
  IContentBlockSettings,
  IDataValidationHandle,
  IStoryBlockContent,
  IStoryBlockStepAddProp,
  IStoryBlockStoryAddProp,
} from 'models/Content';
import { TabNames } from 'models/Tab';
import {
  BackgroundFormattingDefault,
  StoryBlockStepDefault,
  StoryBlockStoryDefault,
} from 'utils/ContentDefaultValues';
import { formatContentDataBasedOnType } from 'utils/Formatter';

interface IStep extends IStoryBlockStepAddProp {
  stories: Array<IStoryBlockStoryAddProp>;
}

enum ConfirmDialogType {
  None = 'none',
  Step = 'step',
  Story = 'story',
}

const TabOptions = [
  { label: 'Setting', value: TabNames.SETTING },
  { label: 'Background', value: TabNames.BACKGROUND },
];

const StoryBlockBased = forwardRef<
  IContentBlockHandle,
  IContentBlockSettings<IStoryBlockContent>
>(({ content, isPartofInteractiveContent = false }, ref) => {
  const selectedStepRef = useRef<IDataValidationHandle>(null);
  const selectedStoryRef = useRef<IDataValidationHandle>(null);

  const [activeTab, setActiveTab] = useState<TabNames>(TabNames.SETTING);

  const [contentName, setContentName] = useState<{
    text: string;
    error: string;
  }>({
    text: '',
    error: '',
  });
  const [steps, setSteps] = useState<Array<IStep>>([]);
  const [selectedStepId, setSelectedStepId] = useState<string>('-1');
  const [selectedStepError, setSelectedStepError] = useState<{
    title: string;
    description: string;
    advisorInstructionText: string;
    popupText: string;
    popupButtonText: string;
  }>({
    title: '',
    description: '',
    advisorInstructionText: '',
    popupText: '',
    popupButtonText: '',
  });
  const [selectedStoryId, setSelectedStoryId] = useState<string>('-1');
  const [selectedStoryError, setSelectedStoryError] = useState<{
    description: string;
    actionButtonTitle: string;
    agreeText: string;
    ignoreText: string;
  }>({
    description: '',
    actionButtonTitle: '',
    agreeText: '',
    ignoreText: '',
  });
  const [dialogType, setDialogType] = useState<ConfirmDialogType>(
    ConfirmDialogType.None,
  );

  const [backgroundSettings, setBackgroundSettings] =
    useState<IBackgroundSettings>({
      ...BackgroundFormattingDefault.backgroundFormatting,
      selectedFile: null,
    });

  useEffect(() => {
    if (content?.specific) {
      setContentName({
        ...contentName,
        text: content.common.contentName,
      });

      const formattedSteps = content.specific.steps.map(step => ({
        ...step.additionalProperties,
        featureImageFile: null,
        id: step.additionalProperties.id ?? uuidv4(),
        stories:
          step.stories?.map(story => ({
            ...story.additionalProperties,
            featureImageFile: null,
            id: story.additionalProperties.id ?? uuidv4(),
          })) ?? [],
      }));

      setSteps(formattedSteps);
      setSelectedStepId(formattedSteps[0]?.id ?? '-1');
      setSelectedStoryId(formattedSteps[0]?.stories[0]?.id ?? '-1');

      setBackgroundSettings(prev => ({
        ...prev,
        ...content.specific.backgroundFormatting,
        backgroundOpacity:
          content.specific.backgroundFormatting.backgroundOpacity + '',
      }));
    }
  }, [content]);

  const DialogMap = {
    [ConfirmDialogType.None]: {
      message: '',
      loadingText: '',
      onConfirm: () => {},
    },
    [ConfirmDialogType.Step]: {
      message: 'Are you sure you want to delete this step?',
      loadingText: 'Deleting...',
      onConfirm: () => handleDeleteStep(),
    },
    [ConfirmDialogType.Story]: {
      message: 'Are you sure you want to delete this story?',
      loadingText: 'Deleting...',
      onConfirm: () => handleDeleteStory(),
    },
  };

  const handleChangeInput = (e: ChangeEvent<HTMLInputElement>) => {
    const { value } = e.target;

    setContentName({
      text: value,
      error: value.trim().length === 0 ? 'Content name is required' : '',
    });
  };

  const handleStepChange = (e: ChangeEvent<HTMLSelectElement>) => {
    if (!validateStepFields()) return;

    const stepId = e.target.value;
    setSelectedStepId(stepId);
    setSelectedStoryId(
      steps.find(step => step.id === stepId)?.stories[0]?.id ?? '-1',
    );
  };

  const handleAddStep = () => {
    const newStep = {
      ...StoryBlockStepDefault,
      id: uuidv4(),
      stories: [],
    };
    setSteps([...steps, newStep]);
    setSelectedStepId(newStep.id);
    setSelectedStoryId('-1');
  };

  const handleDeleteStep = () => {
    const updatedSteps = [...steps.filter(step => step.id !== selectedStepId)];
    setSteps(updatedSteps);
    setSelectedStepId(updatedSteps[0]?.id ?? '-1');
    setSelectedStoryId(updatedSteps[0]?.stories[0]?.id ?? '-1');
    handleCloseDialog();
  };

  const updateSteps = (updatedStep: IStoryBlockStepAddProp) => {
    setSteps([
      ...steps.map(step => {
        if (step.id === updatedStep.id) {
          return { ...step, ...updatedStep };
        }
        return step;
      }),
    ]);
  };

  const handleAddStory = () => {
    const newStory = { ...StoryBlockStoryDefault, id: uuidv4() };
    setSteps([
      ...steps.map(step => {
        if (step.id === selectedStepId) {
          return {
            ...step,
            stories: [...step.stories, newStory],
          };
        }
        return step;
      }),
    ]);
    setSelectedStoryId(newStory.id);
  };

  const handleDeleteStory = () => {
    const updatedSteps = [
      ...steps.map(step => {
        if (step.id === selectedStepId) {
          return {
            ...step,
            stories: step.stories.filter(story => story.id !== selectedStoryId),
          };
        }
        return step;
      }),
    ];
    setSteps(updatedSteps);
    setSelectedStoryId(
      updatedSteps.find(step => step.id === selectedStepId)?.stories[0]?.id ??
        '-1',
    );
    handleCloseDialog();
  };

  const updateStories = (updatedStory: IStoryBlockStoryAddProp) => {
    setSteps([
      ...steps.map(step => {
        if (step.id === selectedStepId) {
          return {
            ...step,
            stories: step.stories.map(story => {
              if (story.id === updatedStory.id) {
                return { ...story, ...updatedStory };
              }
              return story;
            }),
          };
        }
        return step;
      }),
    ]);
  };

  const validateFields = () => {
    let isValid = true;

    if (contentName.text.trim().length === 0) {
      isValid = false;
      setContentName({
        ...contentName,
        error: 'Content name is required',
      });
    }

    return isValid;
  };

  const validateStepFields = () => {
    if (steps.length === 0 || !selectedStepRef.current) return true;
    const { success } = selectedStepRef.current?.validateData()!;
    return success;
  };

  const validateStoryFields = () => {
    if (steps.length === 0 || !selectedStoryRef.current) return true;
    if (steps.find(step => step.id === selectedStepId)?.stories.length === 0)
      return false;
    const { success } = selectedStoryRef.current?.validateData()!;
    return success;
  };

  useImperativeHandle(ref, () => ({
    validateAndGetData: () => {
      const isValid =
        validateFields() && validateStepFields() && validateStoryFields();
      if (!isValid) {
        return { success: false };
      }

      return {
        success: true,
        data: formatContentDataBasedOnType(
          {
            id: content.id!,
            contentName: contentName.text,
            updateContent: true,
            backgroundSettings,
            storyBlockContent: {
              steps,
            },
          },
          ContentTypes.STORY_BLOCK,
        ),
      };
    },
  }));

  const handleCloseDialog = () => setDialogType(ConfirmDialogType.None);

  return (
    <Fragment>
      <Tabs
        value={activeTab}
        onValueChange={activeTab => {
          if (validateFields() && validateStepFields() && validateStoryFields())
            setActiveTab(activeTab as TabNames);
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
                  value: contentName.text,
                  error: contentName.error,
                  placeholder: 'Enter content name',
                  onChange: handleChangeInput,
                },
              ]}
            />
          )}

          <div className="content-my-6">
            <div className="content-mb-4 content-flex content-gap-2">
              {' '}
              <Select
                value={selectedStepId}
                onValueChange={value =>
                  handleStepChange({ target: { value } } as any)
                }
              >
                <SelectTrigger disabled={false}>
                  <SelectValue placeholder="Select a Step" />
                </SelectTrigger>
                <SelectContent>
                  {steps
                    ?.map((step, index) => ({
                      id: step.id,
                      label: `Step ${index + 1}`,
                      value: step.id,
                    }))
                    .map(option => (
                      <SelectItem key={option.id} value={option.value}>
                        {option.label}
                      </SelectItem>
                    ))}
                </SelectContent>
              </Select>
              <Button
                onClick={handleAddStep}
                className="content-w-28"
                size="sm"
              >
                Add Step
              </Button>
              {steps?.length > 0 && (
                <Button
                  onClick={() => setDialogType(ConfirmDialogType.Step)}
                  className="!content-bg-transparent content-p-0"
                >
                  <DeleteIcon />
                </Button>
              )}
            </div>

            {steps?.length === 0 ? (
              <p>No steps added yet</p>
            ) : (
              selectedStepId !== '-1' && (
                <div className="content-flex content-flex-col content-gap-4">
                  <StepSection
                    ref={selectedStepRef}
                    step={steps.find(step => step.id === selectedStepId)!}
                    errors={selectedStepError}
                    updateSteps={updateSteps}
                    updateErrors={setSelectedStepError}
                  />

                  <div className="content-flex content-gap-2">
                    {' '}
                    <Select
                      value={selectedStoryId}
                      onValueChange={value => {
                        if (!validateStoryFields()) return;
                        setSelectedStoryId(value);
                      }}
                    >
                      <SelectTrigger disabled={false}>
                        <SelectValue placeholder="Select a Story" />
                      </SelectTrigger>
                      <SelectContent>
                        {steps
                          .find(step => step.id === selectedStepId)!
                          .stories.map((story, index) => ({
                            id: story.id,
                            label: `Story ${index + 1}`,
                            value: story.id,
                          }))
                          .map(option => (
                            <SelectItem key={option.id} value={option.value}>
                              {option.label}
                            </SelectItem>
                          ))}
                      </SelectContent>
                    </Select>
                    <Button
                      onClick={handleAddStory}
                      className="content-w-32"
                      size="sm"
                    >
                      Add Story
                    </Button>
                    {steps.find(step => step.id === selectedStepId)!.stories
                      .length > 0 && (
                      <Button
                        onClick={() => setDialogType(ConfirmDialogType.Story)}
                        className="!content-bg-transparent content-p-0"
                      >
                        <DeleteIcon />
                      </Button>
                    )}
                  </div>

                  {steps.find(step => step.id === selectedStepId)?.stories
                    ?.length === 0 ? (
                    <p>No stories added yet</p>
                  ) : (
                    selectedStoryId !== '-1' && (
                      <StorySection
                        ref={selectedStoryRef}
                        story={
                          steps
                            .find(step => step.id === selectedStepId)
                            ?.stories.find(
                              story => story.id === selectedStoryId,
                            )!
                        }
                        errors={selectedStoryError}
                        updateStories={updateStories}
                        updateErrors={setSelectedStoryError}
                      />
                    )
                  )}
                </div>
              )
            )}
          </div>
        </TabsContent>
        <TabsContent value={TabNames.BACKGROUND}>
          <BackgroundSettings
            backgroundSettings={backgroundSettings}
            updateBackgroundSettings={setBackgroundSettings}
          />
        </TabsContent>
      </Tabs>

      <ConfirmDialog
        isOpen={dialogType !== ConfirmDialogType.None}
        message={DialogMap[dialogType].message}
        loadingText="Deleting..."
        onClose={handleCloseDialog}
        onConfirm={DialogMap[dialogType].onConfirm}
      />
    </Fragment>
  );
});

StoryBlockBased.displayName = 'StoryBlockBased';

export default StoryBlockBased;
