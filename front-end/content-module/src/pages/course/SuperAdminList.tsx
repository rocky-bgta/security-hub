import clsx from 'clsx';
import { Fragment, useEffect, useState } from 'react';
import { IoAdd, IoSearchSharp } from 'react-icons/io5';
import { Link } from 'react-router-dom';
import { toast } from 'react-toastify';

import { DeleteIcon, EditIcon, ViewIcon } from 'assets/icons';
import { Button } from 'common/Button';
import { Checkbox } from 'common/Checkbox';
import { Input } from 'common/Input';
import Pagination from 'common/Pagination';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from 'common/Table';
import BulkAction from 'components/BulkAction';
import ConfirmDialog from 'components/ConfirmDialog';
import CourseModal from 'features/course/CourseModal';
import CourseViewModal from 'features/course/CourseViewModal';
import { useAPI } from 'hooks/UseAPI';
import useDebounce from 'hooks/UseDebounce';
import { ICourse } from 'models/Course';
import { IGetListParams, IList, IResponse, Status } from 'models/Global';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { routes } from 'routes/Routes';
import { InitGetListParams } from 'utils/Constants';
import {
  HumanizeDate,
  isSuccessResponse,
  objectToQueryString,
} from 'utils/Helper';

enum ModalType {
  General = 'general',
  View = 'view',
  None = 'none',
}

enum ConfirmDialogType {
  None = 'none',
  Delete = 'delete',
  BulkEnable = 'bulk_enable',
  BulkDisable = 'bulk_disable',
  BulkDelete = 'bulk_delete',
  BulkExport = 'bulk_export',
}

