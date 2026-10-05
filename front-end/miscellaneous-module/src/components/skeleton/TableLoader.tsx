import Skeleton, { SkeletonTheme } from 'react-loading-skeleton';

const TableLoader = ({ count = 10 }: { count?: number }) => {
  return (
    <SkeletonTheme baseColor="#7C7C7C" highlightColor="#9A9A9A" duration={1}>
      <div className="border border-card-border">
        <div className="flex items-center gap-4 bg-white/25 p-3">
          {/* Title */}
          <div className="w-[18%]">
            <Skeleton height={16} width={100} />
          </div>

          {/* Leader Name */}
          <div className="w-1/4">
            <Skeleton height={16} width={100} />
          </div>

          {/* Designation */}
          <div className="w-[15%]">
            <Skeleton height={16} width={100} />
          </div>

          {/* Video */}
          <div className="w-[15%]">
            <Skeleton height={16} width={100} />
          </div>

          {/* Status */}
          <div className="w-[12%]">
            <Skeleton height={16} width={100} />
          </div>

          {/* Created Date */}
          <div className="w-[15%]">
            <Skeleton height={16} width={100} />
          </div>
        </div>
        {[...Array(count)].map((_, index) => (
          <div
            key={index}
            className="flex items-center gap-10 border-b border-card-border p-4"
          >
            {/* Title */}
            <div className="w-[18%]">
              <Skeleton height={16} />
            </div>

            {/* Leader Name */}
            <div className="w-1/4">
              <Skeleton height={16} />
            </div>

            {/* Designation */}
            <div className="w-[15%]">
              <Skeleton height={16} />
            </div>

            {/* Video */}
            <div className="w-[15%]">
              <Skeleton height={16} />
            </div>

            {/* Status */}
            <div className="w-[12%]">
              <Skeleton height={16} containerClassName="rounded-full" />
            </div>

            {/* Created Date */}
            <div className="w-[15%]">
              <Skeleton height={16} />
            </div>
          </div>
        ))}
      </div>
    </SkeletonTheme>
  );
};

export default TableLoader;
