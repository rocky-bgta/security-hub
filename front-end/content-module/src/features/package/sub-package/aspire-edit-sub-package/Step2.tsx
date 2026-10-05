import { Filter, RotateCcw } from 'lucide-react';
import { useCallback, useEffect, useState } from 'react';

import { Button } from 'common/Button';
import { Card, CardContent, CardTitle } from 'common/Card';

import SpinnerLoader from 'common/loader/SpinnerLoader';
import Pagination from 'common/Pagination';
import FilterControls from 'components/FilterControls';
import SelectedTopicsDisplay from 'components/SelectedTopicsDisplay';
import TopicCard from 'components/TopicCard';
import { useAPI } from 'hooks/UseAPI';
import useDebounce from 'hooks/UseDebounce';
import {
  ICategory,
  ICompliance,
  IContentType,
  ICountry,
} from 'models/Configuration';
import { ISelectTopic } from 'models/Course';
import { IFilterData } from 'models/Filter';
import { IGetListParams, IResponse, Status } from 'models/Global';
import { ISelectOption } from 'models/Input';
import { ITopicResponse } from 'models/Topic';
import { toast } from 'react-toastify';
import { API_END_POINTS } from 'routes/APIEndpoints';

interface IProps {
  data: Array<ISelectTopic>;
  onUpdate: (data: Array<ISelectTopic>) => void;
  onSubmit?: () => void;
  onPrevious: () => void;
  packageId: string;
  loading: boolean;
}

