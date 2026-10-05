import clsx from 'clsx';
import { Fragment, MouseEvent, useEffect, useState } from 'react';
import { FiPlus } from 'react-icons/fi';
import { IoIosArrowDown } from 'react-icons/io';
import { useParams } from 'react-router-dom';
import { toast } from 'react-toastify';

import { CertificateViewIcon, DeleteIcon, EditIcon } from 'assets/icons';
import { Button } from 'common/Button';
import ConfirmDialog from 'components/ConfirmDialog';
import ChapterModal from 'features/chapter/ChapterModal';
import ChapterViewModal from 'features/chapter/ChapterViewModal';
import ChapterWiseContents from 'features/content/ChapterWiseContents';
import { useAPI } from 'hooks/UseAPI';
import { IChapter } from 'models/Chapter';
import { IList, IResponse, Status } from 'models/Global';
import { MdContentPasteOff } from 'react-icons/md';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { InitGetListParams } from 'utils/Constants';
import { isSuccessResponse, objectToQueryString } from 'utils/Helper';

enum ModalType {
  NONE = 'none',
  GENERAL = 'general',
  VIEW = 'view',
}

const CourseChapters = () => {
  const { slug } = useParams();
  const [data, setData] = useState<IList<IChapter>>({
    ...InitGetListParams,
    total: 0,
    items: [],
  });
  const [queryString, setQueryString] = useState<string>('');
  const [selectedChapter, setSelectedChapter] = useState<IChapter | null>(null);

  const [modalData, setModalData] = useState<IChapter | null>(null);
  const [modalType, setModalType] = useState<ModalType>(ModalType.NONE);
  const [openConfirmDialog, setOpenConfirmDialog] = useState<boolean>(false);
  const [submitting, setSubmitting] = useState<boolean>(false);

  const apiClient = useAPI();

  useEffect(() => {
    const resp = objectToQueryString({
      ...InitGetListParams,
      pageSize: 100,
      topicId: slug as string,
    });
    setQueryString(resp);
  }, [slug]);

  useEffect(() => {
    if (queryString) {
      fetchChapterList();
    }
  }, [queryString]);

  const fetchChapterList = async () => {
    try {
      const response: IResponse<IList<IChapter>> = await apiClient.get(
        API_END_POINTS.CHAPTER_LIST + queryString,
      );
      setData({
        ...InitGetListParams,
        total: response.data.total,
        items: response.data.items,
      });
      setSelectedChapter(response.data.items[0]);
    } catch (error) {
      console.error('Error fetching chapter data:', error);
    }
  };

  const handleViewChapter = (
    e: MouseEvent<HTMLButtonElement>,
    chapter: IChapter,
  ) => {
    e.preventDefault();
    e.stopPropagation();

    setModalData(chapter);
    setModalType(ModalType.VIEW);
  };

  const handleEditChapter = (
    e: MouseEvent<HTMLButtonElement>,
    chapter: IChapter,
  ) => {
    e.preventDefault();
    e.stopPropagation();

    setModalData(chapter);
    setModalType(ModalType.GENERAL);
  };

  const handleDeleteChapter = (
    e: MouseEvent<HTMLButtonElement>,
    id: string,
  ) => {
    e.preventDefault();
    e.stopPropagation();

    setModalData(data.items.find(item => item.id === id)!);
    setOpenConfirmDialog(true);
  };

  const handleDeleteConfirm = async () => {
    setSubmitting(true);

    try {
      const response: IResponse<IChapter> = await apiClient.del(
        API_END_POINTS.CHAPTER_DELETE + modalData?.id,
      );

      if (!isSuccessResponse(response.statusCode)) {
        throw new Error(response.message);
      }

      toast.success(response.message);
      fetchChapterList();

      setModalData(null);
      setSelectedChapter(null);
      handleClose();
    } catch (error: any) {
      console.error('Error delete chapter:', error);
      toast.error(error.message);
    } finally {
      setSubmitting(false);
    }
  };

  const handleClose = () => setOpenConfirmDialog(false);

  const handleOpenModal = () => setModalType(ModalType.GENERAL);

  const handleHideModal = () => {
    setModalType(ModalType.NONE);
    setModalData(null);
  };

  const handleToggleShowChapterContent = (chapter: IChapter) =>
    setSelectedChapter(selectedChapter === chapter ? null : chapter);

  return (
    <Fragment>
      <div className="content-mb-4 content-mt-6 content-flex content-flex-col content-gap-3 content-justify-between sm:content-mb-5 sm:content-mt-8 sm:content-flex-row sm:content-items-start">
        <p className="content-text-base content-font-medium content-text-white sm:content-text-lg">
          Chapters
        </p>
        {data.items.length >= 1 && (
          <Button size="sm" onClick={handleOpenModal}>
            <span>
              <FiPlus />
            </span>
            Create New Chapter
          </Button>
        )}
      </div>

      {data.items.length === 0 ? (
        <div className="content-flex content-flex-col content-items-center content-justify-center content-gap-y-6">
          <MdContentPasteOff className="content-mt-20 content-text-9xl content-text-primary" />
          <p className="content-mt-6 content-text-center content-text-stormy-gray">
            Click the plus button above Kid to begin adding content blocks
          </p>
          <Button
            size="sm"
            onClick={handleOpenModal}
            className="content-mx-auto content-mt-7"
          >
            <span>
              <FiPlus className="content-text-xl" />
            </span>
            Create New Chapter
          </Button>
        </div>
      ) : (
        <div className="content-flex content-flex-col content-space-y-2">
          {data.items.map(chapter => (
            <div
              key={chapter.id}
              className="content-border content-border-card-border"
            >
              <div
                onClick={_ => handleToggleShowChapterContent(chapter)}
                className="content-flex content-w-full content-cursor-pointer content-flex-col content-items-start content-justify-between content-gap-3 content-bg-white content-bg-opacity-25 content-px-4 content-py-3 content-text-left sm:content-flex-row sm:content-items-center sm:content-px-6 sm:content-py-4"
              >
                <div className="content-flex content-items-center content-space-x-2">
                  <IoIosArrowDown
                    className={clsx(
                      'content-size-5 content-text-white content-transition-transform',
                      chapter.id === selectedChapter?.id
                        ? 'content-rotate-180 content-transform'
                        : '',
                    )}
                  />
                  <span className="content-text-sm content-font-medium content-text-white sm:content-text-base">
                    {chapter.chapterName}
                  </span>
                </div>
                <div className="content-flex content-flex-wrap content-items-center content-gap-3 sm:content-gap-4">
                  <span
                    className={clsx(
                      'content-rounded-full content-px-3 content-py-1 content-text-sm',
                      chapter.chapterStatus === Status.ENABLED
                        ? 'content-bg-success content-text-white'
                        : 'content-bg-red-100 content-text-red-700',
                    )}
                  >
                    {chapter.chapterStatus === Status.ENABLED
                      ? 'Enabled'
                      : 'Disabled'}
                  </span>
                  <Button
                    className="!content-bg-transparent content-p-0"
                    onClick={e => handleViewChapter(e, chapter)}
                  >
                    <CertificateViewIcon />
                  </Button>
                  <Button
                    className="!content-bg-transparent content-p-0"
                    onClick={e => handleEditChapter(e, chapter)}
                  >
                    <EditIcon stroke="#fff" width={22} />
                  </Button>
                  <Button
                    className="!content-bg-transparent content-p-0"
                    onClick={e => handleDeleteChapter(e, chapter.id)}
                  >
                    <DeleteIcon fill="#fff" />
                  </Button>
                </div>
              </div>

              {chapter.id === selectedChapter?.id && (
                <ChapterWiseContents chapterId={chapter.id} />
              )}
            </div>
          ))}
        </div>
      )}

      <ChapterViewModal
        isOpen={modalType === ModalType.VIEW}
        onClose={handleHideModal}
        data={modalData}
      />

      <ChapterModal
        isOpen={modalType === ModalType.GENERAL}
        onSubmit={fetchChapterList}
        onClose={handleHideModal}
        topicId={slug as string}
        selectedChapter={modalData}
        chapterLength={data.items.length}
      />

      <ConfirmDialog
        isOpen={openConfirmDialog}
        message="Are you sure you want to delete this chapter?"
        loading={submitting}
        loadingText="Deleting..."
        onClose={handleClose}
        onConfirm={handleDeleteConfirm}
      />
    </Fragment>
  );
};

export default CourseChapters;
