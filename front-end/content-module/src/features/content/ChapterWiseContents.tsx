import { Fragment, useCallback, useEffect, useRef, useState } from 'react';
import { toast } from 'react-toastify';

import Loader from 'common/loader/Loader';
import ConfirmDialog from 'components/ConfirmDialog';
import ContentSettingsModal from 'components/ContentSettingsModal';
import ContentBlocks from 'features/content/ContentBlocks';
import ContentsIndex from 'features/content/ContentsIndex';
import ContentTypeSection from 'features/content/ContentTypeSection';
import InteractiveContentBased from 'features/content/settings/interactive/InteractiveContentBased';
import InteractiveVideoBased from 'features/content/settings/interactive/InteractiveVideoBased';
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
import { useAPI } from 'hooks/UseAPI';
import { useUploader } from 'hooks/UseUploader';
import { IChapter } from 'models/Chapter';
import {
  ContentStatus,
  ContentTypes,
  IContent,
  IContentBlockHandle,
  IInteractiveContent,
  IInteractiveGeneralContent,
  IInteractiveVideoContent,
  ILinkContent,
  IMarkdownContent,
  IntervalType,
  IPdfContent,
  IQuestionContent,
  IQuizContent,
  ISliderLevelContent,
  ISliderShowContent,
  IStoryBlockContent,
  ITabbedContent,
  ITextContent,
  IVideoContent,
  ProcessingStatus,
  TContent,
} from 'models/Content';
import { IResponse } from 'models/Global';
import { MdContentPasteOff } from 'react-icons/md';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { GetContentDefaultValue } from 'utils/ContentDefaultValues';
import { isSuccessResponse } from 'utils/Helper';

interface IProps {
  chapterId: string;
}

type normalizeContentType = {
  contentList: IInteractiveContent | Array<IInteractiveContent>;
};

export const formatContentArray = (input: Array<IInteractiveContent>) => {
  return input.map(item => ({
    ...item,
    contentBody: {
      id: item.contentBody.id,
      common: (item.contentBody as any).commonContent,
      specific: (item.contentBody as any).specificContent,
    },
  }));
};

