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
import { Card } from 'common/Card';
import { Input } from 'common/Input';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import { Tabs, TabsContent, TabsList, TabsTrigger } from 'common/Tab';
import ConfirmDialog from 'components/ConfirmDialog';
import FileUploader, { FileUploaderHandle } from 'components/FileUploader';
import ImageCard from 'components/ImageCard';
import InputCard from 'components/InputCard';
import BackgroundSettings from 'features/content/settings/BackgroundSettings';
import TabSection from 'features/content/settings/tabbed/TabSection';
import TypeSection from 'features/content/settings/TypeSection';
import {
  ContentTypes,
  IBackgroundSettings,
  IContentBlockHandle,
  IContentBlockSettings,
  IDataValidationHandle,
  IFileFields,
  IInputFields,
  ITab,
  ITabbedContent,
} from 'models/Content';
import { ISelectOption } from 'models/Input';
import { TabNames } from 'models/Tab';
import { TypeOptions, ValidAudioFormats } from 'utils/Constants';
import {
  BackgroundFormattingDefault,
  TabDefault,
} from 'utils/ContentDefaultValues';
import { formatContentDataBasedOnType } from 'utils/Formatter';
import { getMediaDuration } from 'utils/Helper';

const TabOptions = [
  { label: 'Setting', value: TabNames.SETTING },
  { label: 'Tabs', value: TabNames.TABS },
  { label: 'Background', value: TabNames.BACKGROUND },
];

const TabbedPositionOptions = [
  { id: 1, label: 'Column', value: 'COLUMN' },
  { id: 2, label: 'Row', value: 'ROW' },
];

const TabbedBased = forwardRef<
  IContentBlockHandle,
  IContentBlockSettings<ITabbedContent>
