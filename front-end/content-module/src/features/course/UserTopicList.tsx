import { Fragment } from 'react/jsx-runtime';

import NoDataText from 'common/NoDataText';
import CourseCardLoader from 'components/skeleton/CourseCard';
import UserTopicCard from 'components/UserTopicCard';
import { IUserSubPackageTopic } from 'models/Package';
import { routes } from 'routes/Routes';

interface IProps {
  loading: boolean;
  data: Array<IUserSubPackageTopic>;
  subPackageId?: string;
}

const UserTopicList = ({ loading, data, subPackageId }: IProps) => {
  return (
    <Fragment>
      {loading ? (
        <CourseCardLoader count={4} />
      ) : (
        <Fragment>
          {data?.length > 0 ? (
            <div className="content-grid content-grid-cols-1 content-gap-4 sm:content-grid-cols-2 lg:content-grid-cols-4">
              {data?.map(item => (
                <UserTopicCard
                  key={item.id}
                  data={item}
                  path={routes?.courseDetails?.path
                    .replace(':packageId', subPackageId || '')
                    .replace(':slug', item?.id)}
                  packageId={subPackageId || ''}
                />
              ))}
            </div>
          ) : (
            <NoDataText text="No Topic Found" />
          )}
        </Fragment>
      )}
    </Fragment>
  );
};

export default UserTopicList;
