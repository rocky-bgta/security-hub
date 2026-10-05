import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from 'common/Dialog';
import Pagination from 'common/Pagination';
import { useAPI } from 'hooks/UseAPI';
import useDebounce from 'hooks/UseDebounce';
import { BookOpen, Clock, Loader2, Package } from 'lucide-react';
import { IGetListParams, IList, IResponse } from 'models/Global';
import { IAssignedLicense } from 'models/License';
import { Fragment, useEffect, useState } from 'react';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { FILE_PATH_PREFIX, InitGetListParams } from 'utils/Constants';
import { objectToQueryString } from 'utils/Helper';

interface IProps {
  isOpen: boolean;
  onClose: () => void;
  data: IAssignedLicense;
}

interface ITopicList {
  topicName: string;
  description: string;
  durationMinutes: number;
  thumbnail: string;
  contentType: string;
  category: Array<string>;
}

const TopicsListModal = ({ isOpen, onClose, data }: IProps) => {
  const apiClient = useAPI();
  const [queryString, setQueryString] = useState<string>('');
  const [queryParams, setQueryParams] = useState<IGetListParams>({
    ...InitGetListParams,
    productId: data.productId,
    packageId: data.packageId,
  });
  const [topics, setTopics] = useState<IList<ITopicList>>({
    offset: 0,
    pageSize: 10,
    total: 0,
    items: [],
  });
  const [loading, setLoading] = useState(true);
  const searchDebounce = useDebounce(queryString, 500);

  useEffect(() => {
    const resp = objectToQueryString(queryParams);
    setQueryString(resp);
  }, [queryParams]);

  useEffect(() => {
    if (searchDebounce && isOpen) {
      fetchTopics();
    }
  }, [searchDebounce, isOpen]);

  const fetchTopics = async () => {
    setLoading(true);
    try {
      const response: IResponse<IList<ITopicList>> = await apiClient.get(
        API_END_POINTS.TOPIC_BY_PACKAGE + queryString,
      );
      setTopics(response.data);
    } catch (error) {
      console.error('Error fetching package details:', error);
    } finally {
      setLoading(false);
    }
  };

  const onPageChangeHandler = (page: number) => {
    setQueryParams(prevState => ({
      ...prevState,
      offset: page - 1,
    }));
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="content-max-h-[90vh] content-w-1/2 content-overflow-y-auto">
        <DialogHeader>
          <DialogTitle className="content-flex content-items-center content-gap-2 content-text-2xl content-font-bold">
            <Package className="content-size-6 content-text-primary" />
            Package Topics
          </DialogTitle>
          <DialogDescription className="content-text-sm content-text-gray-500">
            {topics.total} topics found for this package
          </DialogDescription>
        </DialogHeader>

        <div className="content-mt-4 content-space-y-4">
          {loading ? (
            <div className="content-flex content-h-full content-items-center content-justify-center content-py-12">
              <Loader2 className="content-size-6 content-animate-spin content-text-primary" />
            </div>
          ) : (
            <Fragment>
              {topics.items.length === 0 ? (
                <div className="content-py-12 content-text-center content-text-gray-500">
                  <BookOpen className="content-mx-auto content-mb-3 content-size-12 content-opacity-50" />
                  <p>No topics found for this package</p>
                </div>
              ) : (
                <div className="content-grid content-gap-4">
                  {topics.items.map((topic, index) => (
                    <div
                      key={index}
                      className="content-rounded-lg content-border content-border-card-border content-p-4"
                    >
                      <div className="content-flex content-gap-4">
                        <div className="content-shrink-0">
                          {topic.thumbnail ? (
                            <img
                              src={FILE_PATH_PREFIX + topic.thumbnail}
                              alt={topic.topicName}
                              className="content-size-24 content-rounded-md content-object-cover"
                            />
                          ) : (
                            <div className="content-size-24 content-rounded-md content-bg-gray-100" />
                          )}
                        </div>

                        <div className="content-flex-1">
                          <h3 className="content-mb-2 content-text-lg content-font-semibold">
                            {topic.topicName}
                          </h3>
                          <p className="content-mb-3 content-line-clamp-2 content-text-sm content-text-ash-gray">
                            {topic.description ? topic.description : 'N/A'}
                          </p>
                          <div className="content-flex content-flex-wrap content-gap-3 content-text-sm content-text-gray-500">
                            <div className="content-flex content-items-center content-gap-1">
                              <Clock className="content-size-4" />
                              <span>{topic.durationMinutes} minutes</span>
                            </div>

                            <div className="content-flex content-items-center content-gap-1">
                              <Package className="content-size-4" />
                              <span>
                                {topic.category
                                  ? topic.category.join(', ')
                                  : 'N/A'}
                              </span>
                            </div>

                            <div className="content-rounded content-bg-primary/10 content-px-2 content-py-1 content-text-xs content-font-medium content-text-primary">
                              {topic.contentType ? topic.contentType : 'N/A'}
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </Fragment>
          )}

          {topics.total > topics.pageSize && (
            <div className="content-flex content-justify-end content-pt-4">
              <Pagination
                total={topics.total}
                perPage={topics.pageSize}
                onPageChange={onPageChangeHandler}
              />
            </div>
          )}
        </div>
      </DialogContent>
    </Dialog>
  );
};

export default TopicsListModal;
