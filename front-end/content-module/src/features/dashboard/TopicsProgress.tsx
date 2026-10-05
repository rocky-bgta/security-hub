import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';

import { IGetListParams, IList, IResponse } from 'models/Global';
import { API_END_POINTS } from 'routes/APIEndpoints';

import { InitGetListParams } from 'utils/Constants';
import { HumanizeDate, objectToQueryString } from 'utils/Helper';

import NoDataText from 'common/NoDataText';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from 'common/Table';
import SearchBox from 'components/SearchBox';
import AssignedPackagesLoader from 'components/skeleton/AssignedPackages';
import Border from 'components/UserBorder';
import { useAPI } from 'hooks/UseAPI';
import useDebounce from 'hooks/UseDebounce';
import { IUserTopicsProgress } from 'models/Users';
import { routes } from 'routes/Routes';

const TopicsProgress = () => {
  const [loading, setLoading] = useState<boolean>(false);
  const [topicsProgress, setTopicsProgress] = useState<IUserTopicsProgress[]>(
    [],
  );
  const [queryString, setQueryString] = useState<string>('');
  const [queryParams, setQueryParams] = useState<IGetListParams>({
    ...InitGetListParams,
    pageSize: 20,
    search: '',
  });
  const searchDebounce = useDebounce(queryString, 500);

  const apiClient = useAPI();

  useEffect(() => {
    const resp = objectToQueryString(queryParams);
    setQueryString(resp);
  }, [queryParams]);

  useEffect(() => {
    if (searchDebounce) {
      fetchTopicsProgress();
    }
  }, [searchDebounce]);

  const fetchTopicsProgress = async () => {
    setLoading(true);

    try {
      const response: IResponse<IList<IUserTopicsProgress>> =
        await apiClient.get(API_END_POINTS.USER_TOPICS_PROGRESS + queryString);

      setTopicsProgress(response.data.items);
    } catch (error) {
      console.error('Error fetching topic details:', error);
    } finally {
      setLoading(false);
    }
  };

  const getProgressWidth = (progress: string) => {
    const chatArray = progress.split('%');
    return Math.round(Number(chatArray[0]));
  };

  return (
    <div>
      <Border className="content-h-full">
        <div className="content-p-4 sm:content-p-6">
          <div className="content-flex content-flex-col content-justify-between content-gap-3 sm:content-flex-row sm:content-items-center sm:content-gap-0">
            <Link
              to={routes.courseList.path}
              className="content-text-lg content-font-semibold content-text-white content-underline sm:content-text-xl"
            >
              Topic Progress
            </Link>
            <SearchBox
              className="content-w-full sm:content-w-[280px] lg:content-w-[380px]"
              value={String(queryParams.search)}
              onChange={value => {
                setQueryParams(prevState => ({
                  ...prevState,
                  search: value,
                  offset: 0,
                }));
              }}
            />
          </div>

          <div className="content-mt-3 content-pr-1 lg:content-max-h-[340px] lg:content-overflow-auto">
            <div className="content-min-w-[520px] content-overflow-hidden content-rounded-2xl content-border content-border-card-border">
              {loading ? (
                <AssignedPackagesLoader />
              ) : (
                <Table className="!content-border-none">
                  <TableHeader>
                    <TableRow>
                      <TableHead>Name</TableHead>
                      <TableHead>Start Date</TableHead>
                      <TableHead>Expiration Date</TableHead>
                      <TableHead>Progress</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {topicsProgress?.length > 0 ? (
                      <>
                        {topicsProgress?.map((topicProgress, index) => (
                          <TableRow key={index}>
                            <TableCell>{topicProgress.name}</TableCell>
                            <TableCell>
                              {HumanizeDate(topicProgress.start_date)}
                            </TableCell>
                            <TableCell>
                              {HumanizeDate(topicProgress.expire_date)}
                            </TableCell>
                            <TableCell>
                              <div className="content-flex content-w-full content-items-center content-gap-2">
                                {getProgressWidth(topicProgress.progress)}%
                                <div className="content-h-2.5 content-w-full content-rounded-full content-bg-white content-bg-opacity-25">
                                  <div
                                    className="content-h-2.5 content-w-full content-rounded-full content-bg-primary"
                                    style={{
                                      width: `${getProgressWidth(topicProgress.progress)}%`,
                                    }}
                                  ></div>
                                </div>
                              </div>
                            </TableCell>
                          </TableRow>
                        ))}
                      </>
                    ) : (
                      <TableRow>
                        <TableCell colSpan={4}>
                          <NoDataText
                            text="No topics progress found"
                            className="content-p-4 content-text-center"
                          />
                        </TableCell>
                      </TableRow>
                    )}
                  </TableBody>
                </Table>
              )}
            </div>
          </div>
        </div>
      </Border>
    </div>
  );
};

export default TopicsProgress;