const SuperAdminCourseList = ({ hostPath = routes }) => {
  const [data, setData] = useState<IList<ICourse>>({
    ...InitGetListParams,
    total: 0,
    items: [],
  });
  const [selectedCourses, setSelectedCourses] = useState<Array<string>>([]);

  const [queryString, setQueryString] = useState<string>('');
  const [queryParams, setQueryParams] = useState<IGetListParams>({
    ...InitGetListParams,
    search: '',
  });

  const [showModal, setShowModal] = useState<ModalType>(ModalType.None);
  const [dialogType, setDialogType] = useState<ConfirmDialogType>(
    ConfirmDialogType.None,
  );
  const [submitting, setSubmitting] = useState<boolean>(false);
  const [courseId, setCourseId] = useState<string>('');
  const [loading, setLoading] = useState<boolean>(true);
  const searchDebounce = useDebounce(queryString, 1000);

  const apiClient = useAPI();

  useEffect(() => {
    const resp = objectToQueryString(queryParams);
    setQueryString(resp);
  }, [queryParams]);

  useEffect(() => {
    if (searchDebounce) {
      fetchCourseData();
    }
  }, [searchDebounce]);

  const DialogMap = {
    [ConfirmDialogType.None]: {
      message: '',
      loadingText: '',
      onConfirm: () => {},
    },
    [ConfirmDialogType.Delete]: {
      message: 'Are you sure you want to delete this topic?',
      loadingText: 'Deleting...',
      onConfirm: () => handleDeleteCourse(),
    },
    [ConfirmDialogType.BulkEnable]: {
      message: 'Are you sure you want to enable the selected topics?',
      loadingText: 'Enabling...',
      onConfirm: () => handleBulkEnable(),
    },
    [ConfirmDialogType.BulkDisable]: {
      message: 'Are you sure you want to disable the selected topics?',
      loadingText: 'Disabling...',
      onConfirm: () => handleBulkDisable(),
    },
    [ConfirmDialogType.BulkDelete]: {
      message: 'Are you sure you want to delete the selected topics?',
      loadingText: 'Deleting...',
      onConfirm: () => handleBulkDelete(),
    },
    [ConfirmDialogType.BulkExport]: {
      message: 'Are you sure you want to export the selected topics?',
      loadingText: 'Exporting...',
      onConfirm: () => handleExportCSV(),
    },
  };

  const fetchCourseData = async () => {
    try {
      const response: IResponse<IList<ICourse>> = await apiClient.get(
        API_END_POINTS.COURSE_LIST + queryString,
      );
      setData({
        ...InitGetListParams,
        total: response.data.total,
        items: response.data.items,
      });
    } catch (error) {
      console.error('Error fetching course data:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleSelectAll = () => {
    if (selectedCourses.length === data.items.length) {
      setSelectedCourses([]);
    } else {
      setSelectedCourses(data.items.map(course => course.id));
    }
  };

  const isAllSelected =
    selectedCourses.length === data.items.length && data.items.length > 0;

  const columns = [
    {
      key: 'select',
      header: (
        <Checkbox checked={isAllSelected} onCheckedChange={handleSelectAll} />
      ),
      render: (course: any) => (
        <Checkbox
          checked={selectedCourses.includes(course.id)}
          onCheckedChange={() => handleSelectCourse(course.id)}
        />
      ),
    },
    {
      key: 'courseName',
      header: 'Name',
      tdClassName: 'content-w-[18%] content-text-left',
      render: (course: any) => (
        <Link
          className="hover:content-underline"
          to={hostPath.courseChapters.path.replace(':slug', course.id)}
          state={{ courseName: course.courseName }}
        >
          {course.courseName}
        </Link>
      ),
    },
    {
      key: 'courseDescription',
      header: 'Description',
      tdClassName: 'content-w-[24%] content-text-left',
      render: (course: any) => {
        const description = course.courseDescription || '';
        if (typeof description === 'object') {
          return '';
        }
        const words = description.split(' ');
        return words.length > 12
          ? words.slice(0, 12).join(' ') + '...'
          : description;
      },
    },
    {
      key: 'status',
      header: 'Status',
      render: (course: any) => (
        <span
          className={clsx(
            'content-rounded content-px-2 content-py-1',
            course.courseStatus === 'ENABLED'
              ? 'content-bg-success content-bg-opacity-10 content-text-success'
              : 'content-bg-vibrant-red content-bg-opacity-10 content-text-vibrant-red',
          )}
        >
          {course.courseStatus === 'ENABLED' ? 'Enabled' : 'Disabled'}
        </span>
      ),
    },
    {
      key: 'products',
      header: 'Products',
      render: (row: any) => row?.productIds?.length,
    },
    {
      key: 'chapter',
      header: 'chapters',
      render: (row: any) => row?.chapterIds?.length,
    },
    {
      key: 'createdAt',
      header: 'Created Date',
      render: (row: any) => HumanizeDate(row.createdAt),
    },
    {
      key: 'updatedAt',
      header: 'Updated Date',
      render: (row: any) => HumanizeDate(row.updatedAt),
    },
    {
      key: 'action',
      header: 'Action',
      render: (course: any) => (
        <div className="content-flex content-justify-center content-gap-x-4">
          <Button
            onClick={() => handleOpenModal(ModalType.View, course.id)}
            className="content-bg-transparent content-p-0 hover:!content-bg-transparent"
          >
            <ViewIcon width={24} height={24} />
          </Button>
          <Button
            onClick={() => handleOpenModal(ModalType.General, course.id)}
            className="content-bg-transparent content-p-0 hover:!content-bg-transparent"
          >
            <EditIcon width={20} height={20} />
          </Button>
          <Button
            onClick={() => handleClickDelete(course.id)}
            className="content-bg-transparent content-p-0 hover:!content-bg-transparent"
          >
            <DeleteIcon width={20} height={20} />
          </Button>
        </div>
      ),
    },
  ];

  const handleClickDelete = (id: string) => {
    setCourseId(id);
    setDialogType(ConfirmDialogType.Delete);
  };

  const handleDeleteCourse = async () => {
    setSubmitting(true);

    try {
      const response = await apiClient.del(
        API_END_POINTS.COURSE_DELETE + courseId,
      );
      if (!isSuccessResponse(response.statusCode)) {
        throw new Error(response.message);
      }

      toast.success(response.message);
      fetchCourseData();
    } catch (error: any) {
      console.error('Error delete chapter:', error);
      toast.error(error.message);
    } finally {
      setSubmitting(false);
      setDialogType(ConfirmDialogType.None);
      setCourseId('');
    }
  };

  const handleSelectCourse = (id: string) => {
    setSelectedCourses(prevSelected =>
      prevSelected.includes(id)
        ? prevSelected.filter(userId => userId !== id)
        : [...prevSelected, id],
    );
  };

  const onPageChangeHandler = (page: number) => {
    setQueryParams(prevState => ({
      ...prevState,
      offset: page - 1,
    }));
  };

  const onPageLimitChangeHandler = (limit: number) => {
    setQueryParams(prevState => ({ ...prevState, limit, offset: 0 }));
  };

  const handleOpenModal = (content: ModalType, id?: string) => {
    setShowModal(content);
    if (id) setCourseId(id);
  };

  const handleHideModal = () => {
    setShowModal(ModalType.None);
    if (courseId) setCourseId('');
  };

  const handleSubmit = () => fetchCourseData();

  const handleBulkEnable = async () => {
    setSubmitting(true);

    try {
      const response = await apiClient.put(API_END_POINTS.COURSE_BULK_UPDATE, {
        data: { ids: selectedCourses, status: Status.ENABLED },
      });
      if (!isSuccessResponse(response.statusCode)) {
        throw new Error(response.message);
      }

      toast.success(response.message);
      setSelectedCourses([]);
      fetchCourseData();
    } catch (error: any) {
      console.error('Error enabling courses:', error);
      toast.error(error.message);
    } finally {
      setSubmitting(false);
      setDialogType(ConfirmDialogType.None);
    }
  };

  const handleBulkDisable = async () => {
    setSubmitting(true);

    try {
      const response = await apiClient.put(API_END_POINTS.COURSE_BULK_UPDATE, {
        data: { ids: selectedCourses, status: Status.DISABLED },
      });
      if (!isSuccessResponse(response.statusCode)) {
        throw new Error(response.message);
      }

      toast.success(response.message);
      setSelectedCourses([]);
      fetchCourseData();
    } catch (error: any) {
      console.error('Error disabling courses:', error);
      toast.error(error.message);
    } finally {
      setSubmitting(false);
      setDialogType(ConfirmDialogType.None);
    }
  };

  const handleBulkDelete = async () => {
    setSubmitting(true);

    try {
      const response = await apiClient.del(API_END_POINTS.COURSE_BULK_DELETE, {
        data: {
          ids: selectedCourses,
        },
      });
      if (!isSuccessResponse(response.statusCode)) {
        throw new Error(response.message);
      }

      toast.success(response.message);
      setSelectedCourses([]);
      fetchCourseData();
    } catch (error: any) {
      console.error('Error delete courses:', error);
      toast.error(error.message);
    } finally {
      setSubmitting(false);
      setDialogType(ConfirmDialogType.None);
    }
  };

  const handleExportCSV = async () => {
    setSubmitting(true);

    try {
      const response = await apiClient.post(API_END_POINTS.COURSE_BULK_EXPORT, {
        data: { ids: selectedCourses },
      });
      const blob = new Blob([response], { type: 'text/csv' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'courses.csv';
      a.click();
    } catch (error) {
      console.error('Error exporting CSV:', error);
    } finally {
      setSubmitting(false);
      setDialogType(ConfirmDialogType.None);
    }
  };

  const handleSearch = () => fetchCourseData();

  return (
    <Fragment>
      <section>
        <div className="content-mb-6 content-flex content-justify-between content-text-secondary">
          <h4 className="content-font-medium">
            Topic List {data.total > 0 && `(${data.total})`}
          </h4>
          <div className="content-flex content-gap-x-5">
            <Button
              onClick={() => handleOpenModal(ModalType.General)}
              className="content-flex content-items-center content-gap-x-2 content-rounded content-bg-primary hover:!content-bg-primary"
              size="sm"
            >
              <IoAdd className="content-text-2xl" /> Create Topic
            </Button>
            <div className="content-flex content-text-secondary">
              <Input
                id="search"
                placeholder="Search"
                className="content-rounded-r-none content-border-soft-blue-gray content-px-2 content-py-1"
                value={queryParams.search}
                onChange={e =>
                  setQueryParams(prevState => ({
                    ...prevState,
                    search: e.target.value,
                  }))
                }
              />
              <Button className="content-p-0" onClick={handleSearch}>
                <IoSearchSharp className="content-h-auto content-w-10 content-rounded-r content-bg-primary content-p-2 content-text-3xl content-text-white" />
              </Button>
            </div>
          </div>
        </div>
        {selectedCourses.length > 0 && (
          <BulkAction
            selectedIds={selectedCourses}
            onEnable={() => setDialogType(ConfirmDialogType.BulkEnable)}
            onDisable={() => setDialogType(ConfirmDialogType.BulkDisable)}
            onDelete={() => setDialogType(ConfirmDialogType.BulkDelete)}
            onExportCSV={() => setDialogType(ConfirmDialogType.BulkExport)}
          />
        )}
        <div>
          {loading ? (
            <p>Loading</p>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  {columns.map(column => (
                    <TableHead key={column.key}>{column.header}</TableHead>
                  ))}
                </TableRow>
              </TableHeader>
              <TableBody>
                {data.items.map((row, index) => (
                  <TableRow key={index}>
                    {columns.map(column => (
                      <TableCell key={column.key}>
                        {column.render
                          ? column.render(row)
                          : (row as any)[column.key]}
                      </TableCell>
                    ))}
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}

          <div className="content-my-10 content-flex content-justify-end">
            <Pagination
              total={data?.total}
              perPage={data?.pageSize}
              onPageChange={onPageChangeHandler}
            />
          </div>
        </div>
      </section>

      <CourseModal
        topicId={courseId}
        isOpen={showModal === ModalType.General}
        onClose={handleHideModal}
        onSubmit={handleSubmit}
      />

      <CourseViewModal
        isOpen={showModal === ModalType.View}
        onClose={handleHideModal}
        topicId={courseId}
      />

      <ConfirmDialog
        isOpen={dialogType !== ConfirmDialogType.None}
        message={DialogMap[dialogType]?.message}
        loading={submitting}
        loadingText={DialogMap[dialogType]?.loadingText}
        onClose={() => setDialogType(ConfirmDialogType.None)}
        onConfirm={DialogMap[dialogType]?.onConfirm}
      />
    </Fragment>
  );
};

export default SuperAdminCourseList;
