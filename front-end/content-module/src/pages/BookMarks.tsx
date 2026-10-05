import { useEffect, useMemo, useState } from 'react';
import { Fragment } from 'react/jsx-runtime';

import NoDataText from 'common/NoDataText';
import CourseCardLoader from 'components/skeleton/CourseCard';
import SearchBox from 'components/SearchBox';
import Border from 'components/UserBorder';
import UserCourseCard from 'components/UserCourseCard';
import UserHeading from 'components/UserHeading';
import { useAPI } from 'hooks/UseAPI';
import useDebounce from 'hooks/UseDebounce';
import { useStore } from 'hooks/UseStore';
import { IGetListParams } from 'models/Global';
import { IUserSubPackageTopic } from 'models/Package';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { routes } from 'routes/Routes';
import { InitGetListParams } from 'utils/Constants';
import { objectToQueryString } from 'utils/Helper';

const BookMarks = () => {
  const { userInfo } = useStore();
  const [loading, setLoading] = useState<boolean>(true);
  const [bookmarkData, setBookmarkData] = useState<Array<IUserSubPackageTopic>>(
    [],
  );
  const [search, setSearch] = useState('');
  const [queryString, setQueryString] = useState<string>('');
  const [queryParams, setQueryParams] = useState<IGetListParams>({
    ...InitGetListParams,
    userId: userInfo.userId,
  });
  const searchDebounce = useDebounce(queryString, 500);
  const apiClient = useAPI();

  const filteredBookmarks = useMemo(() => {
    const term = search.trim().toLowerCase();
    if (!term) return bookmarkData;

    return bookmarkData.filter(
      item =>
        item.topicName?.toLowerCase().includes(term) ||
        item.description?.toLowerCase().includes(term),
    );
  }, [bookmarkData, search]);

  useEffect(() => {
    const resp = objectToQueryString(queryParams);
    setQueryString(resp);
  }, [queryParams]);

  useEffect(() => {
    if (searchDebounce) {
      fetchCourseData();
    }
  }, [searchDebounce]);

  const fetchCourseData = async () => {
    setLoading(true);

    try {
      const response = await apiClient.get(
        API_END_POINTS.USER_BOOKMARK_LIST + queryString,
      );
      setBookmarkData(response.data);
    } catch (error) {
      console.error('Error fetching course data:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Fragment>
      <UserHeading variant="title" text="Bookmark" className="content-mb-4 sm:content-mb-6" />
      <Border>
        <div className="content-p-4 sm:content-p-6">
          <div className="content-mb-4 sm:content-mb-6">
            <div className="content-flex content-flex-col content-gap-3 content-justify-between sm:content-flex-row sm:content-items-center sm:content-gap-4">
              <UserHeading variant="subtitle" text="Saved Courses" />
              <SearchBox
                placeholder="Search bookmarks"
                value={search}
                onChange={setSearch}
                className="content-w-full sm:content-w-[280px] lg:content-w-[380px]"
              />
            </div>
          </div>
          {loading ? (
            <CourseCardLoader count={8} />
          ) : bookmarkData?.length > 0 ? (
            filteredBookmarks.length > 0 ? (
              <div className="content-grid content-grid-cols-1 content-gap-4 sm:content-gap-6 sm:content-grid-cols-2 lg:content-grid-cols-4">
                {filteredBookmarks.map((item, index) => (
                  <UserCourseCard
                    key={index}
                    data={item}
                    reFetchData={fetchCourseData}
                    path={routes.courseDetails.path
                      .replace(':packageId', item?.subPackageId || '')
                      .replace(':slug', item?.id)}
                    packageId={item?.id}
                  />
                ))}
              </div>
            ) : (
              <NoDataText text="No matching bookmarks found" />
            )
          ) : (
            <NoDataText text="No Bookmark Found" />
          )}
        </div>
      </Border>
    </Fragment>
  );
};
export default BookMarks;