const Step2 = ({
  data,
  onUpdate,
  onPrevious,
  onSubmit,
  packageId,
  loading,
}: IProps) => {
  const apiClient = useAPI();
  const [selectedTopics, setSelectedTopics] = useState<Array<ISelectTopic>>(
    data.length > 0 ? data : [],
  );
  const [filterData, setFilterData] = useState<IFilterData>({
    countryIds: [],
    complianceIds: [],
    categoryIds: [],
    contentTypeIds: [],
    durationMinutes: [],
  });
  const [topics, setTopics] = useState<ITopicResponse>({
    topics: [],
    totalElements: 0,
    totalPages: 0,
    currentPage: 0,
    hasNext: false,
    hasPrevious: false,
  });
  const [topicsLoading, setTopicsLoading] = useState<boolean>(true);
  const [search, setSearch] = useState<string>('');
  const [categoryOptions, setCategoryOptions] = useState<ISelectOption[]>([]);
  const [contentTypeOptions, setContentTypeOptions] = useState<ISelectOption[]>(
    [],
  );
  const [complianceOptions, setComplianceOptions] = useState<ISelectOption[]>(
    [],
  );
  const [countryOptions, setCountryOptions] = useState<ISelectOption[]>([]);
  const [queryParams, setQueryParams] = useState<IGetListParams>({
    offset: 1,
    pageSize: 10,
  });
  const searchDebounced = useDebounce(search, 1000);

  const filterOptions = {
    categoryOptions,
    contentTypeOptions,
    complianceOptions,
    countryOptions,
  };

  useEffect(() => {
    if (data.length > 0) {
      setSelectedTopics(data);
    }
  }, [data]);

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
  }, []);

  useEffect(() => {
    const fetchTopics = async () => {
      setTopicsLoading(true);
      try {
        const response: IResponse<ITopicResponse> = await apiClient.post(
          API_END_POINTS.FILTER_TOPIC_LIST + packageId,
          {
            data: {
              countryIds: filterData.countryIds.map(item => item.id),
              complianceIds: filterData.complianceIds.map(item => item.id),
              categoryIds: filterData.categoryIds.map(item => item.id),
              contentTypeId: filterData.contentTypeIds.map(item => item.id),
              durationRanges: filterData.durationMinutes,
              searchText: searchDebounced,
              page: queryParams.offset,
              size: queryParams.pageSize,
              status: Status.ENABLED,
              selectedTopicId: selectedTopics.map(item => item.id),
            },
          },
        );

        setTopics(response.data);
      } catch (error) {
        console.error('Error fetching topics:', error);
      } finally {
        setTopicsLoading(false);
      }
    };
    fetchTopics();
  }, [filterData, searchDebounced, queryParams]);

  const handleFilterChange = useCallback((updates: Partial<IFilterData>) => {
    setFilterData(prev => ({ ...prev, ...updates }));
  }, []);

  const handleTopicToggle = useCallback(
    (topicId: string, topicName: string) => {
      setSelectedTopics(prev => {
        const isSelected = prev.some(topic => topic.id === topicId);
        if (isSelected) {
          return prev.filter(topic => topic.id !== topicId);
        } else {
          return [...prev, { id: topicId, topicName: topicName }];
        }
      });
    },
    [],
  );

  const handleReset = useCallback(() => {
    setFilterData({
      countryIds: [],
      complianceIds: [],
      categoryIds: [],
      contentTypeIds: [],
      durationMinutes: [],
    });
    setSearch('');
  }, []);

  const handleSave = () => {
    if (selectedTopics.length === 0) {
      return toast.error('Select at least one topic');
    }
    // onUpdate(selectedTopics);
    onSubmit?.();
  };
  useEffect(() => {
    onUpdate(selectedTopics);
  }, [selectedTopics]);

  const handlePageChange = (page: number) => {
    setQueryParams(prevState => ({
      ...prevState,
      offset: page,
    }));
  };

  return (
    <Card className="content-w-full">
      <div className="content-px-5 content-pt-5">
        <div className="content-mb-3 content-flex content-items-center content-justify-between">
          <CardTitle className="content-flex content-items-center content-gap-2">
            <div className="content-flex content-items-center content-gap-2">
              <Filter width={20} height={20} />
              Filter Topic
            </div>
          </CardTitle>
          <Button variant="secondary" onClick={handleReset}>
            <RotateCcw />
            Reset
          </Button>
        </div>
      </div>

      <CardContent>
        <FilterControls
          filterData={filterData}
          onFilterChange={handleFilterChange}
          options={filterOptions}
          search={search}
          setSearch={setSearch}
        />

        <p className="content-mt-5 content-text-xl content-font-semibold">
          Select Topics *{' '}
          <span className="content-text-base content-text-primary">
            ({topics.totalElements || 0} topics available)
          </span>
        </p>
        <p className="content-mb-4 content-text-ash-gray">
          Choose at least{' '}
          <span className="content-text-primary">Two topic</span> that will be
          covered in this product
        </p>

        <SelectedTopicsDisplay
          selectedTopics={selectedTopics.map(topic => topic.topicName)}
          topics={topics.topics}
        />

        <div className="content-grid content-max-h-[400px] content-mt-5 content-grid-cols-2 content-gap-5 content-overflow-y-auto">
          {topicsLoading ? (
            <div className="content-col-span-2 content-flex content-h-[100px] content-items-center content-justify-center">
              <SpinnerLoader />
            </div>
          ) : (
            topics.topics.map(topic => (
              <TopicCard
                key={topic.id}
                topic={topic}
                isSelected={selectedTopics.some(
                  selectedTopic => selectedTopic.id === topic.id,
                )}
                onToggle={() => handleTopicToggle(topic.id, topic.topicName)}
              />
            ))
          )}
        </div>

        <div className="content-mt-4">
          <Pagination
            total={topics.totalElements}
            perPage={queryParams.pageSize ?? 10}
            onPageChange={handlePageChange}
          />
        </div>

        <div className="content-flex content-justify-between content-pt-6">
          <Button
            variant="outline"
            onClick={onPrevious}
            className="content-px-8 content-py-2"
          >
            Previous
          </Button>
          <Button onClick={handleSave} disabled={loading}>
            {loading ? 'Saving...' : 'Save & Continue'}
          </Button>
        </div>
      </CardContent>
    </Card>
  );
};

export default Step2;
