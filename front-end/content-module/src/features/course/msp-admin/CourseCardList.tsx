import { useCallback, useEffect, useMemo, useState } from 'react';
import { Filter, RotateCcw } from 'lucide-react';

import { Button } from 'common/Button';
import { Card } from 'common/Card';
import { Label } from 'common/Label';
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
import TopicCardLoader from 'components/skeleton/TopicCard';
import CourseViewModal from 'features/course/CourseViewModal';
import { useAPI } from 'hooks/UseAPI';
import useDebounce from 'hooks/UseDebounce';
import { useStore } from 'hooks/UseStore';
import {
  ICategory,
  ICompliance,
  IContentType,
  ICountry,
} from 'models/Configuration';
import { IFilterData } from 'models/Filter';
import { CourseStatus, IGetListParams, IList, IResponse } from 'models/Global';
import { ISelectOption } from 'models/Input';
import { IMspTopic } from 'models/Topic';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { routes } from 'routes/Routes';
import { InitGetListParams } from 'utils/Constants';

enum ModalType {
  View = 'view',
  None = 'none',
}

interface IProps {
  hostPath: typeof routes;
}

interface IMspTopicListPayload {
  filter: {
    searchText: string;
    categoryIds: string[];
    complianceIds: string[];
    countryIds: string[];
    contentTypeId: string[];
    durationRanges: string[];
    tags: string[];
    selectedTopicId: string[];
    status?: CourseStatus;
  };
  search: string;
  offset: number;
  pageSize: number;
  sortBy: string;
  order: string;
}

const buildMspTopicPayload = ({
  filterData,
  search,
  offset,
  pageSize,
  status,
}: {
  filterData: IFilterData;
  search: string;
  offset: number;
  pageSize: number;
  status: CourseStatus;
}): IMspTopicListPayload => ({
  filter: {
    searchText: search,
    categoryIds: filterData.categoryIds.map(item => String(item.id)),
    complianceIds: filterData.complianceIds.map(item => String(item.id)),
    countryIds: filterData.countryIds.map(item => String(item.id)),
    contentTypeId: filterData.contentTypeIds.map(item => String(item.id)),
    durationRanges: filterData.durationMinutes,
    tags: [],
    selectedTopicId: [],
    ...(status ? { status } : {}),
  },
  search,
  offset,
  pageSize,
  sortBy: 'createdAt',
  order: 'desc',
});

const MSPCourseCardList = ({ hostPath: _hostPath }: IProps) => {
  const apiClient = useAPI();
  const { userInfo } = useStore();

  const [topics, setTopics] = useState<IList<IMspTopic>>({
    items: [],
    total: 0,
    offset: 0,
    pageSize: 10,
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
  const [queryParams, setQueryParams] = useState<IGetListParams>({
    ...InitGetListParams,
    search: '',
    offset: 0,
    pageSize: 10,
  });

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

  const requestPayload = useMemo(
    () =>
      buildMspTopicPayload({
        filterData,
        search: searchDebounced,
        offset: queryParams.offset ?? 0,
        pageSize: queryParams.pageSize ?? 10,
        status,
      }),
    [filterData, searchDebounced, queryParams.offset, queryParams.pageSize, status],
  );

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
    if (!userInfo?.userId) return;

    setLoading(true);
    try {
      const response: IResponse<IList<IMspTopic>> = await apiClient.post(
        API_END_POINTS.MSP_PRODUCT_TOPICS.replace(':mspId', userInfo.userId),
        { data: requestPayload },
      );

      setTopics({
        ...response.data,
        items: Array.isArray(response.data?.items) ? response.data.items : [],
      });
    } catch (error) {
      console.error('Error fetching MSP course data:', error);
    } finally {
      setLoading(false);
    }
  }, [apiClient, requestPayload, userInfo?.userId]);

  useEffect(() => {
    fetchCourseData();
  }, [fetchCourseData]);

  const handleFilterChange = useCallback((updates: Partial<IFilterData>) => {
    setFilterData(prev => ({ ...prev, ...updates }));
    setQueryParams(prev => ({ ...prev, offset: 0 }));
  }, []);

  const handleSearchChange = (value: string) => {
    setSearch(value);
    setQueryParams(prev => ({ ...prev, offset: 0 }));
  };

  const handleStatusChange = (value: string) => {
    setStatus(value === 'ALL' ? ('' as CourseStatus) : (value as CourseStatus));
    setQueryParams(prev => ({ ...prev, offset: 0 }));
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
    setQueryParams(prev => ({ ...prev, offset: 0 }));
  };

  const onPageChangeHandler = (page: number) => {
    setQueryParams(prev => ({
      ...prev,
      offset: page - 1,
    }));
  };

  const handleOpenViewModal = (id: string) => {
    setShowModal(ModalType.View);
    setTopicId(id);
  };

  const handleHideModal = () => {
    setShowModal(ModalType.None);
    if (topicId) setTopicId('');
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
              Here you can view all your Topics
            </p>
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
            <TopicCardLoader count={queryParams.pageSize ?? 10} />
          ) : topics.items.length > 0 ? (
            topics.items.map(item => (
              <ContentBox
                key={item.topicId}
                viewOnly
                onClick={() => {}}
                title={item.topicName}
                description={item.description}
                image={item.thumbnail || ''}
                isOpenView={() => handleOpenViewModal(item.topicId)}
                isOpenEdit={() => {}}
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
            total={topics.total}
            perPage={queryParams.pageSize ?? 10}
            onPageChange={onPageChangeHandler}
          />
        </div>
      </section>

      {showModal === ModalType.View && (
        <CourseViewModal
          isOpen={showModal === ModalType.View}
          onClose={handleHideModal}
          topicId={topicId}
        />
      )}
    </div>
  );
};

export default MSPCourseCardList;
