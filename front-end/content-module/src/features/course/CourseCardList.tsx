import { useCallback, useEffect, useState } from 'react';
import { IoAdd } from 'react-icons/io5';
import { Filter, RotateCcw } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

import { Button } from 'common/Button';
import Pagination from 'common/Pagination';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import ContentBox from 'components/ContentBox';
import FilterControls from 'components/FilterControls';
import CourseModal from 'features/course/CourseModal';
import CourseViewModal from 'features/course/CourseViewModal';
import { useAPI } from 'hooks/UseAPI';
import useDebounce from 'hooks/UseDebounce';
import {
  ICategory,
  ICompliance,
  IContentType,
  ICountry,
} from 'models/Configuration';
import { CourseStatus, IResponse, Status } from 'models/Global';
import { IFilterData } from 'models/Filter';
import { ISelectOption } from 'models/Input';
import { ITopicResponse } from 'models/Topic';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { routes } from 'routes/Routes';
import { Card } from 'common/Card';
import { Label } from 'common/Label';
import TopicCardLoader from 'components/skeleton/TopicCard';

enum ModalType {
  General = 'general',
  View = 'view',
  None = 'none',
  Edit = 'edit',
}

interface IProps {
  hostPath: typeof routes;
}

const AspireCourseCardList = ({ hostPath }: IProps) => {
  const navigate = useNavigate();
  const apiClient = useAPI();

  const [topics, setTopics] = useState<ITopicResponse>({
    topics: [],
    totalElements: 0,
    totalPages: 0,
    currentPage: 0,
    hasNext: false,
    hasPrevious: false,
  });

  const [filterData, setFilterData] = useState<IFilterData>({
    countryIds: [],
    complianceIds: [],
    categoryIds: [],
    contentTypeIds: [],
    durationMinutes: [],
  });
  const [search, setSearch] = useState<string>('');
  const searchDebounced = useDebounce(search, 1000);
  const [status, setStatus] = useState<CourseStatus>('' as CourseStatus);
  const [queryParams, setQueryParams] = useState({ page: 1, size: 10 });

  const [categoryOptions, setCategoryOptions] = useState<ISelectOption[]>([]);
  const [contentTypeOptions, setContentTypeOptions] = useState<ISelectOption[]>(
    [],
  );
  const [complianceOptions, setComplianceOptions] = useState<ISelectOption[]>(
    [],
  );
  const [countryOptions, setCountryOptions] = useState<ISelectOption[]>([]);

  const [showModal, setShowModal] = useState<ModalType>(ModalType.None);
  const [loading, setLoading] = useState<boolean>(true);
  const [topicId, setTopicId] = useState<string>('');

  useEffect(() => {
    const fetchOptions = async () => {
      try {
        const [categoryRes, contentTypeRes, complianceRes, countryRes] =
          await Promise.all([
            apiClient.get(API_END_POINTS.CATEGORY_LIST),
            apiClient.get(API_END_POINTS.CONTENT_TYPE_LIST),
            apiClient.get(API_END_POINTS.COMPLIANCE_LIST),
            apiClient.get(API_END_POINTS.COUNTRY_LIST),
          ]);

        setCategoryOptions(
          categoryRes.data?.map((item: ICategory) => ({
            id: item.id,
            label: item.categoryName,
            value: item.id,
          })) || [],
        );

        setContentTypeOptions(
          contentTypeRes.data?.map((item: IContentType) => ({
            id: item.id,
            label: item.typeName,
            value: item.id,
          })) || [],
        );

        setComplianceOptions(
          complianceRes.data?.map((item: ICompliance) => ({
            id: item.id,
            label: item.complianceName,
            value: item.id,
          })) || [],
        );

        setCountryOptions(
          countryRes.data?.map((item: ICountry) => ({
            id: item.id,
            label: item.name,
            value: item.id,
          })) || [],
        );
      } catch (error) {
        console.error('Error fetching filter options:', error);
      }
    };

    fetchOptions();
  }, [apiClient]);

  const fetchCourseData = useCallback(async () => {
    setLoading(true);
    try {
      const response: IResponse<ITopicResponse> = await apiClient.post(
        API_END_POINTS.TOPIC_FILTER,
        {
          data: {
            countryIds: filterData.countryIds.map(item => item.id),
            complianceIds: filterData.complianceIds.map(item => item.id),
            categoryIds: filterData.categoryIds.map(item => item.id),
            contentTypeId: filterData.contentTypeIds.map(item => item.id),
            durationRanges: filterData.durationMinutes,
            payloadTypeIds: [],
            searchText: searchDebounced,
            page: queryParams.page,
            size: queryParams.size,
            ...(status ? { status } : {}),
          },
        },
      );

      setTopics({
        ...response.data,
        topics: Array.isArray(response.data?.topics) ? response.data.topics : [],
      });
    } catch (error) {
      console.error('Error fetching course data:', error);
    } finally {
      setLoading(false);
    }
  }, [apiClient, filterData, searchDebounced, queryParams, status]);

  useEffect(() => {
    fetchCourseData();
  }, [fetchCourseData]);

  const handleFilterChange = useCallback((updates: Partial<IFilterData>) => {
    setFilterData(prev => ({ ...prev, ...updates }));
    setQueryParams(prev => ({ ...prev, page: 1 }));
  }, []);

  const handleSearchChange = (value: string) => {
    setSearch(value);
    setQueryParams(prev => ({ ...prev, page: 1 }));
  };

  const handleStatusChange = (value: string) => {
    setStatus(value === 'ALL' ? ('' as CourseStatus) : (value as CourseStatus));
    setQueryParams(prev => ({ ...prev, page: 1 }));
  };

  const handleResetFilters = () => {
    setFilterData({
      countryIds: [],
      complianceIds: [],
      categoryIds: [],
      contentTypeIds: [],
      durationMinutes: [],
    });
    setSearch('');
    setStatus('' as CourseStatus);
    setQueryParams(prev => ({ ...prev, page: 1 }));
  };

  const onPageChangeHandler = (page: number) => {
    setQueryParams(prev => ({ ...prev, page }));
  };

  const handleOpenModal = (
    content: ModalType,
    id?: string,
    courseName?: string,
  ) => {
    if (id) {
      navigate(hostPath.courseChapters.path.replace(':slug', id), {
        state: { courseName },
      });
      return;
    }
    setShowModal(content);
  };

  const handleOpenCardModal = (content: ModalType, id: string) => {
    setShowModal(content);
    setTopicId(id);
  };

  const handleHideModal = () => {
    setShowModal(ModalType.None);
    if (topicId) setTopicId('');
  };

  const handleSubmit = () => {
    fetchCourseData();
  };

  return (
    <div>
      <section>
        <div className="content-mb-6 content-flex content-items-center content-justify-between">
          <div>
            <h4 className="content-text-3xl content-font-bold content-text-foreground">
              Add Content
            </h4>

            <p className="content-text-muted-foreground">
              Here you can create and manage all your Topics
            </p>
          </div>

          <div>
            <Button
              onClick={() => handleOpenModal(ModalType.General)}
              className="content-text-nowrap content-py-2"
            >
              <IoAdd className="content-text-2xl" /> Create Topic
            </Button>
          </div>
        </div>

        <Card className="content-mb-6 content-p-4">
          <div className="content-mb-3 content-flex content-items-center content-justify-between">
            <div className="content-flex content-items-center content-gap-2 content-text-lg content-font-semibold">
              <Filter width={20} height={20} />
              Filter Topics
            </div>
            <Button variant="secondary" onClick={handleResetFilters}>
              <RotateCcw />
              Reset
            </Button>
          </div>
          <FilterControls
            filterData={filterData}
            onFilterChange={handleFilterChange}
            options={{
              categoryOptions,
              contentTypeOptions,
              complianceOptions,
              countryOptions,
            }}
            search={search}
            setSearch={handleSearchChange}
          />
          <div className="content-col-span-1 content-mt-3">
            <Label htmlFor="status">Status</Label>
            <Select value={status || 'ALL'} onValueChange={handleStatusChange}>
              <SelectTrigger>
                <SelectValue placeholder="Filter by Status" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="ALL">All Status</SelectItem>
                <SelectItem value={CourseStatus.ENABLED}>Enabled</SelectItem>
                <SelectItem value={CourseStatus.DISABLED}>Disabled</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </Card>

        <div className="content-grid content-gap-4 xl:content-grid-cols-4 2xl:content-grid-cols-5">
          {loading ? (
            <TopicCardLoader count={queryParams.size} />
          ) : topics.topics.length > 0 ? (
            topics.topics.map(item => (
              <ContentBox
                key={item.id}
                onClick={() =>
                  handleOpenModal(ModalType.General, item.id, item.topicName)
                }
                title={item.topicName}
                status={item.status === Status.ENABLED ? 'Enabled' : 'Disabled'}
                description={item.description}
                image={item.thumbnailUrl || ''}
                isOpenView={() => handleOpenCardModal(ModalType.View, item.id)}
                isOpenEdit={() => handleOpenCardModal(ModalType.Edit, item.id)}
              />
            ))
          ) : (
            <div className="content-col-span-full content-py-8 content-text-center content-text-muted-foreground">
              No topics found
            </div>
          )}
        </div>

        <div className="content-mt-4">
          <Pagination
            total={topics.totalElements}
            perPage={queryParams.size}
            onPageChange={onPageChangeHandler}
          />
        </div>
      </section>

      {showModal === ModalType.General && (
        <CourseModal
          topicId={topicId}
          isOpen={showModal === ModalType.General}
          onClose={handleHideModal}
          onSubmit={handleSubmit}
        />
      )}

      {showModal === ModalType.View && (
        <CourseViewModal
          isOpen={showModal === ModalType.View}
          onClose={handleHideModal}
          topicId={topicId}
        />
      )}

      {showModal === ModalType.Edit && (
        <CourseModal
          topicId={topicId}
          isOpen={showModal === ModalType.Edit}
          onClose={handleHideModal}
          onSubmit={handleSubmit}
        />
      )}
    </div>
  );
};

export default AspireCourseCardList;
