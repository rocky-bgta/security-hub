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
import ImageCard from 'components/ImageCard';
import InputCard from 'components/InputCard';
import BackgroundSettings from 'features/content/settings/BackgroundSettings';
import SlideSection from 'features/content/settings/slider-show/SlideSection';
import TypeSection from 'features/content/settings/TypeSection';
import {
  ContentTypes,
  IBackgroundSettings,
  IContentBlockHandle,
  IContentBlockSettings,
  IDataValidationHandle,
  IFileFields,
  IInputFields,
  ISlide,
  ISliderShowContent,
} from 'models/Content';
import { ISelectOption } from 'models/Input';
import { TabNames } from 'models/Tab';
import { TypeOptions } from 'utils/Constants';
import {
  BackgroundFormattingDefault,
  SlideDefault,
} from 'utils/ContentDefaultValues';
import { formatContentDataBasedOnType } from 'utils/Formatter';

const TabOptions = [
  { label: 'Setting', value: TabNames.SETTING },
  { label: 'Slides', value: TabNames.SLIDES },
  { label: 'Background', value: TabNames.BACKGROUND },
];

const SliderShowBased = forwardRef<
  IContentBlockHandle,
  IContentBlockSettings<ISliderShowContent>
>(({ content, isPartofInteractiveContent = false }, ref) => {
  const selectedSlideRef = useRef<IDataValidationHandle>(null);

  const [activeTab, setActiveTab] = useState<TabNames>(TabNames.SETTING);

  const [selectedTypes, setSelectedTypes] = useState<Array<ISelectOption>>([]);
  const [typedInputs, setTypedInputs] = useState<Array<IInputFields>>([]);
  const [featureImage, setFeatureImage] = useState<IFileFields>({
    link: '',
    selectedFile: null,
  });
  const [buttonValue, setButtonValue] = useState<Array<IInputFields>>([
    {
      key: 'contentName',
      label: 'Content name',
      value: '',
      placeholder: 'Enter content name',
      error: '',
    },
    {
      key: 'buttonText',
      label: 'Button Text',
      value: '',
      placeholder: 'Enter text',
      error: '',
    },
    {
      key: 'buttonTextColor',
      label: 'Button Text Color',
      value: '#ffffff',
    },
    {
      key: 'buttonColor',
      label: 'Button Color',
      value: '#ffffff',
    },
    {
      key: 'buttonHoverColor',
      label: 'Button Hover Color',
      value: '#ffffff',
    },
  ]);

  const [slides, setSlides] = useState<Array<ISlide>>([]);
  const [slideError, setSlideError] = useState<{
    title: string;
    subtitle: string;
    paragraph: string;
  }>({
    title: '',
    subtitle: '',
    paragraph: '',
  });
  const [selectedSlideId, setSelectedSlideId] = useState<string>('-1');
  const [openConfirmDialog, setOpenConfirmDialog] = useState<boolean>(false);

  const [backgroundSettings, setBackgroundSettings] =
    useState<IBackgroundSettings>({
      ...BackgroundFormattingDefault.backgroundFormatting,
      selectedFile: null,
    });

  useEffect(() => {
    if (content?.specific) {
      const tempSelectedTypes = [],
        tempTypedInputs = [];

      if (content.specific.titleFormatting.title.length > 0) {
        tempSelectedTypes.push(TypeOptions[0]);
        tempTypedInputs.push({
          key: TypeOptions[0].label,
          value: content.specific.titleFormatting.title,
          placeholder: 'Enter ' + TypeOptions[0].value,
          color: content.specific.titleFormatting.titleColor,
          contrast: content.specific.titleFormatting.titleHighContrastMode,
        });
      }
      if (content.specific.subTitleFormatting.subtitle.length > 0) {
        tempSelectedTypes.push(TypeOptions[1]);
        tempTypedInputs.push({
          key: TypeOptions[1].label,
          value: content.specific.subTitleFormatting.subtitle,
          placeholder: 'Enter ' + TypeOptions[1].value,
          color: content.specific.subTitleFormatting.subtitleColor,
          contrast:
            content.specific.subTitleFormatting.subtitleHighContrastMode,
        });
      }
      if (content.specific.paragraphFormatting.paragraph.length > 0) {
        tempSelectedTypes.push(TypeOptions[2]);
        tempTypedInputs.push({
          key: TypeOptions[2].label,
          value: content.specific.paragraphFormatting.paragraph,
          placeholder: 'Enter ' + TypeOptions[2].value,
          color: content.specific.paragraphFormatting.paragraphColor,
          contrast:
            content.specific.paragraphFormatting.paragraphHighContrastMode,
        });
      }

      const tempSlides = content.specific.slides?.map(slide => ({
        ...slide,
        featureImageFile: null,
        id: slide.id ?? uuidv4(),
      }));

      const tempButtonValue = buttonValue.map(input => {
        switch (input.key) {
          case 'contentName':
            return {
              ...input,
              value: content.common.contentName,
            };
          case 'buttonText':
            return {
              ...input,
              value: content.specific.slideAdditionalProperties.buttonText,
            };
          case 'buttonTextColor':
            return {
              ...input,
              value: content.specific.slideAdditionalProperties.buttonTextColor,
            };
          case 'buttonColor':
            return {
              ...input,
              value: content.specific.slideAdditionalProperties.buttonColor,
            };
          case 'buttonHoverColor':
            return {
              ...input,
              value:
                content.specific.slideAdditionalProperties.buttonHoverColor,
            };
          default:
            return input;
        }
      });

      setSelectedTypes(tempSelectedTypes);
      setTypedInputs(tempTypedInputs);
      setSlides(tempSlides);
      setSelectedSlideId(tempSlides[0]?.id ?? '-1');
      setButtonValue(tempButtonValue);
      setFeatureImage({
        link: content?.specific?.featureImageLink,
        selectedFile: null,
      });
      setBackgroundSettings(prev => ({
        ...prev,
        ...content.specific.backgroundFormatting,
        backgroundOpacity:
          content.specific.backgroundFormatting.backgroundOpacity + '',
      }));
    }
  }, [content]);

  const handleChangeInput = (e: ChangeEvent<HTMLInputElement>) => {
    const { id, value } = e.target;

    const updatedButtonValue = buttonValue.map(button => {
      if (button.key === id)
        return {
          ...button,
          value,
          error: value.trim().length ? '' : button.label + ' is required',
        };
      return button;
    });

    setButtonValue([...updatedButtonValue]);
  };

  const handleResetColor = (id: string) => {
    const [key] = id.split('_');

    const updatedInputs = buttonValue.map(input => {
      if (input.key === key) {
        return { ...input, value: '#ffffff' };
      }
      return input;
    });

    setButtonValue([...updatedInputs]);
  };

  const handleUploadFeatureImage = (file: File | null) => {
    setFeatureImage({
      link: '',
      selectedFile: file,
    });
  };

  const handleAddSlide = () => {
    const newSlide = {
      ...SlideDefault,
      id: uuidv4(),
    };

    setSlides([...slides, newSlide]);
    setSelectedSlideId(newSlide.id);
  };

  const handleDeleteSlide = () => {
    const updatedSlides = slides.filter(slide => slide.id !== selectedSlideId);
    setSlides([...updatedSlides]);
    setSelectedSlideId(updatedSlides[0]?.id ?? '-1');
    toggleConfirmDialog();
  };

  const updateSlides = (updatedSlide: ISlide) => {
    setSlides([
      ...slides.map(slide => {
        if (slide.id === updatedSlide.id) {
          return { ...slide, ...updatedSlide };
        }
        return slide;
      }),
    ]);
  };

  const validateFields = () => {
    let isValid = true;

    const updatedTypedInputs = typedInputs.map(input => {
      if (input.value.length === 0) {
        isValid = false;
        return { ...input, error: input.label + ' is required' };
      }
      return { ...input, error: '' };
    });
    setTypedInputs(updatedTypedInputs);

    const updatedButtonValue = buttonValue.map(input => {
      if (input.value.length === 0) {
        isValid = false;
        return { ...input, error: input.label + ' is required' };
      }
      return { ...input, error: '' };
    });
    setButtonValue(updatedButtonValue);

    return isValid;
  };

  const validateSlideFields = () => {
    if (slides.length === 0 || !selectedSlideRef.current) return true;
    const { success } = selectedSlideRef.current?.validateData()!;
    return success;
  };

  const toggleConfirmDialog = () => setOpenConfirmDialog(!openConfirmDialog);

  useImperativeHandle(ref, () => ({
    validateAndGetData: () => {
      const isValid = validateFields() && validateSlideFields();
      if (!isValid) {
        return { success: false };
      }

      return {
        success: true,
        data: formatContentDataBasedOnType(
          {
            id: content.id!,
            contentName: buttonValue.find(input => input.key === 'contentName')
              ?.value!,
            updateContent: true,
            backgroundSettings,
            typedInputs,
            featureImage,
            sliderShowContent: {
              slides,
              buttonValue,
            },
          },
          ContentTypes.SLIDER_SHOW,
        ),
      };
    },
  }));

  return (
    <Fragment>
      <Tabs
        value={activeTab}
        onValueChange={newActiveTab => {
          let isValid = true;
          if (activeTab === TabNames.SETTING) {
            isValid = validateFields();
          } else if (activeTab === TabNames.SLIDES) {
            isValid = validateSlideFields();
          }

          if (isValid) setActiveTab(newActiveTab as TabNames);
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
                  value: buttonValue.find(input => input.key === 'contentName')
                    ?.value,
                  error: buttonValue.find(input => input.key === 'contentName')
                    ?.error,
                  placeholder: 'Enter content name',
                  onChange: handleChangeInput,
                },
              ]}
            />
          )}

          <TypeSection
            selectedTypes={selectedTypes}
            setSelectedTypes={setSelectedTypes}
            typedInputs={typedInputs}
            setTypedInputs={setTypedInputs}
          />

          <div className="content-my-5">
            <InputCard
              title="Navigation Button"
              inputFields={[
                {
                  className: 'my-2 w-full rounded border px-3 py-2',
                  type: 'text',
                  id: 'buttonText',
                  value: buttonValue.find(input => input.key === 'buttonText')
                    ?.value,
                  label: buttonValue.find(input => input.key === 'buttonText')
                    ?.label,
                  error: buttonValue.find(input => input.key === 'buttonText')
                    ?.error,
                  placeholder: 'Button text',
                  onChange: handleChangeInput,
                },
                {
                  className: 'my-2',
                  type: 'color',
                  id: 'buttonTextColor',
                  value: buttonValue.find(
                    input => input.key === 'buttonTextColor',
                  )?.value,
                  label: buttonValue.find(
                    input => input.key === 'buttonTextColor',
                  )?.label,
                  error: buttonValue.find(
                    input => input.key === 'buttonTextColor',
                  )?.error,
                  onChange: handleChangeInput,
                  onReset: handleResetColor,
                },
                {
                  type: 'color',
                  id: 'buttonColor',
                  value: buttonValue.find(input => input.key === 'buttonColor')
                    ?.value,
                  label: buttonValue.find(input => input.key === 'buttonColor')
                    ?.label,
                  error: buttonValue.find(input => input.key === 'buttonColor')
                    ?.error,
                  onChange: handleChangeInput,
                  onReset: handleResetColor,
                },
                {
                  type: 'color',
                  id: 'buttonHoverColor',
                  value: buttonValue.find(
                    input => input.key === 'buttonHoverColor',
                  )?.value,
                  label: buttonValue.find(
                    input => input.key === 'buttonHoverColor',
                  )?.label,
                  error: buttonValue.find(
                    input => input.key === 'buttonHoverColor',
                  )?.error,
                  onChange: handleChangeInput,
                  onReset: handleResetColor,
                },
              ]}
            />
          </div>

          <ImageCard
            title="Feature Image"
            subTitle="Image"
            selectedImage={featureImage.link}
            selectedFile={featureImage.selectedFile}
            onUploadImage={handleUploadFeatureImage}
          />
        </TabsContent>
        <TabsContent value={TabNames.SLIDES}>
          <div className="content-mt-6">
            <div className="content-flex content-gap-2">
              <Select
                value={selectedSlideId}
                onValueChange={value => setSelectedSlideId(value)}
              >
                <SelectTrigger disabled={false}>
                  <SelectValue placeholder="Select a Slide" />
                </SelectTrigger>
                <SelectContent>
                  {slides
                    ?.map((slide, index) => ({
                      id: slide.id,
                      label: `Slide ${index + 1}`,
                      value: slide.id,
                    }))
                    .map(option => (
                      <SelectItem key={option.id} value={option.value}>
                        {option.label}
                      </SelectItem>
                    ))}
                </SelectContent>
              </Select>

              <Button
                onClick={handleAddSlide}
                className="content-w-28"
                size="sm"
              >
                Add Slide
              </Button>
              {slides?.length > 0 && (
                <Button
                  onClick={toggleConfirmDialog}
                  className="!content-bg-transparent content-p-0"
                >
                  <DeleteIcon />
                </Button>
              )}
            </div>

            {slides?.length === 0 ? (
              <p className="content-my-4">No slides added yet</p>
            ) : (
              selectedSlideId !== '-1' && (
                <SlideSection
                  ref={selectedSlideRef}
                  slide={slides.find(slide => slide.id === selectedSlideId)!}
                  updateSlides={updateSlides}
                  errors={slideError}
                  updateErrors={setSlideError}
                />
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
        isOpen={openConfirmDialog}
        message="Are you sure you want to delete this slide?"
        loadingText="Deleting..."
        onClose={toggleConfirmDialog}
        onConfirm={handleDeleteSlide}
      />
    </Fragment>
  );
});

SliderShowBased.displayName = 'SliderShowBased';

export default SliderShowBased;
