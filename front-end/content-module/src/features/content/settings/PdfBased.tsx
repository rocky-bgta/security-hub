import clsx from 'clsx';
import {
  ChangeEvent,
  forwardRef,
  useEffect,
  useImperativeHandle,
  useRef,
  useState,
} from 'react';

import { Button } from 'common/Button';
import { Tabs, TabsContent, TabsList, TabsTrigger } from 'common/Tab';
import { FileUploaderHandle } from 'components/FileUploader';
import ImageCard from 'components/ImageCard';
import InputCard from 'components/InputCard';
import BackgroundSettings from 'features/content/settings/BackgroundSettings';
import TypeSection from 'features/content/settings/TypeSection';
import { useValidatePdfUrl } from 'hooks/UseValidatePdfUrl';
import {
  ContentTypes,
  IBackgroundSettings,
  IContentBlockHandle,
  IContentBlockSettings,
  IFileFields,
  IInputFields,
  IPdfContent,
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

const PdfBased = forwardRef<
  IContentBlockHandle,
  IContentBlockSettings<IPdfContent>
>(({ content, isPartofInteractiveContent = false }, ref) => {
  const fileUploaderRef = useRef<FileUploaderHandle>(null);

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
      key: 'pdfText',
      label: 'File Link Text',
      value: '',
      placeholder: 'Enter text',
      error: '',
    },
    {
      key: 'pdfTextColor',
      value: '#ffffff',
    },
    {
      key: 'pdfIsRequired',
      label: 'Required to click Before continuing',
      value: 'false',
    },
  ]);
  const [pdf, setPdf] = useState<IFileFields>({
    link: '',
    selectedFile: null,
    objectURL: '',
  });
  const [featureImage, setFeatureImage] = useState<IFileFields>({
    link: '',
    selectedFile: null,
  });

  const [backgroundSettings, setBackgroundSettings] =
    useState<IBackgroundSettings>({
      ...BackgroundFormattingDefault.backgroundFormatting,
      selectedFile: null,
    });

  const { isPdf } = useValidatePdfUrl(
    inputs.find(input => input.key === 'pdfLink')?.value!,
  );

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
          case 'pdfText':
            return {
              ...input,
              value: content.specific.metadata.additionalProp1.pdfText,
            };
          case 'pdfTextColor':
            return {
              ...input,
              value: content.specific.metadata.additionalProp1.pdfTextColor,
            };
          case 'pdfIsRequired':
            return {
              ...input,
              value: content.specific.metadata.additionalProp1.pdfIsRequired
                ? 'true'
                : 'false',
            };
          default:
            return input;
        }
      });

      setSelectedTypes(tempSelectedTypes);
      setTypedInputs(tempTypedInputs);
      setInputs(tempInputs);
      setPdf(prev => ({
        ...prev,
        link: content.specific.metadata.additionalProp1.pdfLink,
      }));
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

  const pdfIsRequired =
    inputs.find(input => input.key === 'pdfIsRequired')?.value === 'true';

  const handleChangeInput = async (e: ChangeEvent<HTMLInputElement>) => {
    const { id, value } = e.target;

    if (id === 'pdfLink') {
      setPdf({
        ...pdf,
        link: value,
        selectedFile: null,
        objectURL: '',
      });

      return;
    }

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
        return { ...input, value: '#ffffff' };
      }
      return input;
    });

    setInputs(updatedInputs);
  };

  const handleUpdatePdf = (files: FileList) => {
    if (files.length > 0) {
      setPdf({
        link: '',
        selectedFile: files[0],
        objectURL: URL.createObjectURL(files[0]),
      });
    }
  };

  const handleUpdateFeatureImage = (file: File | null) =>
    setFeatureImage({
      link: '',
      selectedFile: file,
    });

  const handleClearPdf = () => {
    setPdf({
      link: '',
      selectedFile: null,
      objectURL: '',
    });

    fileUploaderRef.current?.clearFiles();
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

    const updatedInputs = inputs.map(input => {
      if (input.value.length === 0) {
        isValid = false;
        return { ...input, error: input.label + ' is required' };
      }
      return { ...input, error: '' };
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
              ?.value!,
            updateContent: true,
            backgroundSettings,
            typedInputs,
            inputs,
            featureImage,
            pdfContent: { pdf },
          },
          ContentTypes.PDF,
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
            title="PDF"
            inputFields={[
              {
                className: 'content-text-gray-700',
                type: 'file',
                id: 'pdfFile',
                ref: fileUploaderRef,
                accept: 'application/pdf',
                placeholder: 'Upload Documents',
                onUpload: handleUpdatePdf,
              },
              {
                className:
                  'content-my-2 content-w-full content-rounded content-border content-px-3 content-py-2',
                type: 'text',
                id: 'pdfText',
                label: inputs.find(input => input.key === 'pdfText')?.label,
                value: inputs.find(input => input.key === 'pdfText')?.value,
                error: inputs.find(input => input.key === 'pdfText')?.error,
                placeholder: inputs.find(input => input.key === 'pdfText')
                  ?.placeholder,
                onChange: handleChangeInput,
              },
              {
                className:
                  'content-my-2 content-w-full content-rounded content-border content-px-3 content-py-2',
                type: 'text',
                id: 'pdfLink',
                label: 'PDF Link from blob storage (Optional)',
                value: pdf.link,
                placeholder: 'Enter PDF Link',
                onChange: handleChangeInput,
              },
              {
                type: 'color',
                id: 'pdfTextColor',
                value: inputs.find(input => input.key === 'pdfTextColor')
                  ?.value,
                onChange: handleChangeInput,
                onReset: handleResetColor,
              },
            ]}
          >
            {pdf.objectURL || isPdf || (!isPdf && pdf.link) ? (
              <div className="content-mt-5 content-flex content-flex-col content-gap-y-2">
                <embed
                  src={pdf.objectURL ? pdf.objectURL : pdf.link}
                  type="application/pdf"
                  className="content-h-[500px] content-w-full content-rounded content-shadow-md"
                />
                <Button
                  onClick={handleClearPdf}
                  className="content-w-fit !content-bg-transparent content-px-0 content-text-stormy-gray content-underline"
                >
                  Clear PDF
                </Button>
              </div>
            ) : null}

            <div className="content-mt-5 content-flex content-items-center content-gap-x-2">
              <button
                onClick={() => {
                  setInputs(prev =>
                    prev.map(input => {
                      if (input.key === 'pdfIsRequired') {
                        return {
                          ...input,
                          value: pdfIsRequired ? 'false' : 'true',
                        };
                      }
                      return input;
                    }),
                  );
                }}
                className={clsx(
                  'content-relative content-h-6 content-w-12 content-rounded-full content-transition content-duration-300',
                  pdfIsRequired
                    ? 'content-bg-light-blue'
                    : 'content-bg-gray-400',
                )}
              >
                <div
                  className={clsx(
                    'content-absolute content-left-1 content-top-1 content-size-4 content-rounded-full content-bg-white content-shadow-md content-transition-transform content-duration-300',
                    pdfIsRequired
                      ? 'content-translate-x-6'
                      : 'content-translate-x-0',
                  )}
                />
              </button>
              <p>Required to click Before continuing</p>
            </div>
          </InputCard>
        </div>

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

PdfBased.displayName = 'PdfBased';

export default PdfBased;
