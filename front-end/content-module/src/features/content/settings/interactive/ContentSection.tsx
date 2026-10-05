import {
  ChangeEvent,
  Dispatch,
  forwardRef,
  Fragment,
  SetStateAction,
  useImperativeHandle,
  useRef,
  useState,
} from 'react';
import { v4 as uuidv4 } from 'uuid';

import { CloseIcon } from 'assets/icons';
import clsx from 'clsx';
import { Button } from 'common/Button';
import { Checkbox } from 'common/Checkbox';
import { Input } from 'common/Input';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import ConfirmDialog from 'components/ConfirmDialog';
import LinkBased from 'features/content/settings/LinkBased';
import MarkdownBased from 'features/content/settings/MarkdownBased';
import PdfBased from 'features/content/settings/PdfBased';
import QuestionBased from 'features/content/settings/QuestionBased';
import QuizBased from 'features/content/settings/quiz/QuizBased';
import SliderLevelBased from 'features/content/settings/slider-level/SliderLevelBased';
import SliderShowBased from 'features/content/settings/slider-show/SliderShowBased';
import StoryBlockBased from 'features/content/settings/story-block/StoryBlockBased';
import TabbedBased from 'features/content/settings/tabbed/TabbedBased';
import TextBased from 'features/content/settings/TextBased';
import VideoBased from 'features/content/settings/VideoBased';
import { Trash } from 'lucide-react';
import {
  ContentStatus,
  ContentTypes,
  IContent,
  IContentBlockHandle,
  IInteractiveContent,
  ILinkContent,
  IMarkdownContent,
  IPdfContent,
  IQuestionContent,
  IQuizContent,
  ISliderLevelContent,
  ISliderShowContent,
  IStoryBlockContent,
  ITabbedContent,
  ITextContent,
  IVideoContent,
  TContent,
} from 'models/Content';
import { GetContentDefaultValue } from 'utils/ContentDefaultValues';
import { timeToSeconds } from 'utils/Helper';

interface IProps {
  type: ContentTypes;
  videoLength?: string;
  contents: Array<IInteractiveContent>;
  setContents: Dispatch<SetStateAction<Array<IInteractiveContent>>>;
  selectedContentId: string;
  setSelectedContentId: Dispatch<SetStateAction<string>>;
  showIsDefault?: boolean;
  showDefaultSubContentsList?: boolean;
  defaultSubContents?: Array<IInteractiveContent>;
  selectedDefaultSubContents?: Array<IInteractiveContent>;
  onToggleDefaultSubContent?: (
    content: IInteractiveContent,
    checked: boolean,
  ) => void;
  onChangeDefaultSubContentTime?: (id: string, time: string) => void;
}

