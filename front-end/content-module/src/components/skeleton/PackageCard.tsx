import Skeleton, { SkeletonTheme } from 'react-loading-skeleton';

import Border from 'components/UserBorder';

const PackageCardLoader = () => {
  return (
    <div className="content-grid content-grid-cols-1 content-gap-4 sm:content-grid-cols-2 lg:content-grid-cols-4">
      <SkeletonTheme baseColor="#7C7C7C" highlightColor="#9A9A9A" duration={1}>
        {[...Array(4)].map((_, index) => (
          <Border key={index}>
            <div className="content-w-full content-p-3">
              <Skeleton height={10} />
              <div className="content-flex content-items-center content-gap-2">
                <Skeleton height={15} width={15} circle />
                <Skeleton height={10} containerClassName="content-w-full" />
              </div>
            </div>
          </Border>
        ))}
      </SkeletonTheme>
    </div>
  );
};

export default PackageCardLoader;
