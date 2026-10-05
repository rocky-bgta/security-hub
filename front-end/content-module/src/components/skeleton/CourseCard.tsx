import Skeleton, { SkeletonTheme } from 'react-loading-skeleton';

import Border from 'components/UserBorder';

const CourseCardLoader = ({ count = 4 }: { count?: number }) => {
  return (
    <div className="content-grid content-grid-cols-1 content-gap-4 sm:content-grid-cols-2 lg:content-grid-cols-4">
      <SkeletonTheme baseColor="#7C7C7C" highlightColor="#9A9A9A" duration={1}>
        {[...Array(count)].map((_, index) => (
          <Border key={index}>
            <div className="content-m-1">
              <Skeleton height={190} containerClassName="content-flex" />
              <div className="content-p-3">
                <Skeleton height={10} />
                <div className="content-flex content-items-center content-justify-between content-gap-4">
                  <Skeleton height={10} containerClassName="content-w-full" />
                  <Skeleton height={10} width={30} />
                </div>
                <div className="content-flex content-items-center content-gap-2">
                  <Skeleton height={10} width={20} />
                  <Skeleton height={10} containerClassName="content-w-full" />
                </div>
              </div>
            </div>
          </Border>
        ))}
      </SkeletonTheme>
    </div>
  );
};

export default CourseCardLoader;
