import {
  ChangeEvent,
  forwardRef,
  SetStateAction,
  useEffect,
  useImperativeHandle,
  useMemo,
  useRef,
  useState,
} from 'react';
import { v4 as uuidv4 } from 'uuid';

import { Button } from 'common/Button';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import { Tabs, TabsContent, TabsList, TabsTrigger } from 'common/Tab';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'components/common/Select';
import ConfirmDialog from 'components/ConfirmDialog';
import InputCard from 'components/InputCard';
import VideoCard from 'components/VideoCard';
import BackgroundSettings from 'features/content/settings/BackgroundSettings';
import InteractiveContentSection from 'features/content/settings/interactive/ContentSection';
import { useAPI } from 'hooks/UseAPI';
import { Trash } from 'lucide-react';
import {
  ContentTypes,
  IBackgroundSettings,
  IContentBlockHandle,
  IContentBlockSettings,
  IInteractiveContent,
  IInteractiveVideoByLanguage,
  IInteractiveVideoContent,
  ProcessingStatus,
} from 'models/Content';
import { IResponse } from 'models/Global';
import { TabNames } from 'models/Tab';
import { toast } from 'react-toastify';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { BackgroundFormattingDefault } from 'utils/ContentDefaultValues';
import { formatContentDataBasedOnType } from 'utils/Formatter';
import { getMediaDuration, isSuccessResponse } from 'utils/Helper';

interface ILanguage {
  id: string;
  code: string;
  displayName: string;
}

const TabOptions = [
  { label: 'Setting', value: TabNames.SETTING },
  { label: 'Content', value: TabNames.CONTENT },
  { label: 'Background', value: TabNames.BACKGROUND },
];

const getPrimaryLanguageVideo = (
  interactiveVideoByLanguage?: Array<IInteractiveVideoByLanguage>,
) => {
  if (!interactiveVideoByLanguage?.length) return null;

  return (
    interactiveVideoByLanguage.find(item => item.default) ||
    interactiveVideoByLanguage[0]
  );
};

const createVideoCard = (
  overrides?: Partial<IInteractiveVideoByLanguage>,
): IInteractiveVideoByLanguage => ({
  id: overrides?.id ?? uuidv4(),
  cardTitle: overrides?.cardTitle ?? 'Video Card',
  contentList: overrides?.contentList ?? [],
  videoUrl: overrides?.videoUrl ?? '',
  videoFile: overrides?.videoFile ?? null,
  videoLength: overrides?.videoLength ?? '',
  isProcessing: overrides?.isProcessing ?? false,
  processingStatus: overrides?.processingStatus ?? ProcessingStatus.QUEUE,
  processVideoUrl: overrides?.processVideoUrl ?? '',
  language: overrides?.language ?? '',
  default: overrides?.default ?? false,
});

const InteractiveVideoBased = forwardRef<
  IContentBlockHandle,
  IContentBlockSettings<IInteractiveVideoContent>
