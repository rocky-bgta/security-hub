import { Dispatch, Fragment, SetStateAction, useEffect, useState } from 'react';
import { IoIosArrowDown } from 'react-icons/io';
import { IoCheckmarkSharp } from 'react-icons/io5';
import { useNavigate, useParams } from 'react-router-dom';
import { IUserChapter } from 'models/Course';
import { routes } from 'routes/Routes';
import { cn } from 'utils/Helper';
import {
  MousePointerClick,
  PlayCircle,
  FileText,
  Link2,
  Brain,
  LayoutGrid,
  BookOpen,
  Sparkles,
} from 'lucide-react';
import { ContentTypes } from 'models/Content';
import { PUBLIC_URL } from 'utils/Constants';

interface IProps {
  chapters: Array<IUserChapter>;
  courseId: string;
  openBox: string;
  setOpenBox: Dispatch<SetStateAction<string>>;
  hostPath: typeof routes;
  packageId?: string;
}

const contentImages = {
  [ContentTypes.PDF]: PUBLIC_URL + '/content-image/pdf-base.webp',
  [ContentTypes.VIDEO]: PUBLIC_URL + '/content-image/video-base.webp',
  [ContentTypes.ANIMATION]: PUBLIC_URL + '/content-image/animation-base.svg',
  [ContentTypes.TEXT]: PUBLIC_URL + '/content-image/text-base.svg',
  [ContentTypes.MARKDOWN]: PUBLIC_URL + '/content-image/markdown-base.webp',
  [ContentTypes.INTERACTIVE_VIDEO]:
    PUBLIC_URL + '/content-image/interactive-video-base.webp',
  [ContentTypes.INTERACTIVE_CONTENT]:
    PUBLIC_URL + '/content-image/interactive-content-base.webp',
  [ContentTypes.LINK]: PUBLIC_URL + '/content-image/link-base.svg',
  [ContentTypes.QUESTION]: PUBLIC_URL + '/content-image/question-base.webp',
  [ContentTypes.QUIZ]: PUBLIC_URL + '/content-image/quiz-base.svg',
  [ContentTypes.SLIDER_LEVEL]:
    PUBLIC_URL + '/content-image/slider-level-base.svg',
  [ContentTypes.SLIDER_SHOW]:
    PUBLIC_URL + '/content-image/slider-show-base.webp',
  [ContentTypes.STORY_BLOCK]:
    PUBLIC_URL + '/content-image/story-block-base.svg',
  [ContentTypes.TABBED]: PUBLIC_URL + '/content-image/tabbed-base.svg',
};

const contentIcons = {
  [ContentTypes.PDF]: FileText,
  [ContentTypes.VIDEO]: PlayCircle,
  [ContentTypes.ANIMATION]: PlayCircle,
  [ContentTypes.TEXT]: BookOpen,
  [ContentTypes.MARKDOWN]: FileText,
  [ContentTypes.QUESTION]: Brain,
  [ContentTypes.INTERACTIVE_VIDEO]: PlayCircle,
  [ContentTypes.INTERACTIVE_CONTENT]: Sparkles,
  [ContentTypes.LINK]: Link2,
  [ContentTypes.QUIZ]: Brain,
  [ContentTypes.SLIDER_LEVEL]: LayoutGrid,
  [ContentTypes.SLIDER_SHOW]: LayoutGrid,
  [ContentTypes.STORY_BLOCK]: BookOpen,
  [ContentTypes.TABBED]: LayoutGrid,
};