>(({ content, isPartofInteractiveContent = false }, ref) => {
  const audioUploaderRef = useRef<FileUploaderHandle>(null);
  const selectedTabRef = useRef<IDataValidationHandle>(null);

  const [activeTab, setActiveTab] = useState<TabNames>(TabNames.SETTING);

  const [selectedTypes, setSelectedTypes] = useState<Array<ISelectOption>>([]);
  const [typedInputs, setTypedInputs] = useState<Array<IInputFields>>([]);
  const [featureImage, setFeatureImage] = useState<IFileFields>({
    link: '',
    selectedFile: null,
  });
  const [tabbedPosition, setTabbedPosition] = useState<string>('COLUMN');
  const [buttonValue, setButtonValue] = useState<Array<IInputFields>>([
    {
      key: 'contentName',
      label: 'Content name',
      value: '',
      placeholder: 'Enter content name',
      error: '',
    },
    {
      key: 'buttonTextColor',
      value: '#000000',
    },
    {
      key: 'buttonColor',
      value: '#ffffff',
    },
    {
      key: 'buttonHoverColor',
      value: '#ffffff',
    },
  ]);
  const [audio, setAudio] = useState<IFileFields>({
    link: '',
    selectedFile: null,
    objectURL: '',
  });

  const [tabs, setTabs] = useState<Array<ITab>>([]);
  const [tabError, setTabError] = useState<{
    navigationButtonText: string;
    displayTime: string;
    paragraph: string;
  }>({
    navigationButtonText: '',
    displayTime: '',
    paragraph: '',
  });
  const [selectedTabId, setSelectedTabId] = useState<string>('-1');
  const [openConfirmDialog, setOpenConfirmDialog] = useState<boolean>(false);
  const [audioSeconds, setAudioSeconds] = useState<number>(0);
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

      const tempTabs = content.specific.tabSections.map(tab => ({
        ...tab.additionalProperties,
        id: tab.additionalProperties.id ?? uuidv4(),
        audioUrl: tab.additionalProperties.audioUrl,
        audioFile: null,
      }));

      const tempButtonValue = buttonValue.map(input => {
        switch (input.key) {
          case 'contentName':
            return {
              ...input,
              value: content.common.contentName,
            };
          case 'buttonTextColor':
            return {
              ...input,
              value: content.specific.additionalProperties.buttonTextColor,
            };
          case 'buttonColor':
            return {
              ...input,
              value: content.specific.additionalProperties.buttonColor,
            };
          case 'buttonHoverColor':
            return {
              ...input,
              value: content.specific.additionalProperties.buttonHoverColor,
            };
          default:
            return input;
        }
      });

      setSelectedTypes(tempSelectedTypes);
      setTypedInputs(tempTypedInputs);
      setTabs(tempTabs);
      setSelectedTabId(tempTabs[0]?.id ?? '-1');
      setButtonValue(tempButtonValue);
      setTabbedPosition(content.specific.additionalProperties.tabbedPosition);
      setFeatureImage({
        link: content.specific.featureImageLink,
        selectedFile: null,
      });
      setAudio(prev => ({
        ...prev,
        link: content.specific.additionalProperties.audioUrl,
      }));
      setBackgroundSettings(prev => ({
        ...prev,
        ...content.specific.backgroundFormatting,
        backgroundOpacity:
          content.specific.backgroundFormatting.backgroundOpacity + '',
      }));

      // const getAudioDuration = (blobUrl: string): Promise<number> => {
      //   return new Promise((resolve, reject) => {
      //     const audio = new Audio(blobUrl);

      //     audio.addEventListener('loadedmetadata', () => {
      //       resolve(audio.duration);
      //     });

      //     audio.addEventListener('error', e => {
      //       reject(new Error('Failed to load audio metadata'));
      //     });
      //   });
      // };

      const blobUrl = content.specific.additionalProperties.audioUrl;
      if (blobUrl) {
        getMediaDuration(blobUrl, 'audio')
          .then(duration => {
            setAudioSeconds(duration);
          })
          .catch(err => {
            console.error('Error getting audio duration:', err);
          });
      }
    }
  }, [content]);

  const handleChangeInput = (
    e: ChangeEvent<HTMLInputElement | HTMLSelectElement>,
  ) => {
    const { id, value } = e.target;

    if (id === 'tabbedPosition') {
      setTabbedPosition(value);
      return;
    }

    if (id === 'audioLink') {
      setAudio({
        link: value,
        selectedFile: null,
      });
      return;
    }

    const updatedInput = buttonValue.map(input => {
      if (input.key === id)
        return {
          ...input,
          value,
          error: value.trim().length === 0 ? input.label + ' is required' : '',
        };
      return input;
    });

    setButtonValue([...updatedInput]);
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

  const handleUploadAudio = (files: FileList) => {
    const file = files[0];
    const audioUrl = URL.createObjectURL(file);
    const audio = new Audio();
    audio.src = audioUrl;
    audio.onloadedmetadata = () => {
      const duration = audio.duration;
      setAudioSeconds(duration);
    };
    if (files.length > 0) {
      setAudio({
        link: '',
        selectedFile: files[0],
        objectURL: URL.createObjectURL(files[0]),
      });
    }
  };

  const handleAddTab = () => {
    const newTab = {
      ...TabDefault,
      id: uuidv4(),
      audioUrl: '',
      audioFile: null,
    };

    setTabs([...tabs, newTab]);
    setSelectedTabId(newTab.id);
  };

  const handleDeleteTab = () => {
    const updatedTabs = tabs.filter(tab => tab.id !== selectedTabId);
    setTabs([...updatedTabs]);
    setSelectedTabId(updatedTabs[0]?.id ?? '-1');
    toggleConfirmDialog();
  };

  const updateTabs = (updatedTab: ITab) => {
    setTabs([
      ...tabs.map(tab => {
        if (tab.id === updatedTab.id) {
          return { ...tab, ...updatedTab };
        }
        return tab;
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

    return isValid;
  };

  const validateTabFields = () => {
    if (tabs.length === 0 || !selectedTabRef.current) return true;
    const { success } = selectedTabRef.current?.validateData()!;
    return success;
  };

  useImperativeHandle(ref, () => ({
    validateAndGetData: () => {
      const isValid = validateFields() && validateTabFields();
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
            tabbedContent: {
              buttonValue,
              tabbedPosition,
              audio,
              tabs,
            },
          },
          ContentTypes.TABBED,
        ),
      };
    },
  }));

  const toggleConfirmDialog = () => setOpenConfirmDialog(!openConfirmDialog);

  return (
    <Fragment>
      <Tabs
        value={activeTab}
        onValueChange={newActiveTab => {
          let isValid = true;
          if (activeTab === TabNames.SETTING) {
            isValid = validateFields();
          } else if (activeTab === TabNames.TABS) {
            isValid = validateTabFields();
          }
          if (isValid) {
            setActiveTab(newActiveTab as TabNames);
          }
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
                  className: 'content-my-2',
                  type: 'color',
                  id: 'buttonTextColor',
                  value: buttonValue.find(
                    input => input.key === 'buttonTextColor',
                  )?.value,
                  label: 'Button Text Color',
                  onChange: handleChangeInput,
                  onReset: handleResetColor,
                },
                {
                  type: 'color',
                  id: 'buttonColor',
                  value: buttonValue.find(input => input.key === 'buttonColor')
                    ?.value,
                  label: 'Button Color',
                  onChange: handleChangeInput,
                  onReset: handleResetColor,
                },
                {
                  type: 'color',
                  id: 'buttonHoverColor',
                  value: buttonValue.find(
                    input => input.key === 'buttonHoverColor',
                  )?.value,
                  label: 'Button Hover Color',
                  onChange: handleChangeInput,
                  onReset: handleResetColor,
                },
              ]}
            ></InputCard>
          </div>

          <ImageCard
            title="Feature Image"
            subTitle="Image"
            selectedImage={content.specific.featureImageLink}
            selectedFile={featureImage.selectedFile}
            onUploadImage={handleUploadFeatureImage}
          />

          <Card className="content-mt-5" title="Tabbed Position">
            <div className="content-p-5">
              <Select
                value={tabbedPosition}
                onValueChange={value =>
                  handleChangeInput({
                    target: { id: 'tabbedPosition', value },
                  } as any)
                }
              >
                <SelectTrigger disabled={false}>
                  <SelectValue placeholder="Select a Tabbed Position" />
                </SelectTrigger>
                <SelectContent>
                  {TabbedPositionOptions.map(option => (
                    <SelectItem key={option.id} value={option.value}>
                      {option.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          </Card>

          <Card className="content-mt-5" title="Audio">
            <div className="content-px-5 content-pb-5">
              <FileUploader
                ref={audioUploaderRef}
                containerClassName="content-mb-5 content-text-secondary"
                accept={ValidAudioFormats.join(',')}
                placeholder="Upload audio"
                onUpload={handleUploadAudio}
              />
              <Input
                id="audioLink"
                name="audioLink"
                type="text"
                placeholder="Paste audio url"
                value={audio.link}
                onChange={handleChangeInput}
              />
              {!!(audio.link || audio.selectedFile) && (
                <Button
                  className="content-w-fit !content-bg-transparent content-px-0 content-text-stormy-gray content-underline"
                  onClick={() =>
                    setAudio({ link: '', selectedFile: null, objectURL: '' })
                  }
                >
                  Clear Audio
                </Button>
              )}

              {audio.link || audio.objectURL ? (
                <div className="content-mt-4">
                  <audio controls>
                    <source
                      src={audio.objectURL ? audio.objectURL : audio.link}
                      type="audio/mpeg"
                    />
                    Your browser does not support the audio element.
                  </audio>
                </div>
              ) : null}
            </div>
          </Card>
        </TabsContent>
        <TabsContent value={TabNames.TABS}>
          <div className="content-mt-6">
            <div className="content-flex content-gap-2">
              <Select
                value={selectedTabId}
                onValueChange={value => setSelectedTabId(value)}
              >
                <SelectTrigger disabled={false}>
                  <SelectValue placeholder="Select a Tab" />
                </SelectTrigger>
                <SelectContent>
                  {tabs
                    ?.map((tab, index) => ({
                      id: tab.id,
                      label: `Tab ${index + 1}`,
                      value: tab.id,
                    }))
                    .map(option => (
                      <SelectItem key={option.id} value={option.value}>
                        {option.label}
                      </SelectItem>
                    ))}
                </SelectContent>
              </Select>
              <Button onClick={handleAddTab} className="content-w-28" size="sm">
                Add Tab
              </Button>
              {tabs.length > 0 && (
                <Button
                  onClick={toggleConfirmDialog}
                  className="!content-bg-transparent content-p-0"
                >
                  <DeleteIcon />
                </Button>
              )}
            </div>

            {tabs?.length === 0 ? (
              <p className="content-my-4">No tabs added yet</p>
            ) : (
              selectedTabId !== '-1' && (
                <TabSection
                  ref={selectedTabRef}
                  tab={tabs.find(tab => tab.id === selectedTabId)!}
                  updateTabs={updateTabs}
                  errors={tabError}
                  updateErrors={setTabError}
                  audioSeconds={audioSeconds}
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
        message="Are you sure you want to delete this tab?"
        loadingText="Deleting..."
        onClose={toggleConfirmDialog}
        onConfirm={handleDeleteTab}
      />
    </Fragment>
  );
});

TabbedBased.displayName = 'TabbedBased';

export default TabbedBased;
