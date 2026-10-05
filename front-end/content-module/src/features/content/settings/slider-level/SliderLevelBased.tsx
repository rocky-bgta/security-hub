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
import {
  ContentTypes,
  IBackgroundSettings,
  IContentBlockHandle,
  IContentBlockSettings,
  IDataValidationHandle,
  ISliderLevelButtonAddProp,
  ISliderLevelContent,
  ISliderLevelStepAddProp,
} from 'models/Content';
import { TabNames } from 'models/Tab';
import {
  BackgroundFormattingDefault,
  SliderLevelButtonDefault,
  SliderLevelStepDefault,
} from 'utils/ContentDefaultValues';
import { formatContentDataBasedOnType } from 'utils/Formatter';
import ButtonSection from './ButtonSection';
import StepSection from './StepSection';

interface IStep extends ISliderLevelStepAddProp {
  buttons: Array<ISliderLevelButtonAddProp>;
}

enum ConfirmDialogType {
  None = 'none',
  Step = 'step',
  Button = 'button',
}

const TabOptions = [
  { label: 'Setting', value: TabNames.SETTING },
  { label: 'Background', value: TabNames.BACKGROUND },
];

const SliderLevelBased = forwardRef<
  IContentBlockHandle,
  IContentBlockSettings<ISliderLevelContent>
>(({ content, isPartofInteractiveContent = false }, ref) => {
  const selectedStepRef = useRef<IDataValidationHandle>(null);
  const selectedButtonRef = useRef<IDataValidationHandle>(null);

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
    navigationButtonText: string;
    popupText: string;
    popupButtonText: string;
  }>({
    title: '',
    description: '',
    navigationButtonText: '',
    popupText: '',
    popupButtonText: '',
  });
  const [selectedButtonId, setSelectedButtonId] = useState<string>('-1');
  const [selectedButtonError, setSelectedButtonError] = useState<{
    navigationButtonText: string;
    acceptanceText: string;
    popupText: string;
    popupButtonText: string;
  }>({
    navigationButtonText: '',
    acceptanceText: '',
    popupText: '',
    popupButtonText: '',
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
        buttons:
          step.buttons.map(button => ({
            ...button.additionalProperties,
            id: button.additionalProperties.id ?? uuidv4(),
          })) ?? [],
      }));

      setSteps(formattedSteps);
      setSelectedStepId(formattedSteps[0]?.id ?? '-1');
      setSelectedButtonId(formattedSteps[0]?.buttons[0]?.id ?? '-1');

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
    [ConfirmDialogType.Button]: {
      message: 'Are you sure you want to delete this button?',
      loadingText: 'Deleting...',
      onConfirm: () => handleDeleteButton(),
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
    setSelectedButtonId(
      steps.find(step => step.id === stepId)?.buttons[0]?.id ?? '-1',
    );
  };

  const handleAddStep = () => {
    const newStep = {
      ...SliderLevelStepDefault,
      id: uuidv4(),
      buttons: [],
    };
    setSteps([...steps, newStep]);
    setSelectedStepId(newStep.id);
    setSelectedButtonId('-1');
  };

  const handleDeleteStep = () => {
    const updatedSteps = steps.filter(step => step.id !== selectedStepId);
    setSteps([...updatedSteps]);
    setSelectedStepId(updatedSteps[0]?.id ?? '-1');
    setSelectedButtonId(updatedSteps[0]?.buttons[0]?.id ?? '-1');
    handleCloseDialog();
  };

  const updateSteps = (updatedStep: ISliderLevelStepAddProp) => {
    setSteps([
      ...steps.map(step => {
        if (step.id === updatedStep.id) {
          return { ...step, ...updatedStep };
        }
        return step;
      }),
    ]);
  };

  const handleAddButton = () => {
    const newButton = { ...SliderLevelButtonDefault, id: uuidv4() };
    setSteps([
      ...steps.map(step => {
        if (step.id === selectedStepId) {
          return {
            ...step,
            buttons: [...step.buttons, newButton],
          };
        }
        return step;
      }),
    ]);
    setSelectedButtonId(newButton.id);
  };

  const handleDeleteButton = () => {
    const updatedSteps = [
      ...steps.map(step => {
        if (step.id === selectedStepId) {
          return {
            ...step,
            buttons: step.buttons.filter(
              button => button.id !== selectedButtonId,
            ),
          };
        }
        return step;
      }),
    ];
    setSteps(updatedSteps);
    setSelectedButtonId(
      updatedSteps.find(step => step.id === selectedStepId)?.buttons[0]?.id ??
        '-1',
    );
    handleCloseDialog();
  };

  const updateButtons = (updatedButton: ISliderLevelButtonAddProp) => {
    setSteps([
      ...steps.map(step => {
        if (step.id === selectedStepId) {
          return {
            ...step,
            buttons: step.buttons.map(button => {
              if (button.id === updatedButton.id) {
                return { ...button, ...updatedButton };
              }
              return button;
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

  const validateButtonFields = () => {
    if (steps.length === 0 || !selectedButtonRef.current) return true;
    if (steps.find(step => step.id === selectedStepId)?.buttons.length === 0)
      return false;
    const { success } = selectedButtonRef.current?.validateData()!;
    return success;
  };

  useImperativeHandle(ref, () => ({
    validateAndGetData: () => {
      const isValid =
        validateFields() && validateStepFields() && validateButtonFields();
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
            sliderLevelContent: {
              steps,
            },
          },
          ContentTypes.SLIDER_LEVEL,
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
                      value={selectedButtonId}
                      onValueChange={value => setSelectedButtonId(value)}
                    >
                      <SelectTrigger disabled={false}>
                        <SelectValue placeholder="Select a Button" />
                      </SelectTrigger>
                      <SelectContent>
                        {steps
                          .find(step => step.id === selectedStepId)!
                          .buttons.map((button, index) => ({
                            id: button.id,
                            label: `Button ${index + 1}`,
                            value: button.id,
                          }))
                          .map(option => (
                            <SelectItem key={option.id} value={option.value}>
                              {option.label}
                            </SelectItem>
                          ))}
                      </SelectContent>
                    </Select>
                    <Button
                      onClick={handleAddButton}
                      className="content-w-36"
                      size="sm"
                    >
                      Add Button
                    </Button>
                    {steps.find(step => step.id === selectedStepId)!.buttons
                      .length > 0 && (
                      <Button
                        onClick={() => setDialogType(ConfirmDialogType.Button)}
                        className="!content-bg-transparent content-p-0"
                      >
                        <DeleteIcon />
                      </Button>
                    )}
                  </div>

                  {steps.find(step => step.id === selectedStepId)?.buttons
                    ?.length === 0 ? (
                    <p>No buttons added yet</p>
                  ) : (
                    selectedButtonId !== '-1' && (
                      <ButtonSection
                        ref={selectedButtonRef}
                        button={
                          steps
                            .find(step => step.id === selectedStepId)
                            ?.buttons.find(
                              button => button.id === selectedButtonId,
                            )!
                        }
                        errors={selectedButtonError}
                        updateButtons={updateButtons}
                        updateErrors={setSelectedButtonError}
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

SliderLevelBased.displayName = 'SliderLevelBased';

export default SliderLevelBased;