const ChapterWiseContents = ({ chapterId }: IProps) => {
  const sectionRefs = useRef<Array<HTMLDivElement>>([]);
  const selectedContentRef = useRef<IContentBlockHandle>(null);
  const intervalRef = useRef<NodeJS.Timeout | null>(null);

  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [contents, setContents] = useState<Array<IContent<TContent>>>([]);
  const [openConfirmDialog, setOpenConfirmDialog] = useState<boolean>(false);

  const [submitting, setSubmitting] = useState<boolean>(false);
  const [selectedContent, setSelectedContent] =
    useState<IContent<TContent> | null>(null);
  const [activeVideoPollingIds, setActiveVideoPollingIds] = useState<
    Set<string>
  >(new Set());

  const apiClient = useAPI();
  const { uploadFile } = useUploader();
  const { uploadFile: uploadVideoFile, progress: uploadVideoProgress } =
    useUploader();

  const fetchData = useCallback(async () => {
    try {
      const response: IResponse<IChapter> = await apiClient.get(
        API_END_POINTS.CHAPTER_DETAILS + chapterId,
      );

      if (!isSuccessResponse(response.statusCode)) {
        throw Error(response.message);
      }

      const updatedContents = response.data.contentIds.map(content => {
        if (
          content.common.contentType === ContentTypes.INTERACTIVE_VIDEO ||
          content.common.contentType === ContentTypes.INTERACTIVE_CONTENT
        ) {
          if (content.common.contentType === ContentTypes.INTERACTIVE_VIDEO) {
            const interactiveVideoContent =
              content.specific as IInteractiveVideoContent;

            interactiveVideoContent.interactiveVideoByLanguage =
              interactiveVideoContent.interactiveVideoByLanguage.map(
                videoByLanguage => {
                  const normalizedLanguageContentList = normalizeContentList(
                    (
                      videoByLanguage as {
                        contentList?:
                          | Array<IInteractiveContent>
                          | normalizeContentType;
                      }
                    ).contentList ?? [],
                  );

                  return {
                    ...videoByLanguage,
                    contentList: formatContentArray(
                      normalizedLanguageContentList,
                    ),
                  };
                },
              );

            return {
              ...content,
              specific: {
                ...interactiveVideoContent,
              },
            };
          }

          return {
            ...content,
            specific: {
              ...content.specific,
              contentList: formatContentArray(
                normalizeContentList(
                  (content.specific as IInteractiveGeneralContent).contentList,
                ),
              ),
            },
          };
        }
        return content;
      });
      setContents([...updatedContents].reverse());
    } catch (error: any) {
      console.error('Error fetching content:', error);
      toast.error(error.message);
    } finally {
      setIsLoading(false);
    }
  }, [apiClient, chapterId]);

  useEffect(() => {
    if (chapterId) {
      fetchData();
    }
  }, [chapterId, fetchData]);

  const handleVideoPolling = (action: IntervalType, id: string) => {
    if (action === IntervalType.START) {
      setActiveVideoPollingIds(prev => {
        prev.add(id);
        return prev;
      });

      if (intervalRef.current !== null) return;

      intervalRef.current = setInterval(() => {
        fetchData();
      }, 10000);
    }

    if (action === IntervalType.CLEAR) {
      const updatedActiveVideoPollingIds = new Set(activeVideoPollingIds);
      updatedActiveVideoPollingIds.delete(id);
      setActiveVideoPollingIds(updatedActiveVideoPollingIds);

      if (
        updatedActiveVideoPollingIds.size === 0 &&
        intervalRef.current !== null
      ) {
        clearInterval(intervalRef.current);
        intervalRef.current = null;
      }
    }
  };

  useEffect(() => {
    return () => {
      if (intervalRef.current !== null) {
        clearInterval(intervalRef.current);
        intervalRef.current = null;
      }
    };
  }, []);

  const normalizeContentList = (
    data: Array<IInteractiveContent> | normalizeContentType,
  ) => {
    if (Array.isArray(data)) {
      return data;
    }

    const content = data?.contentList;

    if (Array.isArray(content)) {
      return content;
    }

    if (typeof content === 'object' && content !== null) {
      return [content];
    }

    return [];
  };

  const handleAddContent = async (data: {
    name: string;
    type: ContentTypes;
  }) => {
    const payload = {
      common: {
        contentName: data.name,
        contentType: data.type,
        status: ContentStatus.DRAFT,
        chapterIds: [chapterId],
        tags: [],
      },
      specific: GetContentDefaultValue(data.type),
    };

    try {
      const response: IResponse<IContent<TContent>> = await apiClient.post(
        API_END_POINTS.CONTENT_CREATE,
        { data: payload },
      );

      if (!isSuccessResponse(response.statusCode)) {
        throw Error(response.message);
      }

      setContents(prev => [response.data, ...prev]);
      toast.success(response.message);
    } catch (error: any) {
      console.error('Error create content:', error);
      toast.error(error.message);
    }
  };

  const handleDeleteContent = (id: string) => {
    setSelectedContent(contents.find(content => content.id === id)!);
    setOpenConfirmDialog(true);
  };

  const handleDeleteConfirm = async () => {
    setSubmitting(true);

    try {
      const response: IResponse<IContent<TContent>> = await apiClient.del(
        API_END_POINTS.CONTENT_DELETE + selectedContent?.id,
      );

      if (!isSuccessResponse(response.statusCode)) {
        throw new Error(response.message);
      }

      setContents(prev => [
        ...prev.filter(content => content.id !== selectedContent?.id),
      ]);
      toast.success(response.message);
    } catch (error: any) {
      console.error('Error delete content:', error);
      toast.error(error.message);
    } finally {
      setSubmitting(false);
      setSelectedContent(null);
      handleCloseConfirmDialog();
    }
  };

  const handleCloseConfirmDialog = () => {
    setOpenConfirmDialog(false);
    setSelectedContent(null);
  };

  const handleCloseSettingsModal = () => {
    setSelectedContent(null);
    setSubmitting(false);
  };

  const handleEditContent = (id: string) => {
    setSelectedContent(contents.find(content => content.id === id)!);
  };

  const handleSubmitData = async () => {
    if (!selectedContent || !selectedContentRef.current) return;

    const blockData = selectedContentRef.current?.validateAndGetData();
    if (blockData.success) {
      setSubmitting(true);

      const data = {
        ...selectedContent,
        common: {
          ...selectedContent.common,
          contentName: blockData.data!.contentName,
        },
        specific: await uploadContentFiles(
          blockData.data!.specific,
          selectedContent.common.contentType,
        ),
      };

      const payload = structuredClone(data);

      if (
        [
          ContentTypes.INTERACTIVE_VIDEO,
          ContentTypes.INTERACTIVE_CONTENT,
        ].includes(selectedContent.common.contentType)
      ) {
        if (
          selectedContent.common.contentType === ContentTypes.INTERACTIVE_VIDEO
        ) {
          (
            payload.specific as IInteractiveVideoContent
          ).interactiveVideoByLanguage = (
            payload.specific as IInteractiveVideoContent
          ).interactiveVideoByLanguage.map(videoByLanguage => {
            const updatedContentList = (videoByLanguage.contentList ?? []).map(
              (content: IInteractiveContent) => ({
                ...content,
                contentBody: {
                  ...content.contentBody,
                  commonContent: {
                    ...content.contentBody.common,
                  },
                  specificContent: {
                    ...content.contentBody.specific,
                  },
                },
              }),
            );

            updatedContentList.forEach((content: IInteractiveContent) => {
              delete (content.contentBody as any).common;
              delete (content.contentBody as any).specific;
            });

            return {
              ...videoByLanguage,
              contentList: updatedContentList,
            };
          });
        } else {
          (payload.specific as IInteractiveGeneralContent).contentList = (
            payload.specific as IInteractiveGeneralContent
          ).contentList.map((content: IInteractiveContent) => ({
            ...content,
            contentBody: {
              ...content.contentBody,
              commonContent: {
                ...content.contentBody.common,
              },
              specificContent: {
                ...content.contentBody.specific,
              },
            },
          }));

          (payload.specific as IInteractiveGeneralContent).contentList.forEach(
            (content: IInteractiveContent) => {
              delete (content.contentBody as any).common;
              delete (content.contentBody as any).specific;
            },
          );
        }
      }

      try {
        const response = await apiClient.put(
          API_END_POINTS.CONTENT_UPDATE + selectedContent.id,
          { data: payload },
        );

        if (!isSuccessResponse(response.statusCode)) {
          throw new Error(response.message);
        }

        toast.success(response.message);

        const updatedContents = contents.map(content =>
          content.id === selectedContent.id ? data : content,
        );
        setContents(updatedContents);
        setSelectedContent(null);
        handleCloseSettingsModal();
      } catch (error: any) {
        console.error('Error update content:', error);
        toast.error(error.message);
      } finally {
        setSubmitting(false);
      }
    }
  };

  const uploadContentFiles = async (
    content: TContent,
    contentType: ContentTypes,
  ) => {
    const uploadTasks: Array<Promise<void>> = [];

    if (content.backgroundFormatting?.backgroundImageFile) {
      const task = uploadFile(
        content.backgroundFormatting.backgroundImageFile,
      ).then(res => {
        if (res.error) {
          toast.error(res.error);
          return;
        }
        content.backgroundFormatting.backgroundImage = res.url;
      });
      uploadTasks.push(task);
    }
    delete content.backgroundFormatting.backgroundImageFile;

    if (
      [
        ContentTypes.TEXT,
        ContentTypes.LINK,
        ContentTypes.PDF,
        ContentTypes.SLIDER_SHOW,
        ContentTypes.TABBED,
      ].includes(contentType)
    ) {
      if ((content as ITextContent).featureImageFile) {
        const task = uploadFile(
          (content as ITextContent).featureImageFile!,
        ).then(res => {
          if (res.error) {
            toast.error(res.error);
            return;
          }
          (content as ITextContent).featureImageLink = res.url;
        });
        uploadTasks.push(task);
      }
      delete (content as ITextContent).featureImageFile;
    }

    if (contentType === ContentTypes.PDF) {
      if ((content as IPdfContent).metadata.additionalProp1.pdfFile) {
        const task = uploadFile(
          (content as IPdfContent).metadata.additionalProp1.pdfFile!,
        ).then(res => {
          if (res.error) {
            toast.error(res.error);
            return;
          }
          (content as IPdfContent).metadata.additionalProp1.pdfLink = res.url;
        });
        uploadTasks.push(task);
      }
      delete (content as IPdfContent).metadata.additionalProp1.pdfFile;
    }

    if ([ContentTypes.VIDEO, ContentTypes.ANIMATION].includes(contentType)) {
      if ((content as IVideoContent).interactiveVideo?.videoFile) {
        const task = uploadVideoFile(
          (content as IVideoContent).interactiveVideo.videoFile!,
        ).then(res => {
          if (res.error) {
            toast.error(res.error);
            return;
          }
          (content as IVideoContent).interactiveVideo.videoUrl = res.url;
        });
        uploadTasks.push(task);
      }
      delete (content as IVideoContent).interactiveVideo.videoFile;

      if ((content as IVideoContent).captionFile) {
        const task = uploadFile((content as IVideoContent).captionFile!).then(
          res => {
            if (res.error) {
              toast.error(res.error);
              return;
            }
            (content as IVideoContent).captionUrl = res.url;
          },
        );
        uploadTasks.push(task);
      }
      delete (content as IVideoContent).captionFile;
    }

    if (contentType === ContentTypes.SLIDER_SHOW) {
      if ((content as ISliderShowContent)?.slides) {
        (content as ISliderShowContent).slides.forEach((slide, index) => {
          if (slide.featureImageFile) {
            const task = uploadFile(slide.featureImageFile).then(res => {
              if (res.error) {
                toast.error(res.error);
                return;
              }
              (content as ISliderShowContent)!.slides[index].featureImageLink =
                res.url;
            });
            uploadTasks.push(task);
          }
          delete (content as ISliderShowContent)!.slides[index]
            .featureImageFile;
        });
      }
    }

    if (contentType === ContentTypes.STORY_BLOCK) {
      if ((content as IStoryBlockContent).steps) {
        (content as IStoryBlockContent).steps.forEach((step, index) => {
          if (step.additionalProperties.featureImageFile) {
            const task = uploadFile(
              step.additionalProperties.featureImageFile,
            ).then(res => {
              if (res.error) {
                toast.error(res.error);
                return;
              }
              (content as IStoryBlockContent).steps[
                index
              ].additionalProperties.featureImage = res.url;
            });
            uploadTasks.push(task);
          }
          delete (content as IStoryBlockContent).steps[index]
            .additionalProperties.featureImageFile;

          if (step.stories) {
            step.stories.forEach((story, storyIndex) => {
              if (story.additionalProperties.featureImageFile) {
                const task = uploadFile(
                  story.additionalProperties.featureImageFile,
                ).then(res => {
                  if (res.error) {
                    toast.error(res.error);
                    return;
                  }
                  (content as IStoryBlockContent).steps[index].stories[
                    storyIndex
                  ].additionalProperties.featureImage = res.url;
                });
                uploadTasks.push(task);
              }
              delete (content as IStoryBlockContent).steps[index].stories[
                storyIndex
              ].additionalProperties.featureImageFile;
            });
          }
        });
      }
    }

    if (contentType === ContentTypes.TABBED) {
      if ((content as ITabbedContent)?.additionalProperties.audioFile) {
        const task = uploadFile(
          (content as ITabbedContent).additionalProperties.audioFile!,
        ).then(res => {
          if (res.error) {
            toast.error(res.error);
            return;
          }
          (content as ITabbedContent).additionalProperties.audioUrl = res.url;
        });
        uploadTasks.push(task);
      }
      delete (content as ITabbedContent).additionalProperties.audioFile;

      if ((content as ITabbedContent)?.tabSections) {
        (content as ITabbedContent).tabSections.forEach((tabSection, index) => {
          if (tabSection.additionalProperties.audioFile) {
            const task = uploadFile(
              tabSection.additionalProperties.audioFile,
            ).then(res => {
              if (res.error) {
                toast.error(res.error);
                return;
              }
              (content as ITabbedContent).tabSections[
                index
              ].additionalProperties.audioUrl = res.url;
            });
            uploadTasks.push(task);
          }
          delete (content as ITabbedContent).tabSections[index]
            .additionalProperties.audioFile;
        });
      }
    }

    if (contentType === ContentTypes.SLIDER_LEVEL) {
      if ((content as ISliderLevelContent)?.steps) {
        (content as ISliderLevelContent).steps.forEach((step, index) => {
          if (step.additionalProperties.featureImageFile) {
            const task = uploadFile(
              step.additionalProperties.featureImageFile,
            ).then(res => {
              if (res.error) {
                toast.error(res.error);
                return;
              }
              (content as ISliderLevelContent).steps[
                index
              ].additionalProperties.featureImage = res.url;
            });
            uploadTasks.push(task);
          }
          delete (content as ISliderLevelContent).steps[index]
            .additionalProperties.featureImageFile;
        });
      }
    }

    if (
      [
        ContentTypes.INTERACTIVE_VIDEO,
        ContentTypes.INTERACTIVE_CONTENT,
      ].includes(contentType)
    ) {
      if (contentType === ContentTypes.INTERACTIVE_CONTENT) {
        if (
          (content as IInteractiveGeneralContent).interactiveVideo?.videoFile
        ) {
          const task = uploadVideoFile(
            (content as IInteractiveGeneralContent).interactiveVideo.videoFile!,
          ).then(res => {
            if (res.error) {
              toast.error(res.error);
              return;
            }
            (content as IInteractiveGeneralContent).interactiveVideo.videoUrl =
              res.url;

            (
              content as IInteractiveGeneralContent
            ).interactiveVideo.isProcessing = true;
            (
              content as IInteractiveGeneralContent
            ).interactiveVideo.processingStatus = ProcessingStatus.QUEUE;
          });
          uploadTasks.push(task);
        }
        delete (content as IInteractiveGeneralContent).interactiveVideo
          .videoFile;
      }

      if (contentType === ContentTypes.INTERACTIVE_VIDEO) {
        if (
          (content as IInteractiveVideoContent).interactiveVideoByLanguage
            ?.length
        ) {
          (
            content as IInteractiveVideoContent
          ).interactiveVideoByLanguage.forEach((videoByLanguage, index) => {
            if (videoByLanguage.videoFile) {
              const task = uploadVideoFile(videoByLanguage.videoFile).then(
                res => {
                  if (res.error) {
                    toast.error(res.error);
                    return;
                  }

                  (
                    content as IInteractiveVideoContent
                  ).interactiveVideoByLanguage[index].videoUrl = res.url;
                  (
                    content as IInteractiveVideoContent
                  ).interactiveVideoByLanguage[index].isProcessing = true;
                  (
                    content as IInteractiveVideoContent
                  ).interactiveVideoByLanguage[index].processingStatus =
                    ProcessingStatus.QUEUE;
                },
              );
              uploadTasks.push(task);
            }

            delete (content as IInteractiveVideoContent)
              .interactiveVideoByLanguage[index].videoFile;
          });
        }
      }

      if ((content as IInteractiveVideoContent).interactiveVideoByLanguage) {
        (
          content as IInteractiveVideoContent
        ).interactiveVideoByLanguage.forEach(
          (videoByLanguage, videoByLanguageIndex) => {
            (videoByLanguage.contentList ?? []).forEach(
              (nestedContent, index) => {
                const task = uploadContentFiles(
                  nestedContent.contentBody.specific,
                  nestedContent.contentType as ContentTypes,
                ).then(updatedContent => {
                  (
                    content as IInteractiveVideoContent
                  ).interactiveVideoByLanguage[
                    videoByLanguageIndex
                  ].contentList[index] = {
                    ...(content as IInteractiveVideoContent)
                      .interactiveVideoByLanguage[videoByLanguageIndex]
                      .contentList[index],
                    contentBody: {
                      ...(content as IInteractiveVideoContent)
                        .interactiveVideoByLanguage[videoByLanguageIndex]
                        .contentList[index].contentBody,
                      specific: updatedContent,
                    },
                  };
                });
                uploadTasks.push(task);
              },
            );
          },
        );
      }
    }

    if (contentType === ContentTypes.QUIZ) {
      if ((content as IQuizContent).imageFile!) {
        const task = uploadFile((content as IQuizContent).imageFile!).then(
          res => {
            if (res.error) {
              toast.error(res.error);
              return;
            }
            (content as IQuizContent).imageLink = res.url;
          },
        );
        uploadTasks.push(task);
      }
      delete (content as IQuizContent).imageFile;

      if ((content as IQuizContent).pairs) {
        (content as IQuizContent).pairs?.forEach((pair, index) => {
          if (pair.fileLeft) {
            const task = uploadFile(pair.fileLeft).then(res => {
              if (res.error) {
                toast.error(res.error);
                return;
              }
              (content as IQuizContent).pairs![index].linkLeft = res.url;
            });
            uploadTasks.push(task);
          }
          delete (content as IQuizContent).pairs![index].fileLeft;
          delete (content as IQuizContent).pairs![index].objectURLLeft;

          if (pair.fileRight) {
            const task = uploadFile(pair.fileRight).then(res => {
              if (res.error) {
                toast.error(res.error);
                return;
              }
              (content as IQuizContent).pairs![index].linkRight = res.url;
            });
            uploadTasks.push(task);
          }
          delete (content as IQuizContent).pairs![index].fileRight;
          delete (content as IQuizContent).pairs![index].objectURLRight;
        });
      }

      if ((content as IQuizContent).options) {
        (content as IQuizContent).options?.forEach((option, index) => {
          if (option.optionImage) {
            const task = uploadFile(option.optionImage).then(res => {
              if (res.error) {
                toast.error(res.error);
                return;
              }
              (content as IQuizContent).options![index].optionImageLink =
                res.url;
            });
            uploadTasks.push(task);
          }
          delete (content as IQuizContent).options![index].optionImage;
        });
      }
    }

    await Promise.all(uploadTasks);

    return content;
  };

  return (
    <Fragment>
      {isLoading ? (
        <div className="content-relative content-h-[405px] content-w-full">
          <Loader mode="container" />
        </div>
      ) : (
        <div className="content-relative content-flex">
          <ContentsIndex contents={contents} sectionRefs={sectionRefs} />
          <div className="content-w-4/5">
            {contents.length === 0 ? (
              <Fragment>
                <div className="content-flex content-items-center content-justify-center">
                  <MdContentPasteOff className="content-mt-20 content-text-9xl content-text-primary" />
                </div>
                <p className="content-mt-6 content-text-center content-text-stormy-gray">
                  Click the plus button above Kid to begin adding content blocks
                </p>
              </Fragment>
            ) : null}
            <ContentTypeSection onClickOption={handleAddContent} />

            <ContentBlocks
              sectionRefs={sectionRefs}
              contents={contents}
              onClickDelete={handleDeleteContent}
              onClickEdit={handleEditContent}
              handleVideoPolling={handleVideoPolling}
            />

            <ContentSettingsModal
              isOpen={!!selectedContent && !openConfirmDialog}
              loading={submitting}
              onClose={handleCloseSettingsModal}
              onSubmit={handleSubmitData}
            >
              {selectedContent?.common.contentType === ContentTypes.TEXT ? (
                <TextBased
                  ref={selectedContentRef}
                  content={selectedContent as IContent<ITextContent>}
                />
              ) : selectedContent?.common.contentType === ContentTypes.LINK ? (
                <LinkBased
                  ref={selectedContentRef}
                  content={selectedContent as IContent<ILinkContent>}
                />
              ) : selectedContent?.common.contentType === ContentTypes.PDF ? (
                <PdfBased
                  ref={selectedContentRef}
                  content={selectedContent as IContent<IPdfContent>}
                />
              ) : selectedContent?.common.contentType ===
                ContentTypes.QUESTION ? (
                <QuestionBased
                  ref={selectedContentRef}
                  content={selectedContent as IContent<IQuestionContent>}
                />
              ) : selectedContent?.common.contentType ===
                ContentTypes.MARKDOWN ? (
                <MarkdownBased
                  ref={selectedContentRef}
                  content={selectedContent as IContent<IMarkdownContent>}
                />
              ) : selectedContent?.common.contentType ===
                ContentTypes.SLIDER_SHOW ? (
                <SliderShowBased
                  ref={selectedContentRef}
                  content={selectedContent as IContent<ISliderShowContent>}
                />
              ) : selectedContent?.common.contentType ===
                ContentTypes.STORY_BLOCK ? (
                <StoryBlockBased
                  ref={selectedContentRef}
                  content={selectedContent as IContent<IStoryBlockContent>}
                />
              ) : selectedContent?.common.contentType ===
                ContentTypes.SLIDER_LEVEL ? (
                <SliderLevelBased
                  ref={selectedContentRef}
                  content={selectedContent as IContent<ISliderLevelContent>}
                />
              ) : selectedContent?.common.contentType ===
                ContentTypes.INTERACTIVE_VIDEO ? (
                <InteractiveVideoBased
                  ref={selectedContentRef}
                  content={
                    selectedContent as IContent<IInteractiveVideoContent>
                  }
                  uploadVideoProgress={uploadVideoProgress}
                />
              ) : selectedContent?.common.contentType === ContentTypes.VIDEO ||
                selectedContent?.common.contentType ===
                  ContentTypes.ANIMATION ? (
                <VideoBased
                  ref={selectedContentRef}
                  content={selectedContent as IContent<IVideoContent>}
                  uploadVideoProgress={uploadVideoProgress}
                />
              ) : selectedContent?.common.contentType ===
                ContentTypes.TABBED ? (
                <TabbedBased
                  ref={selectedContentRef}
                  content={selectedContent as IContent<ITabbedContent>}
                />
              ) : selectedContent?.common.contentType === ContentTypes.QUIZ ? (
                <QuizBased
                  ref={selectedContentRef}
                  content={selectedContent as IContent<IQuizContent>}
                />
              ) : selectedContent?.common.contentType ===
                ContentTypes.INTERACTIVE_CONTENT ? (
                <InteractiveContentBased
                  ref={selectedContentRef}
                  content={
                    selectedContent as IContent<IInteractiveGeneralContent>
                  }
                  uploadVideoProgress={uploadVideoProgress}
                />
              ) : null}
            </ContentSettingsModal>
          </div>

          <ConfirmDialog
            isOpen={openConfirmDialog}
            message="Are you sure you want to delete this content?"
            loading={submitting}
            loadingText="Deleting..."
            onClose={handleCloseConfirmDialog}
            onConfirm={handleDeleteConfirm}
          />
        </div>
      )}
    </Fragment>
  );
};

export default ChapterWiseContents;