const InteractiveContentSection = forwardRef<IContentBlockHandle, IProps>(
  (
    {
      type,
      videoLength,
      contents,
      setContents,
      selectedContentId,
      setSelectedContentId,
      showIsDefault = false,
      showDefaultSubContentsList = false,
      defaultSubContents = [],
      selectedDefaultSubContents = [],
      onToggleDefaultSubContent,
      onChangeDefaultSubContentTime,
    },
    ref,
  ) => {
    const selectedContentRef = useRef<IContentBlockHandle | null>(null);

    const [error, setError] = useState<string>('');
    const [showModal, setShowModal] = useState<boolean>(false);
    const [openConfirmDialog, setOpenConfirmDialog] = useState<boolean>(false);
    const [newContent, setNewContent] = useState<{
      title: string;
      type: ContentTypes;
    }>({ title: '', type: ContentTypes.TEXT });

    const excludedTypes = [
      ContentTypes.INTERACTIVE_VIDEO,
      ContentTypes.INTERACTIVE_CONTENT,
      ContentTypes.QUESTION,
      ContentTypes.NONE,
    ];

    if (type === ContentTypes.INTERACTIVE_VIDEO) {
      excludedTypes.push(ContentTypes.VIDEO, ContentTypes.ANIMATION);
    }

    const selectedContent = contents?.find(
      content => content.id === selectedContentId,
    );

    const contentList =
      contents?.map(content => ({
        id: content.id as string,
        label: content.contentBody.common.contentName,
        value: content.id as string,
      })) ?? [];

    const contentTypes = Object.values(ContentTypes)
      .filter(item => !excludedTypes.includes(item))
      .map(type => ({
        id: type,
        label: type.replace('_', ' ').toLowerCase(),
        value: type,
      }));

    const handleAddContent = () => {
      if (!getSelectedContentData()) return;

      const newContentId = uuidv4();
      const newdata = {
        id: newContentId,
        contentType: newContent.type,
        contentBody: {
          id: newContentId,
          common: {
            contentName: newContent.title || 'New Content',
            contentType: newContent.type,
            status: ContentStatus.DRAFT,
            chapterIds: [],
            tags: [],
          },
          specific: GetContentDefaultValue(
            newContent.type,
          ) as unknown as TContent,
        },
        time: '00:00:00',
        canSkip: false,
        isDefault: false,
      };

      setContents(prev => [...prev, newdata]);
      setSelectedContentId(newdata.id);
      handleToggleModal();
    };

    const handleDeleteContent = () => {
      const updatedContents = contents.filter(
        content => content.id !== selectedContentId,
      );
      setContents([...updatedContents]);
      setSelectedContentId(updatedContents[0]?.id ?? '');
      toggleConfirmDialog();
    };

    const handleChangeInput = (e: ChangeEvent<HTMLInputElement>) => {
      const { id, value } = e.target,
        checked = (e.target as HTMLInputElement).checked;

      if (['time', 'canSkip', 'isDefault'].includes(id)) {
        setContents(prev =>
          prev.map(content => {
            if (content.id === selectedContentId) {
              return {
                ...content,
                [id]: id === 'time' ? value : checked,
              };
            }
            return content;
          }),
        );

        if (id === 'time') {
          validateTime(value);
        }
      }
    };

    const handleChangeSelectedContent = (value: string) => {
      if (!getSelectedContentData()) return;

      setSelectedContentId(contents?.find(item => item.id === value)?.id ?? '');
    };

    const validateTime = (value: string) => {
      const timeInSeconds = timeToSeconds(value);

      if (
        type === ContentTypes.INTERACTIVE_VIDEO &&
        timeInSeconds > Number(videoLength)
      ) {
        setError('Time cannot be greater than the video length');
        return false;
      }

      if (
        type === ContentTypes.INTERACTIVE_VIDEO &&
        contents
          .filter(content => content.id !== selectedContent?.id)
          .map(content => content.time)
          .includes(value)
      ) {
        setError('This time is already set for another content');
        return false;
      }

      if (type === ContentTypes.INTERACTIVE_CONTENT && timeInSeconds === 0) {
        setError('Time cannot be 0');
        return false;
      }

      setError('');
      return true;
    };

    const getSelectedContentData = () => {
      if (!selectedContentRef?.current) return true;

      if (!validateTime(selectedContent?.time as string)) {
        return false;
      }

      const payload = selectedContentRef?.current.validateAndGetData();
      if (!payload.success) return false;

      const { contentName, ...rest } = payload.data!;

      setContents(prev =>
        prev.map(content => {
          if (content.id === payload.data!.id) {
            return {
              ...content,
              contentBody: {
                ...content.contentBody,
                ...rest,
                common: { ...content.contentBody.common, contentName },
              },
            };
          }
          return content;
        }),
      );

      return true;
    };

    const changeContentName = (e: ChangeEvent<HTMLInputElement>) => {
      const contentName = e.target.value;

      const updatedContents = contents.map(content => {
        if (content.id === selectedContentId) {
          return {
            ...content,
            contentBody: {
              ...content.contentBody,
              common: {
                ...content.contentBody.common,
                contentName,
              },
            },
          };
        }
        return content;
      });

      setContents(updatedContents);
    };

    const changeContentType = (value: ContentTypes) => {
      const contentType = value;

      const updatedContents = contents.map(content => {
        if (content.id === selectedContentId) {
          return {
            ...content,
            contentType,
            contentBody: {
              ...content.contentBody,
              common: {
                ...content.contentBody.common,
                contentType,
              },
              specific: GetContentDefaultValue(
                contentType,
              ) as unknown as TContent,
            },
          };
        }
        return content;
      });

      setContents(updatedContents);
    };

    const handleToggleModal = () => {
      setShowModal(!showModal);
      setNewContent({ title: '', type: ContentTypes.TEXT });
    };

    const renderSettingsComponent = () => {
      if (!selectedContent) return null;

      switch (selectedContent.contentType) {
        case ContentTypes.TEXT:
          return (
            <TextBased
              ref={selectedContentRef}
              isPartofInteractiveContent={true}
              content={selectedContent.contentBody as IContent<ITextContent>}
            />
          );
        case ContentTypes.LINK:
          return (
            <LinkBased
              ref={selectedContentRef}
              isPartofInteractiveContent={true}
              content={selectedContent.contentBody as IContent<ILinkContent>}
            />
          );
        case ContentTypes.PDF:
          return (
            <PdfBased
              ref={selectedContentRef}
              isPartofInteractiveContent={true}
              content={selectedContent.contentBody as IContent<IPdfContent>}
            />
          );
        case ContentTypes.QUESTION:
          return (
            <QuestionBased
              ref={selectedContentRef}
              isPartofInteractiveContent={true}
              content={
                selectedContent.contentBody as IContent<IQuestionContent>
              }
            />
          );
        case ContentTypes.MARKDOWN:
          return (
            <MarkdownBased
              ref={selectedContentRef}
              isPartofInteractiveContent={true}
              content={
                selectedContent.contentBody as IContent<IMarkdownContent>
              }
            />
          );
        case ContentTypes.SLIDER_SHOW:
          return (
            <SliderShowBased
              ref={selectedContentRef}
              isPartofInteractiveContent={true}
              content={
                selectedContent.contentBody as IContent<ISliderShowContent>
              }
            />
          );
        case ContentTypes.STORY_BLOCK:
          return (
            <StoryBlockBased
              ref={selectedContentRef}
              isPartofInteractiveContent={true}
              content={
                selectedContent.contentBody as IContent<IStoryBlockContent>
              }
            />
          );
        case ContentTypes.SLIDER_LEVEL:
          return (
            <SliderLevelBased
              ref={selectedContentRef}
              isPartofInteractiveContent={true}
              content={
                selectedContent.contentBody as IContent<ISliderLevelContent>
              }
            />
          );
        case ContentTypes.VIDEO:
        case ContentTypes.ANIMATION:
          return (
            <VideoBased
              ref={selectedContentRef}
              isPartofInteractiveContent={true}
              content={selectedContent.contentBody as IContent<IVideoContent>}
            />
          );
        case ContentTypes.TABBED:
          return (
            <TabbedBased
              ref={selectedContentRef}
              isPartofInteractiveContent={true}
              content={selectedContent.contentBody as IContent<ITabbedContent>}
            />
          );
        case ContentTypes.QUIZ:
          return (
            <QuizBased
              ref={selectedContentRef}
              isPartofInteractiveContent={true}
              content={selectedContent.contentBody as IContent<IQuizContent>}
            />
          );
        default:
          return null;
      }
    };

    const toggleConfirmDialog = () => setOpenConfirmDialog(prev => !prev);

    useImperativeHandle(ref, () => ({
      validateAndGetData: () => {
        if (!selectedContentRef?.current) return { success: true };

        if (!validateTime(selectedContent?.time as string)) {
          return { success: false };
        }

        const blockData = selectedContentRef.current?.validateAndGetData();

        if (!blockData.success) {
          return { success: false };
        }

        return {
          success: true,
          data: {
            ...blockData.data!,
          },
        };
      },
    }));

    return (
      <Fragment>
        {showDefaultSubContentsList && (
          <div className="content-mb-4 content-rounded content-border content-p-3">
            <p className="content-mb-3 content-font-medium">Default Contents</p>
            <div className="content-space-y-2">
              {defaultSubContents.map(defaultContent => {
                const selectedDefaultContent = selectedDefaultSubContents.find(
                  content =>
                    content.id === defaultContent.id && content.isDefault,
                );
                const checked = !!selectedDefaultContent;

                return (
                  <div
                    key={defaultContent.id}
                    className="content-grid content-grid-cols-[auto_1fr_140px] content-items-center content-gap-3"
                  >
                    <Checkbox
                      checked={checked}
                      onCheckedChange={(checked: boolean) =>
                        onToggleDefaultSubContent?.(defaultContent, checked)
                      }
                    />
                    <span>{defaultContent.contentBody.common.contentName}</span>
                    <Input
                      value={
                        selectedDefaultContent?.time ?? defaultContent.time
                      }
                      placeholder="HH:MM:SS"
                      disabled={!checked}
                      onChange={e =>
                        onChangeDefaultSubContentTime?.(
                          defaultContent.id as string,
                          e.target.value,
                        )
                      }
                    />
                  </div>
                );
              })}
            </div>
          </div>
        )}
        <div className="content-relative content-mt-6 content-flex content-gap-2">
          <Select
            value={selectedContentId}
            onValueChange={value => handleChangeSelectedContent(value)}
          >
            <SelectTrigger disabled={false}>
              <SelectValue placeholder="Select Content" />
            </SelectTrigger>
            <SelectContent>
              {contentList.map(option => (
                <SelectItem key={option.id} value={option.value}>
                  {option.label}
                </SelectItem>
              ))}
              {contentList.length === 0 && (
                <p className="content-p-2 content-text-center content-text-sm content-text-gray-400">
                  No content added yet
                </p>
              )}
            </SelectContent>
          </Select>
          <Button
            onClick={handleToggleModal}
            className="content-w-40"
            size="sm"
          >
            Add Content
          </Button>
          {contents?.length > 0 && (
            <Button type="button" variant="ghost" onClick={toggleConfirmDialog}>
              <Trash className="content-text-red-500" />
            </Button>
          )}

          {showModal && (
            <div className="content-absolute content-left-10 content-right-0 content-top-12 content-z-10 content-rounded content-border">
              <div className="content-rounded content-bg-dark-blue content-p-5 content-drop-shadow-xl">
                <div className="content-flex content-items-center content-justify-between">
                  <p className="content-text-white">Select Content Type</p>
                  <Button
                    className="!content-bg-transparent content-p-0 content-text-white"
                    onClick={handleToggleModal}
                  >
                    <CloseIcon className="content-size-5" />
                  </Button>
                </div>
                <div className="content-mt-4 content-space-y-4">
                  <Input
                    id="content-title"
                    name="content-title"
                    value={newContent.title}
                    onChange={e =>
                      setNewContent({ ...newContent, title: e.target.value })
                    }
                    placeholder="Enter Content Title"
                    className="content-capitalize"
                  />
                  <Select
                    value={newContent.type}
                    onValueChange={value =>
                      setNewContent({
                        ...newContent,
                        type: value as ContentTypes,
                      })
                    }
                  >
                    <SelectTrigger disabled={false}>
                      <SelectValue placeholder="Select Content Type" />
                    </SelectTrigger>
                    <SelectContent>
                      {contentTypes.map(option => (
                        <SelectItem key={option.id} value={option.value}>
                          {option.label}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>

                  <Button
                    className="content-ml-auto content-px-6 content-py-1.5"
                    onClick={handleAddContent}
                  >
                    Add
                  </Button>
                </div>
              </div>
            </div>
          )}
        </div>

        {selectedContent && (
          <Fragment>
            <div className="content-mt-5 content-space-y-2">
              <p>Change Content Name</p>
              <Input
                id="modify-content-name"
                name="modify-content-name"
                value={selectedContent.contentBody.common.contentName}
                onChange={changeContentName}
                placeholder="Enter Content Name"
              />

              <p>Change Content Type</p>
              <Select
                value={selectedContent.contentType}
                onValueChange={value =>
                  changeContentType(value as ContentTypes)
                }
              >
                <SelectTrigger disabled={false}>
                  <SelectValue placeholder="Select Content Type" />
                </SelectTrigger>
                <SelectContent>
                  {contentTypes.map(option => (
                    <SelectItem key={option.id} value={option.value}>
                      {option.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>

              <div className="content-flex content-items-end content-justify-between">
                <div className="content-w-8/12">
                  <p className="content-my-2">Set Time</p>
                  <Input
                    id="time"
                    name="time"
                    value={selectedContent.time}
                    placeholder="Enter Time"
                    onChange={handleChangeInput}
                    className={clsx({ 'content-has-error': !!error.length })}
                  />
                  {error && (
                    <p className="content-mt-1 content-text-sm content-text-red-500">
                      {error}
                    </p>
                  )}
                </div>
                <div className="content-flex content-flex-col content-items-baseline">
                  <div className="content-flex content-items-center content-justify-between">
                    <Checkbox
                      id="canSkip"
                      checked={!!selectedContent.canSkip}
                      onCheckedChange={checked =>
                        handleChangeInput({
                          target: {
                            id: 'canSkip',
                            checked: checked,
                          },
                        } as any)
                      }
                    />
                    <label htmlFor="canSkip" className="content-ml-2">
                      Can Skip
                    </label>
                  </div>
                  {showIsDefault && (
                    <div className="content-flex content-items-center content-justify-between">
                      <Checkbox
                        id="isDefault"
                        checked={!!selectedContent.isDefault}
                        onCheckedChange={checked =>
                          handleChangeInput({
                            target: {
                              id: 'isDefault',
                              checked: checked,
                            },
                          } as any)
                        }
                      />
                      <label htmlFor="isDefault" className="content-ml-2">
                        Default
                      </label>
                    </div>
                  )}
                </div>
              </div>
            </div>

            <div className="content-mt-5 content-space-y-5">
              {renderSettingsComponent()}
            </div>
          </Fragment>
        )}

        <ConfirmDialog
          isOpen={openConfirmDialog}
          message="Are you sure you want to delete this content?"
          loadingText="Deleting..."
          onClose={toggleConfirmDialog}
          onConfirm={handleDeleteContent}
        />
      </Fragment>
    );
  },
);
InteractiveContentSection.displayName = 'InteractiveContentSection';

export default InteractiveContentSection;
