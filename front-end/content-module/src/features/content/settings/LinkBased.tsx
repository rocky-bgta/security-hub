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
  ILinkContent,
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

const LinkBased = forwardRef<
  IContentBlockHandle,
  IContentBlockSettings<ILinkContent>
>(({ content, isPartofInteractiveContent = false }, ref) => {
  const [activeTab, setActiveTab] = useState<TabNames>(TabNames.SETTING);

  const [selectedTypes, setSelectedTypes] = useState<Array<ISelectOption>>([]);
  const [typedInputs, setTypedInputs] = useState<Array<IInputFields>>([]);
  const [inputs, setInputs] = useState<Array<IInputFields>>([
    {
      key: 'contentName',
      label: 'Content name',
      value: '',
      placeholder: 'Enter content name',
      error: '',
    },
    {
      key: 'linkText',
      label: 'Text',
      value: '',
      placeholder: 'Enter text',
      error: '',
    },
    {
      key: 'url',
      label: 'URL',
      value: '',
      placeholder: 'Enter URL',
      error: '',
    },
    { key: 'linkColor', value: '#000000' },
  ]);
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

      const tempInputs = inputs.map(input => {
        switch (input.key) {
          case 'contentName':
            return {
              ...input,
              value: content.common.contentName,
            };
          case 'linkText':
            return {
              ...input,
              value: content.specific.linkFormatting.linkText,
            };
          case 'url':
            return {
              ...input,
              value: content.specific.linkFormatting.url,
            };
          case 'linkColor':
            return {
              ...input,
              value: content.specific.linkFormatting.linkColor,
            };
          default:
            return input;
        }
      });

      setSelectedTypes(tempSelectedTypes);
      setTypedInputs(tempTypedInputs);
      setInputs(tempInputs);
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
    const { id, value } = e.target;

    const updatedInput = inputs.map(input => {
      if (input.key === id)
        return {
          ...input,
          value,
          error: value.trim().length ? '' : input.label + ' is required',
        };

      return input;
    });

    setInputs(updatedInput);
  };

  const handleResetColor = (id: string) => {
    const [key] = id.split('_');

    const updatedInputs = inputs.map(input => {
      if (input.key === key) {
        return { ...input, value: '#000000' };
      }
      return input;
    });

    setInputs(updatedInputs);
  };

  const handleUploadFile = (file: File | null) =>
    setFeatureImage({
      link: '',
      selectedFile: file,
    });

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

    const updatedInputs = inputs.map(input => {
      if (input.value.length === 0) {
        isValid = false;
        return { ...input, error: input.label + ' is required' };
      }
      return input;
    });
    setInputs(updatedInputs);

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
              ?.value as string,
            updateContent: true,
            backgroundSettings,
            typedInputs,
            inputs,
            featureImage,
          },
          ContentTypes.LINK,
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

        <TypeSection
          selectedTypes={selectedTypes}
          setSelectedTypes={setSelectedTypes}
          typedInputs={typedInputs}
          setTypedInputs={setTypedInputs}
        />

        <div className="content-my-5">
          <InputCard
            title="Link"
            inputFields={[
              {
                className:
                  'content-mt-2 content-w-full content-rounded content-border content-px-3 content-py-2',
                type: 'text',
                id: 'linkText',
                label: inputs.find(input => input.key === 'linkText')?.label,
                value: inputs.find(input => input.key === 'linkText')?.value,
                error: inputs.find(input => input.key === 'linkText')?.error,
                placeholder: inputs.find(input => input.key === 'linkText')
                  ?.placeholder,
                onChange: handleChangeInput,
              },
              {
                className:
                  'content-mt-2 content-w-full content-rounded content-border content-px-3 content-py-2',
                type: 'text',
                id: 'url',
                label: inputs.find(input => input.key === 'url')?.label,
                value: inputs.find(input => input.key === 'url')?.value,
                error: inputs.find(input => input.key === 'url')?.error,
                placeholder: inputs.find(input => input.key === 'url')
                  ?.placeholder,
                onChange: handleChangeInput,
              },
              {
                type: 'color',
                id: 'linkColor',
                value: inputs.find(input => input.key === 'linkColor')?.value,
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
          onUploadImage={handleUploadFile}
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

LinkBased.displayName = 'LinkBased';

export default LinkBased;
