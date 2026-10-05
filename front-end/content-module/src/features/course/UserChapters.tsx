import { Dispatch, Fragment, SetStateAction, useEffect } from 'react';
import { IoIosArrowDown } from 'react-icons/io';
import { IoCheckmarkSharp } from 'react-icons/io5';
import { useNavigate, useParams } from 'react-router-dom';
import { ArrowRight, BookOpen } from 'lucide-react';

import ContentIcon from 'components/ContentIcon';
import { IUserChapter } from 'models/Course';
import { routes } from 'routes/Routes';
import { cn } from 'utils/Helper';

interface IProps {
  chapters: Array<IUserChapter>;
  courseId: string;
  openBox: string;
  setOpenBox: Dispatch<SetStateAction<string>>;
  packageId?: string;
  onContentSelect?: () => void;
}

const UserChapters = ({
  chapters,
  courseId,
  openBox,
  setOpenBox,
  packageId,
  onContentSelect,
}: IProps) => {
  const { contentId } = useParams();
  const navigate = useNavigate();

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
  }, [chapters, contentId, setOpenBox]);

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
            className="content-rounded content-border content-border-card-border"
          >
            <div
              onClick={() => handleOpen(chapter.chapterId)}
              className="content-relative content-flex content-w-full content-cursor-pointer content-items-center content-justify-between content-bg-white/10 content-p-3 content-text-left content-transition-all content-duration-300 hover:content-bg-white/15"
            >
              <div className="content-min-w-0 content-flex-1">
                <div className="content-flex content-min-w-0 content-items-center content-gap-3">
                  <div className="content-flex content-size-8 content-shrink-0 content-items-center content-justify-center content-rounded-lg content-bg-gradient-to-br content-from-primary/80 content-to-primary content-shadow-md">
                    <BookOpen className="content-size-4 content-text-white" />
                  </div>
                  <div className="content-min-w-0 content-flex-1">
                    <p className="content-mb-1 content-line-clamp-2 content-text-base content-font-semibold content-text-white sm:content-text-lg">
                      {chapter?.chapterName}
                    </p>
                    <div className="content-flex content-flex-wrap content-items-center content-gap-x-3 content-gap-y-1 content-text-sm">
                      <span className="content-text-sm content-text-white/70">
                        {chapter?.contents?.length || 0} lessons
                      </span>
                      <div className="content-flex content-items-center content-gap-2">
                        <div className="content-h-1.5 content-w-12 content-overflow-hidden content-rounded-full content-bg-white/20 sm:content-w-16">
                          <div
                            className="content-h-full content-bg-gradient-to-r content-from-primary content-to-primary/80 content-transition-all content-duration-500"
                            style={{ width: `${completionPercentage}%` }}
                          />
                        </div>
                        <span className="content-text-sm content-text-primary">
                          {completionPercentage}%
                        </span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <IoIosArrowDown
                className={cn(
                  'content-h-4 content-w-4 content-shrink-0 content-text-white content-transition-all content-duration-300',
                  isOpen ? 'content-rotate-180' : '',
                )}
              />
            </div>

            {chapter?.chapterId === openBox && (
              <div className="content-space-y-4 content-p-4 sm:content-space-y-6 sm:content-p-6">
                {chapter?.contents?.map(content => {
                  const isOpen = content?.contentId === contentId;
                  const backgroundClass = isOpen
                    ? 'content-bg-[#F7C948] '
                    : content?.done
                      ? 'content-bg-[#13CD9C]'
                      : 'content-bg-white content-bg-opacity-25';

                  const textClass = isOpen
                    ? '!content-text-white'
                    : content?.done
                      ? '!content-text-white !content-text-opacity-50'
                      : 'content-text-white content-text-opacity-75';

                  return (
                    <div
                      key={content?.contentId}
                      onClick={() => {
                        onContentSelect?.();
                        navigate(
                          routes.contentDetails.path
                            .replace(':packageId', packageId || '')
                            .replace(':slug', courseId)
                            .replace(':contentId', content?.contentId),
                        );
                      }}
                    >
                      <div
                        className={cn(
                          'content-relative content-flex content-cursor-pointer content-items-center content-gap-2',
                          textClass,
                        )}
                      >
                        {/* {!isLast && (
                        <div className="-content-z-1 content-absolute content-left-[17px] content-top-10 content-min-h-12 content-w-px content-border-l content-border-dashed content-border-gray-500"></div>
                      )} */}
                        <span
                          className={cn(
                            'content-shrink-0 content-rounded-full content-p-2',
                            backgroundClass,
                          )}
                        >
                          {content?.done ? (
                            <IoCheckmarkSharp className="content-size-5 content-text-white" />
                          ) : (
                            <ContentIcon type={content?.contentType} />
                          )}
                        </span>
                        <span className="content-min-w-0 content-break-words">
                          {content?.title}
                        </span>
                      </div>
                      {content?.contentList?.length > 0 && (
                        <div className="content-mt-3 content-rounded-xl content-border content-border-gray-700 content-bg-gray-800/40 content-p-4">
                          <ul className="content-space-y-2">
                            {content?.contentList?.map(subContent => (
                              <li
                                key={subContent?.id}
                                className="content-flex content-items-start content-gap-x-3 content-pb-2 last:content-pb-0"
                              >
                                <div className="content-flex content-size-3 content-items-center content-justify-center">
                                  <ArrowRight className="content-mt-3 content-size-3 content-text-gray-300" />
                                </div>
                                <span className="content-text-sm content-text-gray-100">
                                  {subContent?.contentName}
                                </span>
                              </li>
                            ))}
                          </ul>
                        </div>
                      )}
                    </div>
                  );
                })}
              </div>
            )}
          </div>
        );
      })}
    </Fragment>
  );
};
export default UserChapters;
