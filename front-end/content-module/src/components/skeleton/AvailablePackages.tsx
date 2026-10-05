import Skeleton, { SkeletonTheme } from 'react-loading-skeleton';

import Border from 'components/UserBorder';

const AvailablePackagesLoader = ({ count = 3 }: { count?: number }) => {
  return (
    <SkeletonTheme baseColor="#7C7C7C" highlightColor="#9A9A9A" duration={1}>
      {[...Array(count)].map((_, index) => (
        <Border key={index} className="content-p-6">
          <div>
            <Skeleton height={18} width={100} />
            <div className="content-space-y-2">
              <Skeleton height={16} />
              <Skeleton height={16} className="content-mb-4" />
              <Skeleton height={16} width={180} />

              <Skeleton height={16} width={100} />

              <Skeleton height={16} width={180} />
              <Skeleton height={16} width={200} />
              <Skeleton height={16} width={280} />
            </div>
          </div>
        </Border>
      ))}
    </SkeletonTheme>
  );
};

export default AvailablePackagesLoader;