>(({ content, uploadVideoProgress }, ref) => {
  const contentSectionRef = useRef<IContentBlockHandle | null>(null);

  const [activeTab, setActiveTab] = useState<TabNames>(TabNames.SETTING);

  const [contentName, setContentName] = useState<{
    text: string;
    error: string;
  }>({
    text: '',
    error: '',
  });
  const [videoInfo, setVideoInfo] = useState<{
    videoUrl: string;
    videoLength: string;
  }>({
    videoUrl: '',
    videoLength: '',
  });
  const [interactiveVideoByLanguage, setInteractiveVideoByLanguage] = useState<
    Array<IInteractiveVideoByLanguage>
  >([]);
  const [selectedVideoCardId, setSelectedVideoCardId] = useState<string>('');
  const [openConfirmDialog, setOpenConfirmDialog] = useState<boolean>(false);
  const [selectedContentId, setSelectedContentId] = useState<string>('');
  const [languageOptions, setLanguageOptions] = useState<Array<ILanguage>>([]);

  const firstVideoCardId = interactiveVideoByLanguage[0]?.id;

  const selectedVideoCard = useMemo(
    () =>
      interactiveVideoByLanguage.find(
        item => item.id === selectedVideoCardId,
      ) || interactiveVideoByLanguage[0],
    [interactiveVideoByLanguage, selectedVideoCardId],
  );

  const isFirstVideoCardSelected = selectedVideoCard?.id === firstVideoCardId;

  const defaultSubContents = useMemo(
    () =>
      (interactiveVideoByLanguage[0]?.contentList ?? []).filter(
        content => content.isDefault,
      ),
    [interactiveVideoByLanguage],
  );

  const editableContents = useMemo(() => {
    const currentContents = selectedVideoCard?.contentList ?? [];

    if (isFirstVideoCardSelected) {
      return currentContents;
    }

    return currentContents.filter(content => !content.isDefault);
  }, [selectedVideoCard, isFirstVideoCardSelected]);

  const [backgroundSettings, setBackgroundSettings] =
    useState<IBackgroundSettings>({
      ...BackgroundFormattingDefault.backgroundFormatting,
      selectedFile: null,
    });

  const setSelectedVideoCardContents = (
    updater: SetStateAction<Array<IInteractiveContent>>,
  ) => {
    if (!selectedVideoCard) return;

    setInteractiveVideoByLanguage(prev =>
      prev.map(item => {
        if (item.id !== selectedVideoCard.id) return item;

        const currentContents = item.contentList ?? [];

        if (isFirstVideoCardSelected) {
          const nextContents =
            typeof updater === 'function' ? updater(currentContents) : updater;

          return {
            ...item,
            contentList: nextContents,
          };
        }

        const defaultContents = currentContents.filter(
          content => content.isDefault,
        );
        const nonDefaultContents = currentContents.filter(
          content => !content.isDefault,
        );
        const nextNonDefaultContents =
          typeof updater === 'function' ? updater(nonDefaultContents) : updater;

        return {
          ...item,
          contentList: [...defaultContents, ...nextNonDefaultContents],
        };
      }),
    );
  };

  const handleSelectDefaultSubContent = (
    content: IInteractiveContent,
    checked: boolean,
  ) => {
    if (!selectedVideoCard || isFirstVideoCardSelected) return;

    setInteractiveVideoByLanguage(prev =>
      prev.map(item => {
        if (item.id !== selectedVideoCard.id) return item;

        const exists = item.contentList.some(
          cardContent => cardContent.id === content.id && cardContent.isDefault,
        );

        if (checked && !exists) {
          return {
            ...item,
            contentList: [
              ...item.contentList,
              {
                ...content,
                contentBody: structuredClone(content.contentBody),
              },
            ],
          };
        }

        if (!checked && exists) {
          return {
            ...item,
            contentList: item.contentList.filter(
              cardContent =>
                !(cardContent.id === content.id && cardContent.isDefault),
            ),
          };
        }

        return item;
      }),
    );
  };

  const handleDefaultSubContentTimeChange = (id: string, time: string) => {
    if (!selectedVideoCard || isFirstVideoCardSelected) return;

    setInteractiveVideoByLanguage(prev =>
      prev.map(item => {
        if (item.id !== selectedVideoCard.id) return item;

        return {
          ...item,
          contentList: item.contentList.map(content =>
            content.id === id && content.isDefault
              ? { ...content, time }
              : content,
          ),
        };
      }),
    );
  };

  const getVideoCardsWithSyncedSelectedContent = () => {
    if (!contentSectionRef?.current || !selectedVideoCard) {
      return { success: true, updatedVideoCards: interactiveVideoByLanguage };
    }

    const payload = contentSectionRef.current.validateAndGetData();

    if (!payload.success) {
      return { success: false, updatedVideoCards: interactiveVideoByLanguage };
    }

    if (!payload.data) {
      return { success: true, updatedVideoCards: interactiveVideoByLanguage };
    }

    const updatedVideoCards = interactiveVideoByLanguage.map(video => {
      if (video.id !== selectedVideoCard.id) return video;

      return {
        ...video,
        contentList: (video.contentList ?? []).map(content =>
          content.id === payload.data!.id
            ? {
                ...content,
                contentBody: { ...content.contentBody, ...payload.data! },
              }
            : content,
        ),
      };
    });

    return { success: true, updatedVideoCards };
  };

  const apiClient = useAPI();

  useEffect(() => {
    const initializeData = async () => {
      try {
        const response: IResponse<Array<ILanguage>> = await apiClient.get(
          API_END_POINTS.GET_ACTIVE_LANGUAGE_LIST,
        );

        if (!isSuccessResponse(response.statusCode)) {
          throw new Error(response.message || 'Failed to fetch languages');
        }

        setLanguageOptions(response.data ?? []);
      } catch (error) {
        toast.error((error as Error).message);
      }

      if (content?.specific) {
        setContentName({
          text: content.common.contentName,
          error: '',
        });

        if (content.specific.interactiveVideoByLanguage?.length) {
          const cards = content.specific.interactiveVideoByLanguage.map(item =>
            createVideoCard({
              ...item,
              contentList:
                item.contentList?.map(nestedContent => ({
                  id: nestedContent.contentBody.id,
                  contentType: nestedContent.contentType,
                  contentBody: nestedContent.contentBody,
                  time: nestedContent.time,
                  canSkip: nestedContent.canSkip,
                  isDefault: nestedContent.isDefault,
                })) ?? [],
            }),
          );

          const primaryCard = getPrimaryLanguageVideo(cards) || cards[0];
          setInteractiveVideoByLanguage(cards);

          setSelectedVideoCardId(primaryCard?.id ?? '');
          setVideoInfo({
            videoUrl: primaryCard?.videoUrl ?? '',
            videoLength: primaryCard?.videoLength ?? '',
          });
          setSelectedContentId(primaryCard?.contentList?.[0]?.id ?? '');
        } else {
          setInteractiveVideoByLanguage([]);
          setSelectedVideoCardId('');
          setVideoInfo({ videoUrl: '', videoLength: '' });
          setSelectedContentId('');
        }

        setBackgroundSettings(prev => ({
          ...prev,
          ...content.specific.backgroundFormatting,
          backgroundOpacity:
            content.specific.backgroundFormatting.backgroundOpacity + '',
        }));
      }
    };

    initializeData();
  }, [content, apiClient]);

  const handleChangeInput = (e: ChangeEvent<HTMLInputElement>) => {
    const { value } = e.target;

    setContentName({
      text: value,
      error: value.trim().length === 0 ? 'Content name is required' : '',
    });
  };

  const handleUploadFile = async (uploadedFile: File | null) => {
    if (!selectedVideoCard) {
      return;
    }

    if (uploadedFile) {
      const duration = await getMediaDuration(uploadedFile, 'video');

      setInteractiveVideoByLanguage(prev =>
        prev.map(item =>
          item.id === selectedVideoCard.id
            ? {
                ...item,
                videoFile: uploadedFile,
                videoLength: String(duration),
                isProcessing: true,
                processingStatus: ProcessingStatus.QUEUE,
              }
            : item,
        ),
      );

      setVideoInfo(prev => ({ ...prev, videoLength: String(duration) }));
      return;
    }

    setInteractiveVideoByLanguage(prev =>
      prev.map(item =>
        item.id === selectedVideoCard.id
          ? {
              ...item,
              videoFile: null,
              videoUrl: '',
              videoLength: '',
            }
          : item,
      ),
    );

    setVideoInfo({ videoUrl: '', videoLength: '' });
  };

  const handleAddVideoCard = () => {
    const newCard = createVideoCard({
      cardTitle: `Video Card ${interactiveVideoByLanguage.length + 1}`,
      contentList: [],
    });

    setInteractiveVideoByLanguage(prev => [...prev, newCard]);
    setSelectedVideoCardId(newCard.id);
    setVideoInfo({ videoUrl: '', videoLength: '' });
    setSelectedContentId('');
  };

  const handleDeleteVideoCard = () => {
    setInteractiveVideoByLanguage(prev => {
      if (prev.length <= 1) {
        return prev;
      }

      const removeId = selectedVideoCardId || prev[0].id;
      const updatedCards = prev.filter(item => item.id !== removeId);
      const fallbackCard = updatedCards[0];

      setSelectedVideoCardId(fallbackCard?.id ?? '');
      setVideoInfo({
        videoUrl: fallbackCard?.videoUrl ?? '',
        videoLength: fallbackCard?.videoLength ?? '',
      });
      setSelectedContentId(
        fallbackCard?.contentList?.find(item => !item.isDefault)?.id ?? '',
      );

      return updatedCards;
    });
  };

  const toggleConfirmDialog = () => setOpenConfirmDialog(prevOpen => !prevOpen);

  const handleVideoCardSelection = (id: string) => {
    setSelectedVideoCardId(id);

    const selected = interactiveVideoByLanguage.find(item => item.id === id);

    setVideoInfo({
      videoUrl: selected?.videoUrl ?? '',
      videoLength: selected?.videoLength ?? '',
    });
    setSelectedContentId(
      selected?.contentList?.find(item => !item.isDefault)?.id ?? '',
    );
  };

  const handleLanguageChange = (language: string) => {
    if (!selectedVideoCard) return;

    setInteractiveVideoByLanguage(prev =>
      prev.map(item =>
        item.id === selectedVideoCard.id
          ? {
              ...item,
              language,
            }
          : item,
      ),
    );
  };

  const handleCardTitleChange = (e: ChangeEvent<HTMLInputElement>) => {
    if (!selectedVideoCard) return;

    setInteractiveVideoByLanguage(prev =>
      prev.map(item =>
        item.id === selectedVideoCard.id
          ? {
              ...item,
              cardTitle: e.target.value,
            }
          : item,
      ),
    );
  };

  const handleTabChange = (activeTab: TabNames) => {
    if (!validateFields()) return;

    const { success, updatedVideoCards } =
      getVideoCardsWithSyncedSelectedContent();

    if (!success) return;

    setInteractiveVideoByLanguage(updatedVideoCards);

    setActiveTab(activeTab);
  };

  useEffect(() => {
    const selectedCardContents = editableContents;

    if (selectedCardContents.length === 0) {
      if (selectedContentId) setSelectedContentId('');
      return;
    }

    const hasSelectedContent = selectedCardContents.some(
      content => content.id === selectedContentId,
    );

    if (!hasSelectedContent) {
      setSelectedContentId(selectedCardContents[0].id ?? '');
    }
  }, [editableContents, selectedContentId]);

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

  useImperativeHandle(ref, () => ({
    validateAndGetData: () => {
      if (!validateFields()) {
        return { success: false };
      }

      if (!contentSectionRef?.current) {
        return {
          success: true,
          data: formatContentDataBasedOnType(
            {
              id: content.id!,
              contentName: contentName.text,
              updateContent: true,
              backgroundSettings,
              interactiveVideoContent: {
                interactiveVideoByLanguage,
              },
            },
            ContentTypes.INTERACTIVE_VIDEO,
          ),
        };
      }

      const { success, updatedVideoCards } =
        getVideoCardsWithSyncedSelectedContent();

      if (!success) {
        return { success: false };
      }
      setInteractiveVideoByLanguage(updatedVideoCards);

      return {
        success: true,
        data: formatContentDataBasedOnType(
          {
            id: content.id!,
            contentName: contentName.text,
            updateContent: true,
            backgroundSettings,
            interactiveVideoContent: {
              interactiveVideoByLanguage: updatedVideoCards,
            },
          },
          ContentTypes.INTERACTIVE_VIDEO,
        ),
      };
    },
  }));

  return (
    <Tabs
      value={activeTab}
      onValueChange={activeTab => {
        handleTabChange(activeTab as TabNames);
      }}
      className="content-w-full"
    >
      <TabsList className="content-w-full">
        {TabOptions.map(item => (
          <TabsTrigger
            key={item.value}
            value={item.value}
            className="content-w-full"
          >
            {item.label}
          </TabsTrigger>
        ))}
      </TabsList>

      <TabsContent value={TabNames.SETTING}>
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

        <div className="content-my-5">
          <div className="content-mb-3 content-flex content-items-center content-justify-between content-gap-3">
            <div className="content-flex-1">
              <Select
                value={selectedVideoCardId}
                onValueChange={handleVideoCardSelection}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Select video card" />
                </SelectTrigger>
                <SelectContent>
                  {interactiveVideoByLanguage.map((item, index) => (
                    <SelectItem key={item.id} value={item.id}>
                      {item.cardTitle ||
                        `Video Card ${String(index + 1).padStart(2, '0')}`}
                    </SelectItem>
                  ))}
                  {interactiveVideoByLanguage.length === 0 && (
                    <p className="content-p-2 content-text-center content-text-sm content-text-gray-400">
                      No videos added yet
                    </p>
                  )}
                </SelectContent>
              </Select>
            </div>
            <div className="content-flex content-items-center content-gap-2">
              <Button type="button" onClick={handleAddVideoCard}>
                Add Video Card
              </Button>
              {interactiveVideoByLanguage.length > 1 && (
                <Button
                  type="button"
                  variant="ghost"
                  onClick={toggleConfirmDialog}
                >
                  <Trash className="content-text-red-500" />
                </Button>
              )}
            </div>
          </div>

          {selectedVideoCard && (
            <div className="content-flex content-flex-col content-gap-y-3">
              <div className="content-space-y-1">
                <Label>Card Title</Label>
                <Input
                  value={selectedVideoCard?.cardTitle || ''}
                  placeholder="Enter card title"
                  onChange={handleCardTitleChange}
                />
              </div>
              <div className="content-mb-2 content-space-y-1">
                <Label>Language</Label>
                <Select
                  value={selectedVideoCard?.language}
                  onValueChange={handleLanguageChange}
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Select language" />
                  </SelectTrigger>
                  <SelectContent>
                    {languageOptions.map(option => (
                      <SelectItem key={option.id} value={option.displayName}>
                        {option.displayName}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>

              <VideoCard
                title="Video with Language"
                subTitle="Upload your video file below"
                selectedVideo={selectedVideoCard?.videoUrl}
                selectedFile={selectedVideoCard?.videoFile}
                onUploadVideo={handleUploadFile}
                progress={uploadVideoProgress}
              ></VideoCard>
            </div>
          )}
        </div>
      </TabsContent>
      <TabsContent value={TabNames.CONTENT}>
        <InteractiveContentSection
          ref={contentSectionRef}
          type={ContentTypes.INTERACTIVE_VIDEO}
          videoLength={videoInfo.videoLength}
          contents={editableContents}
          setContents={setSelectedVideoCardContents}
          selectedContentId={selectedContentId}
          setSelectedContentId={setSelectedContentId}
          showDefaultSubContentsList={
            !isFirstVideoCardSelected && defaultSubContents.length > 0
          }
          defaultSubContents={defaultSubContents}
          selectedDefaultSubContents={selectedVideoCard?.contentList ?? []}
          onToggleDefaultSubContent={handleSelectDefaultSubContent}
          onChangeDefaultSubContentTime={handleDefaultSubContentTimeChange}
          showIsDefault={
            !!selectedVideoCard &&
            selectedVideoCard.id === interactiveVideoByLanguage[0]?.id
          }
        />
      </TabsContent>
      <TabsContent value={TabNames.BACKGROUND}>
        <BackgroundSettings
          backgroundSettings={backgroundSettings}
          updateBackgroundSettings={setBackgroundSettings}
        />
      </TabsContent>

      <ConfirmDialog
        isOpen={openConfirmDialog}
        message="Are you sure you want to delete this video card?"
        loadingText="Deleting..."
        onClose={toggleConfirmDialog}
        onConfirm={() => {
          handleDeleteVideoCard();
          toggleConfirmDialog();
        }}
      />
    </Tabs>
  );
});

InteractiveVideoBased.displayName = 'InteractiveVideoBased';

export default InteractiveVideoBased;
