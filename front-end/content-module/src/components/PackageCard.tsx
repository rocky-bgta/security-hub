import { CourseIcon } from 'assets/icons';
import clsx from 'clsx';
import Border from 'components/UserBorder';
import { IGetListParams } from 'models/Global';
import { IUserPackage } from 'models/Package';

interface IProps {
  data: IUserPackage;
  queryParams: IGetListParams;
  setQueryParams: React.Dispatch<React.SetStateAction<IGetListParams>>;
}

const PackageCard = ({ data, queryParams, setQueryParams }: IProps) => {
  const isSelected = queryParams.packageId === data.packageId;
  return (
    <Border>
      <button
        onClick={() => {
          setQueryParams(prevState => ({
            ...prevState,
            packageId: data.packageId || '',
          }));
        }}
        className={clsx(
          isSelected
            ? 'content-bg-primary hover:content-bg-primary'
            : 'hover:content-bg-white hover:content-bg-opacity-25',
          'content-group content-w-full content-px-4 content-py-3 content-transition-colors content-duration-200',
        )}
      >
        <h3 className="content-mb-2 content-text-left content-text-base content-font-semibold content-text-white">
          {data.packageName || 'N/A'}
        </h3>
        <div className="content-flex content-items-center content-gap-1">
          <CourseIcon className="group-hover:content-fill-black" fill="white" />
          <span
            className={clsx(
              isSelected ? 'content-opacity-100' : 'content-opacity-75',
              'content-text-sm content-text-white group-hover:content-opacity-100',
            )}
          >
            {data.totalCourses || 0} Topics
          </span>
        </div>
      </button>
    </Border>
  );
};

export default PackageCard;
