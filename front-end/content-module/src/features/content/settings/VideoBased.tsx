import {
  ChangeEvent,
  forwardRef,
  useEffect,
  useImperativeHandle,
  useRef,
  useState,
} from 'react';
import { v4 as uuidv4 } from 'uuid';

import { Button } from 'common/Button';
import { Card } from 'common/Card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from 'common/Tab';
import FileUploader, { FileUploaderHandle } from 'components/FileUploader';
import InputCard from 'components/InputCard';
import VideoCard from 'components/VideoCard';
import BackgroundSettings from 'features/content/settings/BackgroundSettings';
import {
  ContentTypes,
  IBackgroundSettings,
  IContentBlockHandle,
  IContentBlockSettings,
  IFileFields,
  IInputFields,
  IVideoContent,
} from 'models/Content';
import { TabNames } from 'models/Tab';
import { BackgroundFormattingDefault } from 'utils/ContentDefaultValues';
import { formatContentDataBasedOnType } from 'utils/Formatter';

const TabOptions = [
  { label: 'Setting', value: TabNames.SETTING },
  { label: 'Background', value: TabNames.BACKGROUND },
];

const VideoBased = forwardRef<
  IContentBlockHandle,
  IContentBlockSettings<IVideoContent>
>(
  (
    { content, isPartofInteractiveContent = false, uploadVideoProgress },
    ref,
  ) => {
    const captionUploaderRef = useRef<FileUploaderHandle>(null);

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
        key: 'title',
        label: 'Title',
        value: '',
        placeholder: 'Enter title',
        error: '',
      },
    ]);
    const [video, setVideo] = useState<IFileFields>({
      link: '',
      selectedFile: null,
    });
    const [caption, setCaption] = useState<IFileFields>({
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
        const tempInputs = inputs.map(input => {
          switch (input.key) {
            case 'contentName':
              return {
                ...input,
                value: content.common.contentName,
              };
            case 'title':
              return {
                ...input,
                value:
                  content.specific.metadata?.additionalProp1?.videoText ?? '',
              };
            default:
              return input;
          }
        });

        setInputs(tempInputs);

        setVideo(prev => ({
          ...prev,
          link: content.specific.interactiveVideo.videoUrl,
        }));
        setCaption(prev => ({
          ...prev,
          link: content.specific.captionUrl,
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

    const handleUpdateFile = (files: FileList) => {
      if (files.length) {
        setCaption({
          link: '',
          selectedFile: files[0],
        });
      }
    };

    const handleUpdateVideoFile = (file: File | null) => {
      setVideo({
        link: '',
        selectedFile: file,
      });
    };

    const handleClearCaption = () => {
      setCaption({ link: '', selectedFile: null });
      captionUploaderRef.current?.clearFiles();
    };

    const validateFields = () => {
      let isValid = true;

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
                ?.value!,
              updateContent: true,
              backgroundSettings,
              videoContent: {
                id: content?.specific?.interactiveVideo?.id || uuidv4(),
                title: inputs.find(input => input.key === 'title')?.value!,
                video,
                caption,
                processVideoUrl:
                  content?.specific?.interactiveVideo?.processVideoUrl,
              },
            },
            ContentTypes.VIDEO,
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
                  value: inputs.find(input => input.key === 'contentName')
                    ?.value,
                  error: inputs.find(input => input.key === 'contentName')
                    ?.error,
                  placeholder: 'Enter content name',
                  onChange: handleChangeInput,
                },
              ]}
            />
          )}

          <div className="content-my-5">
            <InputCard
              title="Title"
              inputFields={[
                {
                  className:
                    'content-my-2 content-w-full content-rounded content-border content-px-3 content-py-2',
                  type: 'text',
                  id: 'title',
                  value: inputs.find(input => input.key === 'title')?.value,
                  error: inputs.find(input => input.key === 'title')?.error,
                  placeholder: 'Enter title',
                  onChange: handleChangeInput,
                },
              ]}
            />
          </div>

          <div className="content-flex content-flex-col content-gap-y-5">
            <VideoCard
              title="Video"
              subTitle="Upload your video file below"
              selectedVideo={video.link}
              selectedFile={video.selectedFile}
              onUploadVideo={handleUpdateVideoFile}
              progress={uploadVideoProgress}
            />

            <Card title="Video Captions">
              <div className="content-p-5 content-pt-2.5">
                <p className="content-text-base content-text-[#2E384D]">
                  Make your videos more accessible adding a caption file.
                </p>

                {caption.selectedFile || caption.link ? (
                  <div className="content-mt-5 content-flex content-flex-col content-gap-y-2">
                    {caption.selectedFile ? (
                      <p className="content-text-base content-text-[#2E384D]">
                        {caption.selectedFile.name}
                      </p>
                    ) : (
                      <p className="content-text-base content-text-[#2E384D]">
                        {caption.link}
                      </p>
                    )}
                    <Button
                      onClick={handleClearCaption}
                      className="content-w-fit !content-bg-transparent content-px-0 content-text-stormy-gray content-underline"
                    >
                      Clear Caption
                    </Button>
                  </div>
                ) : null}

                <FileUploader
                  id="caption"
                  ref={captionUploaderRef}
                  containerClassName="content-mt-4"
                  accept="text/vtt"
                  placeholder="Upload .vtt file"
                  onUpload={handleUpdateFile}
                  maxSize={30}
                />
              </div>
            </Card>
          </div>
        </TabsContent>
        <TabsContent value={TabNames.BACKGROUND}>
          <BackgroundSettings
            backgroundSettings={backgroundSettings}
            updateBackgroundSettings={setBackgroundSettings}
          />
        </TabsContent>
      </Tabs>
    );
  },
);

VideoBased.displayName = 'VideoBased';

export default VideoBased;
