import Skeleton, { SkeletonTheme } from 'react-loading-skeleton';

const LeaderBoardSkeleton = ({ count = 4 }: { count?: number }) => {
  return (
    <div className="content-mt-4 content-grid content-max-h-[300px] content-grid-cols-1 content-items-stretch content-gap-2 content-overflow-y-auto sm:content-grid-cols-2">
      <SkeletonTheme baseColor="#7C7C7C" highlightColor="#9A9A9A" duration={1}>
        {[...Array(count)].map((_, index) => (
          <div key={index}>
            <Skeleton height={40} containerClassName="content-flex" />
            <div className="content-pt-1">
              <Skeleton height={170} />
              <div className="content-mt-2">
                <Skeleton height={20} className="content-mb-2 content-w-full" />
                <Skeleton height={20} width={100} />
              </div>
            </div>
          </div>
        ))}
      </SkeletonTheme>
    </div>
  );
};

export default LeaderBoardSkeleton;
