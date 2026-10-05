import {
  ChangeEvent,
  forwardRef,
  useEffect,
  useImperativeHandle,
  useState,
} from 'react';

import { Tabs, TabsContent, TabsList, TabsTrigger } from 'common/Tab';
import ImageCard from 'components/ImageCard';
import InputCard from 'components/InputCard';
import BackgroundSettings from 'features/content/settings/BackgroundSettings';
import TypeSection from 'features/content/settings/TypeSection';
import {
  ContentTypes,
  IBackgroundSettings,
  IContentBlockHandle,
  IContentBlockSettings,
  IFileFields,
  IInputFields,
  ITextContent,
} from 'models/Content';
import { ISelectOption } from 'models/Input';
import { TabNames } from 'models/Tab';
import { TypeOptions } from 'utils/Constants';
import { BackgroundFormattingDefault } from 'utils/ContentDefaultValues';
import { formatContentDataBasedOnType } from 'utils/Formatter';

const TabOptions = [
  { label: 'Setting', value: TabNames.SETTING },
  { label: 'Background', value: TabNames.BACKGROUND },
];

const TextBased = forwardRef<
  IContentBlockHandle,
  IContentBlockSettings<ITextContent>
>(({ content, isPartofInteractiveContent = false }, ref) => {
  const [activeTab, setActiveTab] = useState<TabNames>(TabNames.SETTING);

  const [contentName, setContentName] = useState<{
    text: string;
    error: string;
  }>({
    text: '',
    error: '',
  });
  const [selectedTypes, setSelectedTypes] = useState<Array<ISelectOption>>([]);
  const [typedInputs, setTypedInputs] = useState<Array<IInputFields>>([]);
  const [featureImage, setFeatureImage] = useState<IFileFields>({
    link: '',
    selectedFile: null,
  });

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
          error: '',
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
          error: '',
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
          error: '',
        });
      }

      setSelectedTypes(tempSelectedTypes);
      setTypedInputs(tempTypedInputs);
      setFeatureImage(prev => ({
        ...prev,
        link: content?.specific?.featureImageLink,
      }));
      setBackgroundSettings(prev => ({
        ...prev,
        ...content.specific.backgroundFormatting,
        backgroundOpacity:
          content.specific.backgroundFormatting.backgroundOpacity + '',
      }));
    }
  }, [content]);

  const handleChangeInput = (e: ChangeEvent<HTMLInputElement>) => {
    const { value } = e.target;

    setContentName({
      text: value,
      error: value.trim().length === 0 ? 'Content name is required' : '',
    });
  };

  const handleUpdateFeatureImage = (file: File | null) =>
    setFeatureImage({
      link: '',
      selectedFile: file,
    });

  const validateFields = () => {
    let isValid = true;

    if (contentName.text.trim().length === 0) {
      isValid = false;
      setContentName({
        ...contentName,
        error: 'Content name is required',
      });
    }

    const updatedTypedInputs = typedInputs.map(input => {
      if (input.value.length === 0) {
        isValid = false;
        return { ...input, error: input.label + ' is required' };
      }
      return { ...input, error: '' };
    });
    setTypedInputs(updatedTypedInputs);

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
            contentName: contentName.text,
            updateContent: true,
            backgroundSettings,
            typedInputs,
            featureImage,
          },
          ContentTypes.TEXT,
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
                value: contentName.text,
                error: contentName.error,
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

        <ImageCard
          title="Feature Image"
          subTitle="Image"
          selectedImage={featureImage.link}
          selectedFile={featureImage.selectedFile}
          onUploadImage={handleUpdateFeatureImage}
        />
      </TabsContent>
      <TabsContent value={TabNames.BACKGROUND}>
        <BackgroundSettings
          backgroundSettings={backgroundSettings}
          updateBackgroundSettings={setBackgroundSettings}
        />
      </TabsContent>
    </Tabs>
  );
});

TextBased.displayName = 'TextBased';

export default TextBased;
