import Border from 'components/UserBorder';
import Skeleton, { SkeletonTheme } from 'react-loading-skeleton';

const CourseDetailsLoader = () => {
  return (
    <SkeletonTheme baseColor="#7C7C7C" highlightColor="#9A9A9A" duration={1}>
      <Border>
        <div className="content-p-6">
          <Skeleton height={20} width="70%" />
          <div className="content-mt-4 content-flex content-items-center">
            <Skeleton height={20} width={200} />
          </div>
          <p className="content-mt-4 content-text-white content-text-opacity-75">
            <Skeleton height={15} />
            <Skeleton height={15} />
            <Skeleton height={15} />
            <Skeleton height={15} />
            <Skeleton height={15} />
          </p>
          <div className="content-mt-6">
            <Border>
              <div className="content-flex content-items-center content-gap-2 content-p-2">
                <div className="content-w-3/4">
                  <Skeleton height={15} />
                  <Skeleton height={15} />
                  <Skeleton height={15} />
                </div>
                <div className="content-flex content-w-1/4 content-justify-end">
                  <Skeleton height={130} width={180} />
                </div>
              </div>
            </Border>
          </div>
          <Skeleton height={20} className="content-mt-5" />
          <div className="content-mt-3 content-flex content-flex-col content-gap-2">
            <Skeleton height={40} />
            <Skeleton height={40} />
            <Skeleton height={40} />
            <Skeleton height={40} />
            <Skeleton height={40} />
            <Skeleton height={40} />
          </div>
        </div>
      </Border>
    </SkeletonTheme>
  );
};

export default CourseDetailsLoader;
