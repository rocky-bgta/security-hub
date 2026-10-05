import Skeleton, { SkeletonTheme } from 'react-loading-skeleton';

import Border from 'components/UserBorder';

const TrackersCardLoader = () => {
  return (
    <div className="content-grid content-grid-cols-1 content-gap-4 sm:content-grid-cols-2 lg:content-grid-cols-3 lg:content-gap-6 xl:content-grid-cols-5">
      <SkeletonTheme baseColor="#7C7C7C" highlightColor="#9A9A9A" duration={1}>
        {[...Array(5)].map((_, index) => (
          <Border key={index}>
            <div className="content-flex content-items-center content-gap-x-4 content-px-4 content-py-[22px]">
              <Skeleton height={50} width={50} circle />

              <div className="content-w-full">
                <Skeleton height={16} />
                <Skeleton height={18} />
              </div>
            </div>
          </Border>
        ))}
      </SkeletonTheme>
    </div>
  );
};

export default TrackersCardLoader;
