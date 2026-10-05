import { Fragment } from 'react/jsx-runtime';

import NoDataText from 'common/NoDataText';
import Pagination from 'common/Pagination';
import CourseCardLoader from 'components/skeleton/CourseCard';
import UserCourseCard from 'components/UserCourseCard';
import { routes } from 'routes/Routes';
import { IUserSubPackageTopic } from 'models/Package';

interface IProps {
  loading: boolean;
  data: Array<IUserSubPackageTopic>;
  onPageChangeHandler: (page: number) => void;
  reFetchData: () => void;
  packageId?: string;
  hostPath: typeof routes;
}

const UserCourseList = ({
  loading,
  data,
  onPageChangeHandler,
  reFetchData,
  packageId,
  hostPath,
}: IProps) => {
  return (
    <Fragment>
      {loading ? (
        <CourseCardLoader count={8} />
      ) : (
        <Fragment>
          {data?.length > 0 ? (
            <div className="content-grid content-grid-cols-4 content-gap-4">
              {data?.map(item => (
                <UserCourseCard
                  key={item.id}
                  data={item}
                  reFetchData={reFetchData}
                  path={hostPath.courseDetails.path
                    .replace(':packageId', packageId || '')
                    .replace(':slug', item?.id)}
                />
              ))}
            </div>
          ) : (
            <NoDataText text="No Course Found" />
          )}
        </Fragment>
      )}

      <div className="content-pt-6">
        <Pagination
          variant="user"
          total={data?.length}
          perPage={8}
          onPageChange={onPageChangeHandler}
        />
      </div>
    </Fragment>
  );
};

export default UserCourseList;
