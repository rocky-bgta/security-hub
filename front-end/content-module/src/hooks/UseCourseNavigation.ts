import { IUserChapter } from 'models/Course';
import { Dispatch, SetStateAction, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { routes } from 'routes/Routes';

type CourseData = {
  chapters: IUserChapter[];
};

export const useCourseNavigation = (
  courseData: CourseData | undefined,
  packageId: string | undefined,
  contentId: string | undefined,
  slug: string | undefined,
  setOpenBox: Dispatch<SetStateAction<string>>,
) => {
  const navigate = useNavigate();

  const getCurrentPosition = useCallback(() => {
    if (!courseData?.chapters || !contentId) return null;

    for (let cIdx = 0; cIdx < courseData.chapters.length; cIdx++) {
      const chapter = courseData.chapters[cIdx];
      const idx = chapter.contents.findIndex(c => c.contentId === contentId);
      if (idx !== -1) return { chapterIndex: cIdx, contentIndex: idx };
    }

    return null;
  }, [courseData, contentId]);

  const isLastContent = useCallback(() => {
    const position = getCurrentPosition();
    if (!position || !courseData?.chapters) return false;

    const { chapterIndex, contentIndex } = position;
    const lastChapterIndex = courseData.chapters.length - 1;
    const lastContentIndex =
      courseData.chapters[lastChapterIndex].contents.length - 1;

    return (
      chapterIndex === lastChapterIndex && contentIndex === lastContentIndex
    );
  }, [getCurrentPosition, courseData]);

  const isFirstContent = useCallback(() => {
    const position = getCurrentPosition();
    if (!position || !courseData?.chapters) return false;

    const { chapterIndex, contentIndex } = position;
    return chapterIndex === 0 && contentIndex === 0;
  }, [getCurrentPosition, courseData]);

  const goToNextContent = useCallback(() => {
    const position = getCurrentPosition();
    if (!position || !courseData?.chapters) return;

    const { chapterIndex, contentIndex } = position;

    const currentChapter = courseData.chapters[chapterIndex];

    if (contentIndex < currentChapter.contents.length - 1) {
      const nextContent = currentChapter.contents[contentIndex + 1];
      navigate(
        routes.contentDetails.path
          .replace(':packageId', packageId as string)
          .replace(':slug', slug as string)
          .replace(':contentId', nextContent.contentId),
      );
      return;
    }

    const nextChapter = courseData.chapters[chapterIndex + 1];
    if (nextChapter && nextChapter.contents.length > 0) {
      const nextContent = nextChapter.contents[0];
      setOpenBox(nextChapter.chapterId);
      navigate(
        routes.contentDetails.path
          .replace(':packageId', packageId as string)
          .replace(':slug', slug as string)
          .replace(':contentId', nextContent.contentId),
      );
    }
  }, [getCurrentPosition, courseData, navigate, packageId, slug, setOpenBox]);

  const goToPreviousContent = useCallback(() => {
    const position = getCurrentPosition();
    if (!position || !courseData?.chapters) return;

    const { chapterIndex, contentIndex } = position;

    const currentChapter = courseData.chapters[chapterIndex];

    if (contentIndex > 0) {
      const prevContent = currentChapter.contents[contentIndex - 1];
      navigate(
        routes.contentDetails.path
          .replace(':slug', slug as string)
          .replace(':contentId', prevContent.contentId),
      );
      return;
    }

    const prevChapter = courseData.chapters[chapterIndex - 1];
    if (prevChapter && prevChapter.contents.length > 0) {
      setOpenBox(prevChapter.chapterId);
      const prevContent = prevChapter.contents[prevChapter.contents.length - 1];
      navigate(
        routes.contentDetails.path
          .replace(':slug', slug as string)
          .replace(':contentId', prevContent.contentId),
      );
    }
  }, [getCurrentPosition, courseData, navigate, slug, setOpenBox]);

  return {
    getCurrentPosition,
    isFirstContent,
    isLastContent,
    goToNextContent,
    goToPreviousContent,
  };
};