const UserChaptersCardView = ({
  chapters,
  courseId,
  openBox,
  setOpenBox,
  hostPath,
  packageId,
}: IProps) => {
  const { contentId } = useParams();
  const navigate = useNavigate();
  const [hoveredCard, setHoveredCard] = useState<string | null>(null);

  useEffect(() => {
    if (contentId) {
      const selectedChapter = chapters.findIndex(chapter =>
        chapter?.contents?.find(content => content?.contentId === contentId),
      );
      if (selectedChapter > -1) {
        setOpenBox(chapters[selectedChapter]?.chapterId);
      }
      return;
    }
    setOpenBox(chapters[0]?.chapterId);
  }, [chapters]);

  const toggleOpenBox = (id: string) => {
    setOpenBox(prev => (prev === id ? '' : id));
  };

  const handleOpen = (id: string) => {
    toggleOpenBox(id);
  };

  const getCompletionPercentage = (chapter: IUserChapter) => {
    if (!chapter?.contents?.length) return 0;
    const completed = chapter.contents.filter(c => c?.done).length;
    return Math.round((completed / chapter.contents.length) * 100);
  };

  return (
    <Fragment>
      {chapters.map(chapter => {
        const completionPercentage = getCompletionPercentage(chapter);
        const isOpen = chapter.chapterId === openBox;

        return (
          <div
            key={chapter.chapterId}
            className="content-overflow-hidden content-rounded-xl content-border content-border-white/10 content-bg-gradient-to-br content-from-white/5 content-to-white/10 content-shadow-lg content-backdrop-blur-sm content-transition-all content-duration-300 hover:content-border-white/20 hover:content-shadow-2xl"
          >
            {/* Chapter Header */}
            <div
              onClick={() => handleOpen(chapter.chapterId)}
              className="content-relative content-flex content-w-full content-cursor-pointer content-items-center content-justify-between content-bg-white/10 content-p-3 content-text-left content-transition-all content-duration-300 hover:content-bg-white/15"
            >
              <div className="content-flex-1">
                <div className="content-flex content-items-center content-gap-3">
                  <div className="content-flex content-size-10 content-items-center content-justify-center content-rounded-lg content-bg-gradient-to-br content-from-primary/80 content-to-primary content-shadow-md">
                    <BookOpen className="content-size-5 content-text-white" />
                  </div>
                  <div className="content-flex-1">
                    <h3 className="content-mb-1 content-text-lg content-font-bold content-text-white">
                      {chapter?.chapterName}
                    </h3>
                    <div className="content-flex content-items-center content-gap-4 content-text-sm">
                      <span className="content-text-white/70">
                        {chapter?.contents?.length || 0} lessons
                      </span>
                      <div className="content-flex content-items-center content-gap-2">
                        <div className="content-h-1.5 content-w-24 content-overflow-hidden content-rounded-full content-bg-white/20">
                          <div
                            className="content-h-full content-bg-gradient-to-r content-from-primary content-to-primary/80 content-transition-all content-duration-500"
                            style={{ width: `${completionPercentage}%` }}
                          />
                        </div>
                        <span className="content-font-semibold content-text-primary">
                          {completionPercentage}%
                        </span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <IoIosArrowDown
                className={cn(
                  'content-h-6 content-w-6 content-text-white content-transition-all content-duration-300',
                  isOpen ? 'content-rotate-180' : '',
                )}
              />
            </div>

            {/* Content Cards Grid */}
            {isOpen && (
              <div className="content-p-5 content-pt-4">
                <div className="content-grid content-grid-cols-1 content-gap-4 sm:content-grid-cols-2 md:content-grid-cols-3 lg:content-grid-cols-4 xl:content-grid-cols-5">
                  {chapter?.contents?.map(content => {
                    const isActive = content?.contentId === contentId;
                    const Icon =
                      contentIcons[
                      content?.contentType as keyof typeof contentIcons
                      ] || MousePointerClick;

                    return (
                      <div
                        key={content?.contentId}
                        onClick={() =>
                          navigate(
                            hostPath.contentDetails.path
                              .replace(':packageId', packageId || '')
                              .replace(':slug', courseId)
                              .replace(':contentId', content?.contentId),
                          )
                        }
                        onMouseEnter={() => setHoveredCard(content?.contentId)}
                        onMouseLeave={() => setHoveredCard(null)}
                        className={cn(
                          'content-group content-relative content-cursor-pointer content-overflow-hidden content-rounded-xl content-transition-all content-duration-300',
                          'content-border content-border-white/10 content-bg-white/5',
                          'hover:content-border-white/20 hover:content-bg-white/10 hover:content-shadow-xl',
                          isActive &&
                          'content-border-primary/50 content-ring-2 content-ring-primary',
                        )}
                      >
                        {/* Completion Badge */}
                        {content?.done && (
                          <div className="content-absolute content-right-2 content-top-2 content-z-20">
                            <div className="content-relative">
                              <div className="content-absolute content-inset-0 content-rounded-full content-bg-primary content-opacity-50 content-blur-sm" />
                              <div className="content-relative content-flex content-items-center content-justify-center content-gap-2 content-rounded-md content-bg-gradient-to-br content-from-primary content-to-primary/90 content-p-2 content-text-sm content-shadow-xl">
                                <IoCheckmarkSharp className="content-size-3.5 content-text-white" />
                                Completed
                              </div>
                            </div>
                          </div>
                        )}

                        {/* Image Section with Overlay */}
                        <div className="content-relative content-h-32 content-overflow-hidden">
                          <img
                            src={
                              contentImages[
                              content?.contentType as keyof typeof contentImages
                              ]
                            }
                            alt={content?.contentType}
                            className={cn(
                              'content-h-full content-w-full content-object-cover content-transition-transform content-duration-500',
                              hoveredCard === content?.contentId &&
                              'content-scale-110',
                            )}
                          />

                          {/* Gradient Overlay */}
                          <div className="content-absolute content-inset-0 content-bg-gradient-to-t content-from-black/60 content-via-black/20 content-to-transparent" />

                          {/* Icon Overlay */}
                          <div
                            className={cn(
                              'content-absolute content-inset-0 content-flex content-items-center content-justify-center',
                              hoveredCard === content?.contentId
                                ? 'content-opacity-100'
                                : 'content-opacity-0',
                            )}
                          >
                            <div className={cn('content-rounded-full content-border content-p-3 content-shadow-lg content-backdrop-blur-md', content.contentType === ContentTypes.INTERACTIVE_VIDEO || content.contentType === ContentTypes.INTERACTIVE_CONTENT || content.contentType === ContentTypes.VIDEO ? 'content-border-red-600 content-bg-red-600' : 'content-border-white/30 content-bg-white/20')}>
                              <Icon
                                strokeWidth={1.5}
                                className="content-size-6 content-text-white"
                              />
                            </div>
                          </div>

                          {/* Content Type Badge */}
                          <div className="content-absolute content-bottom-2 content-left-2">
                            <div className="content-flex content-items-center content-gap-1.5 content-rounded-full content-border content-border-white/20 content-bg-black/50 content-px-2 content-py-1 content-backdrop-blur-sm">
                              <Icon className="content-size-3 content-text-white" />
                              <span className="content-text-[10px] content-font-medium content-uppercase content-tracking-wide content-text-white">
                                {content?.contentType.replace('_', ' ')}
                              </span>
                            </div>
                          </div>
                        </div>

                        {/* Content Info */}
                        <div
                          className={cn(
                            'content-h-full content-bg-gradient-to-br content-from-white/10 content-to-white/5 content-p-3',
                            content?.done && 'content-bg-primary/20',
                          )}
                        >
                          <p
                            className={cn(
                              'content-line-clamp-2 content-text-sm content-font-semibold content-leading-tight content-transition-colors content-duration-300',
                              content?.done
                                ? 'content-text-primary'
                                : 'content-text-white',
                              hoveredCard === content?.contentId &&
                              'content-text-primary',
                            )}
                          >
                            {content?.title}
                          </p>

                          {/* Progress Indicator for Active Content */}
                          {isActive && (
                            <div className="content-mt-2 content-border-t content-border-white/10 content-pt-2">
                              <div className="content-flex content-items-center content-gap-1.5 content-text-xs content-text-primary">
                                <div className="content-size-1.5 content-animate-pulse content-rounded-full content-bg-primary" />
                                <span className="content-font-medium">
                                  Currently viewing
                                </span>
                              </div>
                            </div>
                          )}
                        </div>

                        {/* Hover Glow Effect */}
                        <div
                          className={cn(
                            'content-pointer-events-none content-absolute content-inset-0 content-transition-opacity content-duration-300',
                            hoveredCard === content?.contentId
                              ? 'content-opacity-100'
                              : 'content-opacity-0',
                          )}
                        >
                          <div className="content-absolute content-inset-0 content-bg-gradient-to-tr content-from-primary/10 content-via-transparent content-to-primary/5" />
                        </div>
                      </div>
                    );
                  })}
                </div>
              </div>
            )}
          </div>
        );
      })}
    </Fragment>
  );
};

export default UserChaptersCardView;
