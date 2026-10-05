import Skeleton, { SkeletonTheme } from 'react-loading-skeleton';

const PoolsAndSurveySkeleton = ({ count = 4 }: { count?: number }) => {
  return (
    <div className="content-mt-4">
      <SkeletonTheme baseColor="#7C7C7C" highlightColor="#9A9A9A" duration={1}>
        {[...Array(count)].map((_, index) => (
          <div key={index}>
            <Skeleton height={20} />
            <Skeleton height={20} width={'50%'} className="content-mt-2" />
            <div className="content-mt-4 content-flex content-flex-col content-gap-2">
              <Skeleton height={24} width={'80%'} />
              <Skeleton height={24} width={'80%'} />
              <Skeleton height={24} width={'80%'} />
              <Skeleton height={24} width={'80%'} />
            </div>
            <div className="content-mt-4 content-flex content-gap-2">
              <Skeleton
                height={48}
                width={120}
                className="content-rounded-full"
              />
              {/* <Skeleton
                height={48}
                width={120}
                className="content-rounded-full"
              /> */}
            </div>
          </div>
        ))}
      </SkeletonTheme>
    </div>
  );
};

export default PoolsAndSurveySkeleton;
