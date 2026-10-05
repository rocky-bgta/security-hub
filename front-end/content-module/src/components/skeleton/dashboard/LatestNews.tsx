import Skeleton, { SkeletonTheme } from 'react-loading-skeleton';

const LatestNewsSkeleton = ({ count = 4 }: { count?: number }) => {
  return (
    <div className="content-mt-4 content-space-y-2">
      <SkeletonTheme baseColor="#7C7C7C" highlightColor="#9A9A9A" duration={1}>
        {[...Array(count)].map((_, index) => (
          <div key={index} className="content-flex content-gap-4">
            <Skeleton height={96} width={96} />
            <div className="content-w-full content-pt-1">
              <Skeleton height={16} width={'60%'} />
              <Skeleton height={16} />
              <div className="content-mt-2 content-grid content-grid-cols-3 content-gap-4">
                <Skeleton height={28} />
                <Skeleton height={28} />
                <Skeleton height={28} />
              </div>
            </div>
          </div>
        ))}
        {/* <div className="content-flex content-w-full content-justify-center">
          <Skeleton height={48} width={120} className="content-rounded-full" />
        </div> */}
      </SkeletonTheme>
    </div>
  );
};

export default LatestNewsSkeleton;
