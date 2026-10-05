import { useEffect, useState } from 'react';
import { Fragment } from 'react/jsx-runtime';

import clsx from 'clsx';
import { Button } from 'common/Button';
import SearchBox from 'components/SearchBox';
import Border from 'components/UserBorder';
import UserHeading from 'components/UserHeading';
import UserCourseList from 'features/course/UserCourseList';
import UserPackageList from 'features/package/UserPackageList';
import { useAPI } from 'hooks/UseAPI';
import useDebounce from 'hooks/UseDebounce';
import { useStore } from 'hooks/UseStore';
import { CourseStatus, IGetListParams, IList, IResponse } from 'models/Global';
import { IUserPackage, IUserSubPackageTopic } from 'models/Package';
import { useNavigate } from 'react-router-dom';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { routes } from 'routes/Routes';
import { BASE_URL, InitGetListParams } from 'utils/Constants';
import { objectToQueryString } from 'utils/Helper';

interface IProps {
  hostPath: typeof routes;
  isDashboard?: boolean;
}

const ClientUserPackageList = ({ hostPath, isDashboard }: IProps) => {
  const { userInfo } = useStore();
  const navigate = useNavigate();
  const [loading, setLoading] = useState<boolean>(true);
  const [courseLoading, setCourseLoading] = useState<boolean>(false);
  const [packageData, setPackageData] = useState<IList<IUserPackage>>({
    ...InitGetListParams,
    total: 0,
    items: [],
  });
  const [queryParams, setQueryParams] = useState<IGetListParams>({
    ...InitGetListParams,
    pageSize: 100,
    userId: userInfo.userId,
    status: isDashboard ? CourseStatus.ALL : CourseStatus.PENDING,
  });
  const [completeCount, setCompleteCount] = useState<number>(0);
  const [courseData, setCourseData] = useState<Array<IUserSubPackageTopic>>([]);
  const [courseQueryString, setCourseQueryString] = useState<string>('');
  const [courseQueryParams, setCourseQueryParams] = useState<IGetListParams>({
    ...InitGetListParams,
    userId: userInfo.userId,
    packageId: '',
    search: '',
  });
  const searchDebounce = useDebounce(courseQueryString, 500);

  const apiClient = useAPI();

  useEffect(() => {
    const resp = objectToQueryString(courseQueryParams);
    setCourseQueryString(resp);
  }, [courseQueryParams]);

  useEffect(() => {
    fetchPackagesData();
  }, [queryParams]);

  useEffect(() => {
    if (searchDebounce && courseQueryParams.packageId) {
      fetchCourseData();
    }
  }, [searchDebounce]);

  useEffect(() => {
    if (!isDashboard) {
      fetchCompletedPackages();
    }
  }, []);

  const fetchPackagesData = async () => {
    setLoading(true);

    try {
      const response: IResponse<IList<IUserPackage>> = await apiClient.get(
        API_END_POINTS.USER_PACKAGE_LIST + objectToQueryString(queryParams),
      );
      setCourseQueryParams(prevState => ({
        ...prevState,
        packageId: response.data?.items[0]?.packageId,
      }));
      setPackageData({
        ...InitGetListParams,
        total: response.data.total,
        items: response.data.items,
      });
      if (queryParams.status === CourseStatus.COMPLETED) {
        setCompleteCount(response.data.total);
      }
    } catch (error) {
      console.error('Error fetching packages:', error);
    } finally {
      setLoading(false);
    }
  };

  const fetchCourseData = async () => {
    setCourseLoading(true);

    try {
      const response: IResponse<Array<IUserSubPackageTopic>> =
        await apiClient.get(
          API_END_POINTS.USER_COURSE_LIST + courseQueryString,
        );
      setCourseData(response.data);
    } catch (error) {
      console.error('Error fetching courses:', error);
    } finally {
      setCourseLoading(false);
    }
  };

  const fetchCompletedPackages = async () => {
    setLoading(true);

    try {
      const response: IResponse<IList<IUserPackage>> = await apiClient.get(
        BASE_URL +
          API_END_POINTS.USER_PACKAGE_LIST +
          objectToQueryString({
            ...queryParams,
            status: CourseStatus.COMPLETED,
          }),
      );
      setCompleteCount(response.data.total);
    } catch (error) {
      console.error('Error fetching packages:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleCourseStatus = (status: CourseStatus) => {
    setQueryParams(prevState => ({
      ...prevState,
      status: status,
    }));
  };

  const onPageChangeHandler = (page: number) => {
    setCourseQueryParams(prevState => ({
      ...prevState,
      offset: page - 1,
    }));
  };

  return (
    <Fragment>
      {!isDashboard && (
        <UserHeading
          variant="title"
          text="All Course "
          className="content-mb-4 sm:content-mb-6"
        />
      )}
      <Border>
        <div className="content-p-4 sm:content-p-6">
          <div className="content-mb-4 content-flex content-flex-col content-gap-3 content-justify-between sm:content-mb-6 sm:content-flex-row sm:content-items-center sm:content-gap-4">
            {isDashboard ? (
              <UserHeading
                variant="subtitle"
                text={`All Course (${packageData.total})`}
                className="content-mb-2"
              />
            ) : (
              <div className="content-flex content-flex-col content-gap-3 sm:content-flex-row sm:content-items-center sm:content-gap-10">
                <button
                  onClick={() => handleCourseStatus(CourseStatus.PENDING)}
                  className={clsx(
                    queryParams.status === CourseStatus.PENDING &&
                      'content-text-primary',
                    'content-text-base content-text-cloudy-white md:content-text-lg',
                  )}
                >
                  Pending Courses{' '}
                  {packageData.total > 0 && `(${packageData.total})`}
                </button>
                <button
                  onClick={() => handleCourseStatus(CourseStatus.COMPLETED)}
                  className={clsx(
                    queryParams.status === CourseStatus.COMPLETED &&
                      'content-text-primary',
                    'content-text-base content-text-cloudy-white md:content-text-lg',
                  )}
                >
                  Completed {completeCount > 0 && `(${completeCount})`}
                </button>
              </div>
            )}
            <SearchBox
              value={String(courseQueryParams.search)}
              onChange={value =>
                setCourseQueryParams(prevState => ({
                  ...prevState,
                  search: value,
                  offset: 0,
                }))
              }
            />
          </div>
          <UserPackageList
            loading={loading}
            data={packageData.items}
            queryParams={courseQueryParams}
            setQueryParams={setCourseQueryParams}
          />
          <UserCourseList
            loading={courseLoading}
            data={courseData}
            onPageChangeHandler={onPageChangeHandler}
            reFetchData={fetchCourseData}
            hostPath={hostPath}
          />
        </div>

        {isDashboard && (
          <div className="content-pb-6">
            <Button
              className="content-mx-auto content-rounded-full"
              onClick={() => navigate(routes.courseList.path)}
            >
              See More
            </Button>
          </div>
        )}
      </Border>
    </Fragment>
  );
};

export default ClientUserPackageList;
