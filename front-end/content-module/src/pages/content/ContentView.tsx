import { Menu, MoveLeft, X } from 'lucide-react';
import { useCallback, useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { toast } from 'react-toastify';

import { SliderLeftIcon, SliderRightIcon } from 'assets/icons';
import { Button } from 'common/Button';
import Loader from 'common/loader/Loader';
import ContentCompleteModal from 'common/modal/miniModal/ContentComplete';
import CourseCompletedModal from 'common/modal/miniModal/CourseCompleted';
import RetakeCourseModal from 'common/modal/miniModal/RetakeCourse';
import TopicCompletedModal from 'common/modal/miniModal/TopicCompleted';
import BreadcrumbsLoader from 'components/skeleton/Breadcrumbs';
import UserChaptersLoader from 'components/skeleton/UserChapters';
import UserBlockWrapper from 'components/UserBlockWrapper';
import Border from 'components/UserBorder';
import { formatContentArray } from 'features/content/ChapterWiseContents';
import UserChapters from 'features/course/UserChapters';
import { useAPI } from 'hooks/UseAPI';
import { useCourseNavigation } from 'hooks/UseCourseNavigation';
import { useStore } from 'hooks/UseStore';
import {
  AllContentTypes,
  ContentTypes,
  IContent,
  IInteractiveGeneralContent,
  IInteractiveVideoContent,
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
  QuizTypes,
  TContent,
} from 'models/Content';
import { IUserCourseDetails } from 'models/Course';
import { IResponse } from 'models/Global';
import { IUserPackageDetails, UserSubPackageStatus } from 'models/Package';
import InteractiveContent from 'pages/content/block/InteractiveContent';
import InteractiveVideo from 'pages/content/block/InteractiveVideo';
import LinkBased from 'pages/content/block/Link';
import Markdown from 'pages/content/block/Markdown';
import PdfBased from 'pages/content/block/Pdf';
import QuestionBased from 'pages/content/block/Question';
import HotSpotBlock from 'pages/content/block/quiz/HotSpot';
import LikerSelectBlock from 'pages/content/block/quiz/LikerSelect';
import MatchingBlock from 'pages/content/block/quiz/Matching';
import OrderingBlock from 'pages/content/block/quiz/Ordering';
import PasswordComplianceBlock from 'pages/content/block/quiz/PasswordCompliance';
import PhishingDetectionBlock from 'pages/content/block/quiz/PhishingDetection';
import PictureChoiceBlock from 'pages/content/block/quiz/PictureChoice';
import RansomwareSimulatorBlock from 'pages/content/block/quiz/RansomwareSimulator';
import ScenarioBlock from 'pages/content/block/quiz/Scenario';
import SliderLevel from 'pages/content/block/SliderLevel';
import SliderShow from 'pages/content/block/SliderShow';
import StoryBlock from 'pages/content/block/StoryBlock';
import Tabbed from 'pages/content/block/Tabbed';
import TextBased from 'pages/content/block/Text';
import VideoBlock from 'pages/content/block/Video';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { routes } from 'routes/Routes';
import { cn, isSuccessResponse, objectToQueryString } from 'utils/Helper';
import PhishingTrainingCompleteModal from 'common/modal/miniModal/PhishingTrainingCompleteModal';

export const renderQuizBlock = (
  content: IContent<IQuizContent>,
  courseComplete?: () => void,
  isCompleted?: boolean,
  showCompleteButton?: (type: AllContentTypes) => void,
) => {
  switch (content.specific.quizType) {
    case QuizTypes.ORDERING:
      return (
        <OrderingBlock
          content={content}
          showCompleteButton={showCompleteButton}
          courseComplete={courseComplete}
          isCompleted={isCompleted}
        />
      );
    case QuizTypes.MATCHING:
      return (
        <MatchingBlock
          content={content}
          showCompleteButton={showCompleteButton}
          courseComplete={courseComplete}
          isCompleted={isCompleted}
        />
      );
    case QuizTypes.PICTURE:
      return (
        <PictureChoiceBlock
          content={content}
          showCompleteButton={showCompleteButton}
          courseComplete={courseComplete}
          isCompleted={isCompleted}
        />
      );
    case QuizTypes.SCENARIO:
      return (
        <ScenarioBlock
          content={content}
          showCompleteButton={showCompleteButton}
          courseComplete={courseComplete}
          isCompleted={isCompleted}
        />
      );
    case QuizTypes.LIKER:
      return (
        <LikerSelectBlock
          content={content}
          showCompleteButton={showCompleteButton}
          courseComplete={courseComplete}
          isCompleted={isCompleted}
        />
      );
    case QuizTypes.HOT_SPOT:
      return (
        <HotSpotBlock
          content={content}
          showCompleteButton={showCompleteButton}
          courseComplete={courseComplete}
          isCompleted={isCompleted}
        />
      );
    case QuizTypes.PASSWORD_COMPLIANCE:
      return (
        <PasswordComplianceBlock
          content={content}
          showCompleteButton={showCompleteButton}
          courseComplete={courseComplete}
          isCompleted={isCompleted}
        />
      );
    case QuizTypes.RANSOMWARE_SIMULATOR:
      return (
        <RansomwareSimulatorBlock
          content={content}
          showCompleteButton={showCompleteButton}
          courseComplete={courseComplete}
          isCompleted={isCompleted}
        />
      );
    case QuizTypes.PHISHING_DETECTION:
      return (
        <PhishingDetectionBlock
          content={content}
          showCompleteButton={showCompleteButton}
          courseComplete={courseComplete}
          isCompleted={isCompleted}
        />
      );
    default:
      return null;
  }
};

const renderContentBlock = (
  content: IContent<TContent>,
  contentComplete: () => void,
  isCompleted: boolean,
) => {
  switch (content?.common.contentType) {
    case ContentTypes.TEXT:
      return <TextBased content={content as IContent<ITextContent>} />;
    case ContentTypes.LINK:
      return (
        <LinkBased
          content={content as IContent<ILinkContent>}
          courseComplete={contentComplete}
        />
      );
    case ContentTypes.PDF:
      return <PdfBased content={content as IContent<IPdfContent>} />;
    case ContentTypes.MARKDOWN:
      return <Markdown content={content as IContent<IMarkdownContent>} />;
    case ContentTypes.SLIDER_SHOW:
      return (
        <SliderShow
          content={content as IContent<ISliderShowContent>}
          courseComplete={contentComplete}
        />
      );
    case ContentTypes.STORY_BLOCK:
      return (
        <StoryBlock
          content={content as IContent<IStoryBlockContent>}
          courseComplete={contentComplete}
        />
      );
    case ContentTypes.SLIDER_LEVEL:
      return (
        <SliderLevel
          content={content as IContent<ISliderLevelContent>}
          courseComplete={contentComplete}
        />
      );
    case ContentTypes.VIDEO:
    case ContentTypes.ANIMATION:
      return (
        <VideoBlock
          content={content as IContent<IVideoContent>}
          courseComplete={contentComplete}
        />
      );
    case ContentTypes.INTERACTIVE_CONTENT:
      return (
        <InteractiveContent
          content={content as IContent<IInteractiveGeneralContent>}
          courseComplete={contentComplete}
        />
      );
    case ContentTypes.INTERACTIVE_VIDEO:
      return (
        <InteractiveVideo
          content={content as IContent<IInteractiveVideoContent>}
          courseComplete={contentComplete}
        />
      );
    case ContentTypes.QUESTION:
      return (
        <QuestionBased
          content={content as IContent<IQuestionContent>}
          courseComplete={contentComplete}
          isCompleted={isCompleted}
        />
      );
    case ContentTypes.TABBED:
      return (
        <Tabbed
          content={content as IContent<ITabbedContent>}
          courseComplete={contentComplete}
        />
      );
    case ContentTypes.QUIZ:
      return renderQuizBlock(
        content as IContent<IQuizContent>,
        contentComplete,
        isCompleted,
      );
    default:
      return null;
  }
};

const showDefaultCompleteButton = [
  ContentTypes.TEXT,
  ContentTypes.PDF,
  ContentTypes.MARKDOWN,
];

const ContentView = () => {
  const { userInfo } = useStore();
  const { packageId, contentId, slug } = useParams();
  const navigate = useNavigate();
  const apiClient = useAPI();
  const [loading, setLoading] = useState<boolean>(true);
  const [contentLoading, setContentLoading] = useState<boolean>(true);
  const [completeLoading, setCompleteLoading] = useState<boolean>(false);
  const [resetLoading, setResetLoading] = useState<boolean>(false);
  const [courseData, setCourseData] = useState<IUserCourseDetails>();
  const [contentData, setContentData] = useState<IContent<TContent>>();
  const [showContentCompleteModal, setShowContentCompleteModal] =
    useState<boolean>(false);
  const [showCompleteButton, _setShowCompleteButton] = useState<
    Array<AllContentTypes>
  >([...showDefaultCompleteButton]);
  const [openBox, setOpenBox] = useState<string>('');
  const [isSidebarOpen, setIsSidebarOpen] = useState<boolean>(false);
  const {
    isFirstContent,
    isLastContent,
    goToNextContent,
    goToPreviousContent,
    getCurrentPosition,
  } = useCourseNavigation(courseData, packageId, contentId, slug, setOpenBox);
  const { chapterIndex = -1, contentIndex = -1 } = getCurrentPosition() ?? {};
  const [showCourseCompleteModal, setShowCourseCompleteModal] =
    useState<boolean>(false);
  const [showRetakeModal, setShowRetakeModal] = useState<boolean>(false);
  const [showTopicCompleteModal, setShowTopicCompleteModal] =
    useState<boolean>(false);
  const [isNavigatingToExam, setIsNavigatingToExam] = useState<boolean>(false);
  const [
    showPhishingTrainingCompleteModal,
    setShowPhishingTrainingCompleteModal,
  ] = useState<boolean>(false);
  const fetchCourseDetails = useCallback(async () => {
    try {
      const response: IResponse<IUserCourseDetails> = await apiClient.get(
        API_END_POINTS.USER_TOPIC_DETAILS.replace(':topicId', slug || '') +
          '?' +
          objectToQueryString({
            userId: userInfo.userId,
            subPackageId: packageId,
          }),
      );

      if (!isSuccessResponse(response.statusCode)) {
        throw new Error(response.message || 'Failed to fetch course details');
      }

      setCourseData(response.data);
    } catch (error) {
      console.error('Error fetching selected sub package topics:', error);
      toast.error((error as Error).message);
    } finally {
      setLoading(false);
    }
  }, [apiClient, packageId, slug, userInfo.userId]);

  const fetchCourseCompleteStatus = useCallback(
    async (options?: {
      updateModals?: boolean;
    }): Promise<IUserPackageDetails | null> => {
      const updateModals = options?.updateModals ?? true;
      try {
        const response: IResponse<IUserPackageDetails> = await apiClient.get(
          API_END_POINTS.USER_SUB_PACKAGE_DETAILS +
            objectToQueryString({
              subPackageId: packageId,
              userId: userInfo.userId,
            }),
        );

        if (!isSuccessResponse(response.statusCode)) {
          throw new Error(
            response.message || 'Failed to fetch course complete status',
          );
        }

        if (
          updateModals &&
          response.data.status === UserSubPackageStatus.EXAM
        ) {
          setShowTopicCompleteModal(false);
          setShowCourseCompleteModal(true);
        }
        if (
          updateModals &&
          response.data.status ===
            UserSubPackageStatus.PHISHING_TRAINING_COMPLETED
        ) {
          setShowTopicCompleteModal(false);
          setShowPhishingTrainingCompleteModal(true);
        }
        return response.data;
      } catch (error) {
        console.error('Error fetching course complete status:', error);
        toast.error((error as Error).message);
        return null;
      } finally {
        setLoading(false);
      }
    },
    [apiClient, packageId, userInfo.userId],
  );

  useEffect(() => {
    fetchCourseDetails();
    fetchCourseCompleteStatus();
  }, [fetchCourseCompleteStatus, fetchCourseDetails]);

  const fetchContentDetails = useCallback(async () => {
    setContentLoading(true);

    try {
      const response: IResponse<IContent<TContent>> = await apiClient.get(
        API_END_POINTS.GET_CONTENT + contentId,
      );

      if (!isSuccessResponse(response.statusCode)) {
        throw new Error(response.message || 'Failed to fetch content details');
      }

      if (
        response.data.common.contentType === ContentTypes.INTERACTIVE_VIDEO ||
        response.data.common.contentType === ContentTypes.INTERACTIVE_CONTENT
      ) {
        if (
          response.data.common.contentType === ContentTypes.INTERACTIVE_VIDEO
        ) {
          const specific = response.data.specific as IInteractiveVideoContent;

          setContentData({
            ...response.data,
            specific: {
              ...specific,
              interactiveVideoByLanguage:
                specific.interactiveVideoByLanguage?.map(videoByLanguage => ({
                  ...videoByLanguage,
                  contentList: formatContentArray(
                    (videoByLanguage.contentList ?? []) as any,
                  ),
                })) ?? [],
            },
          });
          return;
        }

        setContentData({
          ...response.data,
          specific: {
            ...response.data.specific,
            contentList: formatContentArray(
              (response.data.specific as IInteractiveGeneralContent)
                .contentList,
            ),
          },
        });
        return;
      }
      setContentData(response.data);
    } catch (error) {
      console.error('Error Get content:', error);
      toast.error((error as Error).message);
    } finally {
      setContentLoading(false);
    }
  }, [apiClient, contentId]);

  useEffect(() => {
    if (!contentId) return;
    fetchContentDetails();
  }, [contentId, fetchContentDetails]);

  useEffect(() => {
    setIsSidebarOpen(false);
  }, [contentId]);

  useEffect(() => {
    if (!isSidebarOpen) return;

    const onKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') setIsSidebarOpen(false);
    };

    window.addEventListener('keydown', onKeyDown);
    return () => window.removeEventListener('keydown', onKeyDown);
  }, [isSidebarOpen]);

  const updateCompleteStatus = () => {
    if (
      chapterIndex !== undefined &&
      contentIndex !== undefined &&
      courseData?.chapters
    ) {
      const updatedChapters = [...courseData.chapters];
      updatedChapters[chapterIndex].contents[contentIndex].done = true;
      setCourseData({
        ...courseData,
        chapters: updatedChapters,
      });
    }
  };

  const isAllContentsComplete = (): boolean => {
    if (!courseData?.chapters || chapterIndex < 0 || contentIndex < 0)
      return false;
    const chapters = [...courseData.chapters];
    const current = chapters[chapterIndex]?.contents[contentIndex];
    if (!current) return false;
    const updatedChapters = chapters.map((ch, chIdx) =>
      chIdx === chapterIndex
        ? {
            ...ch,
            contents: ch.contents.map((c, cIdx) =>
              cIdx === contentIndex ? { ...c, done: true } : c,
            ),
          }
        : ch,
    );
    return updatedChapters
      .flatMap(ch => ch.contents)
      .every(content => content.done);
  };

  const currentContent = () => {
    if (!courseData || chapterIndex < 0 || contentIndex < 0) return;
    return courseData?.chapters[chapterIndex].contents[contentIndex];
  };

  const currentChapterProgress = () => {
    if (!courseData || chapterIndex < 0) return 0;
    const chapter = courseData.chapters[chapterIndex];
    const totalContents = chapter.contents.length;
    const completedContents = chapter.contents.filter(c => c.done).length;
    return (completedContents / totalContents) * 100;
  };

  const contentComplete = async () => {
    if (currentContent()?.done) return;
    setShowContentCompleteModal(true);
  };

  const handleContentCompleteAPI = async (allContentsComplete: boolean) => {
    if (
      [UserSubPackageStatus.EXAM, UserSubPackageStatus.COMPLETED].includes(
        courseData?.status as UserSubPackageStatus,
      )
    ) {
      setShowContentCompleteModal(false);
      return;
    }

    setCompleteLoading(true);
    try {
      const response = await apiClient.post(
        API_END_POINTS.USER_CONTENT_COMPLETE +
          objectToQueryString({
            topicId: slug,
            contentId: contentId,
            userId: userInfo.userId,
            subPackageId: packageId,
          }),
      );

      if (!isSuccessResponse(response.statusCode)) {
        throw new Error(
          response.message || 'Failed to mark content as complete',
        );
      }

      const packageDetails = await fetchCourseCompleteStatus({
        updateModals: false,
      });

      if (packageDetails?.status === UserSubPackageStatus.EXAM) {
        setShowCourseCompleteModal(true);
      } else if (
        packageDetails?.status ===
        UserSubPackageStatus.PHISHING_TRAINING_COMPLETED
      ) {
        setShowPhishingTrainingCompleteModal(true);
      } else if (allContentsComplete) {
        setShowTopicCompleteModal(true);
      }
    } catch (error) {
      console.error('Error completing content:', error);
      toast.error((error as Error).message);
    } finally {
      setCompleteLoading(false);
      setShowContentCompleteModal(false);
    }
  };

  // const handleCompleteButton = (contentType: AllContentTypes) => {
  //   if (showCompleteButton.includes(contentType)) return;
  //   setShowCompleteButton(prev => [...prev, contentType]);
  // };

  const handleRetakeCourse = async () => {
    setResetLoading(true);

    try {
      const response = await apiClient.post(
        API_END_POINTS.USER_PACKAGE_RESET +
          objectToQueryString({
            userId: userInfo.userId,
            subPackageId: packageId,
          }),
      );

      if (!isSuccessResponse(response.statusCode)) {
        throw new Error(response.message || 'Failed to reset package');
      }

      setShowRetakeModal(false);
      setShowCourseCompleteModal(false);
      setShowPhishingTrainingCompleteModal(false);
      navigate(routes.courseList.path);
    } catch (error) {
      console.error('Error package reset:', error);
      toast.error((error as Error).message);
    } finally {
      setResetLoading(false);
    }
  };

  const handleContentComplete = async () => {
    const allContentsComplete = isAllContentsComplete();
    updateCompleteStatus();
    await handleContentCompleteAPI(allContentsComplete);
    goToNextContent();
  };

  const handleGoBackToCourse = () => {
    navigate(
      routes.courseDetails.path
        .replace(':packageId', packageId || '')
        .replace(':slug', slug || ''),
    );
  };

  return (
    <>
      <Border>
        <div className="content-relative content-flex content-h-[calc(100dvh-5.5rem)] content-min-h-0 content-flex-col content-overflow-hidden sm:content-h-[calc(100dvh-6rem)] lg:content-grid lg:content-grid-cols-4">
          {isSidebarOpen && (
            <button
              type="button"
              aria-label="Close chapter list"
              className="content-absolute content-inset-0 content-z-20 content-bg-black/50 lg:content-hidden"
              onClick={() => setIsSidebarOpen(false)}
            />
          )}

          <aside
            className={cn(
              'content-flex content-flex-col content-space-y-6 content-overflow-y-auto content-border-card-border content-p-4 content-transition-transform content-duration-300',
              'content-absolute content-inset-y-0 content-left-0 content-z-30 content-w-[min(100%,20rem)] content-border-r content-bg-dark',
              isSidebarOpen
                ? 'content-translate-x-0'
                : '-content-translate-x-full',
              'lg:content-relative lg:content-z-auto lg:content-col-span-1 lg:content-h-full lg:content-w-auto lg:content-translate-x-0 lg:content-bg-transparent lg:content-p-6',
            )}
          >
            <div className="content-mb-4 content-flex content-items-center content-justify-between content-gap-2">
              <div className="content-min-w-0 content-flex-1">
                {loading ? (
                  <div className="content-hidden lg:content-block">
                    <BreadcrumbsLoader />
                  </div>
                ) : (
                  <Button
                    variant="outline"
                    className="content-hidden lg:content-inline-flex"
                    onClick={handleGoBackToCourse}
                  >
                    <MoveLeft className="!content-size-6" />
                    Back to Course
                  </Button>
                )}
                <p className="content-text-lg content-font-semibold content-text-white lg:content-hidden">
                  Chapters
                </p>
              </div>
              <Button
                variant="ghost"
                size="icon"
                className="content-shrink-0 content-text-white hover:content-bg-white/10 lg:content-hidden"
                onClick={() => setIsSidebarOpen(false)}
                aria-label="Close chapter list"
              >
                <X className="!content-size-5" />
              </Button>
            </div>

            {loading ? (
              <UserChaptersLoader />
            ) : (
              <UserChapters
                chapters={courseData?.chapters || []}
                courseId={courseData?.topicId || ''}
                openBox={openBox}
                setOpenBox={setOpenBox}
                packageId={packageId || ''}
                onContentSelect={() => setIsSidebarOpen(false)}
              />
            )}
          </aside>

          <div className="content-flex content-min-h-0 content-min-w-0 content-flex-1 content-flex-col content-overflow-hidden content-p-3 sm:content-p-4 lg:content-col-span-3">
            <div className="content-mb-3 content-flex content-items-center content-justify-between content-gap-2 lg:content-hidden">
              <Button
                variant="outline"
                size="sm"
                onClick={handleGoBackToCourse}
              >
                <MoveLeft className="!content-size-5" />
                <span className="content-hidden sm:content-inline">
                  Back to Course
                </span>
                <span className="sm:content-hidden">Back</span>
              </Button>
              <Button
                variant="outline"
                size="sm"
                onClick={() => setIsSidebarOpen(true)}
              >
                <Menu className="!content-size-4" />
                Chapters
              </Button>
            </div>

            <div className="content-relative content-flex content-min-h-0 content-flex-1 content-flex-col content-justify-between">
              <div className="content-min-h-0 content-flex-1 content-overflow-auto">
                {contentLoading ? (
                  <Loader mode="container" />
                ) : (
                  <UserBlockWrapper content={contentData as IContent<TContent>}>
                    {renderContentBlock(
                      contentData as IContent<TContent>,
                      contentComplete,
                      !!currentContent()?.done,
                    )}
                    {!currentContent()?.done &&
                      showCompleteButton.includes(
                        ((contentData?.specific as IQuizContent)
                          ?.quizType as QuizTypes) ||
                          contentData?.common?.contentType,
                      ) && (
                        <div className="content-mx-4 content-mt-6 content-border-t content-border-card-border content-pt-6 sm:content-mx-6 sm:content-mt-8 sm:content-pt-8">
                          <Button
                            className="content-w-full sm:content-w-auto"
                            onClick={contentComplete}
                          >
                            Mark as Completed
                          </Button>
                        </div>
                      )}
                  </UserBlockWrapper>
                )}
              </div>
              <div className="content-mt-4 content-flex content-shrink-0 content-flex-col content-gap-3 content-border-t content-border-card-border content-pt-3 sm:content-mt-6 sm:content-flex-row sm:content-items-center sm:content-gap-6 sm:content-pt-4">
                <div className="content-h-2.5 content-min-w-0 content-w-full content-flex-1 content-rounded-full content-bg-white content-bg-opacity-25">
                  <div
                    className="content-h-2.5 content-rounded-full content-bg-primary"
                    style={{
                      width: `${Math.round(currentChapterProgress() || 0)}%`,
                    }}
                  ></div>
                </div>
                <div className="content-flex content-shrink-0 content-justify-end content-gap-3 sm:content-gap-4">
                  <Button
                    className="content-size-10 !content-rounded-full content-bg-[#D9D9D9]/25 hover:content-bg-primary sm:content-size-12"
                    variant="ghost"
                    onClick={goToPreviousContent}
                    disabled={contentLoading || isFirstContent()}
                  >
                    <SliderLeftIcon stroke="white" width={24} />
                  </Button>
                  <Button
                    className="content-size-10 !content-rounded-full content-bg-[#D9D9D9]/25 hover:content-bg-primary sm:content-size-12"
                    variant="ghost"
                    onClick={goToNextContent}
                    disabled={contentLoading || isLastContent()}
                  >
                    <SliderRightIcon stroke="white" width={24} />
                  </Button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </Border>

      {showContentCompleteModal && (
        <ContentCompleteModal
          isCorrect={true}
          isOpen={showContentCompleteModal}
          onClose={() => setShowContentCompleteModal(false)}
          onClick={handleContentComplete}
          loading={completeLoading}
        />
      )}

      {showTopicCompleteModal && (
        <TopicCompletedModal
          isOpen={showTopicCompleteModal}
          onClose={() => setShowTopicCompleteModal(false)}
          onClick={() =>
            navigate(routes.courseList.path + '?course=' + packageId)
          }
        />
      )}

      {showCourseCompleteModal && (
        <CourseCompletedModal
          isOpen={showCourseCompleteModal}
          onClose={() => setShowCourseCompleteModal(false)}
          retake={() => setShowRetakeModal(true)}
          isNavigatingToExam={isNavigatingToExam}
          takeExam={() => {
            if (!packageId) return;
            setIsNavigatingToExam(true);
            navigate(routes.exam.path.replace(':slug', packageId));
          }}
        />
      )}

      {showRetakeModal && (
        <RetakeCourseModal
          isOpen={showRetakeModal}
          onClose={() => setShowRetakeModal(false)}
          retake={handleRetakeCourse}
          loading={resetLoading}
        />
      )}

      {showPhishingTrainingCompleteModal && (
        <PhishingTrainingCompleteModal
          isOpen={showPhishingTrainingCompleteModal}
          onClose={() => setShowPhishingTrainingCompleteModal(false)}
          isNavigatingToCourse={() => navigate(routes.courseList.path)}
        />
      )}
    </>
  );
};

export default ContentView;
