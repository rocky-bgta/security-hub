import Skeleton, { SkeletonTheme } from 'react-loading-skeleton';

const PerformanceReportLoader = ({ count = 10 }: { count?: number }) => {
  return (
    <SkeletonTheme baseColor="#7C7C7C" highlightColor="#9A9A9A" duration={1}>
      <div className="content-border content-border-card-border">
        <div className="content-flex content-items-center content-gap-4 content-bg-white content-bg-opacity-25 content-p-3">
          <div className="content-w-[17%]">
            <Skeleton height={16} width={100} />
          </div>

          <div className="content-w-[17%]">
            <Skeleton height={16} width={100} />
          </div>

          <div className="content-w-[11%]">
            <Skeleton height={16} width={100} />
          </div>

          <div className="content-w-[11%]">
            <Skeleton height={16} width={100} />
          </div>

          <div className="content-w-[11%]">
            <Skeleton height={16} width={100} />
          </div>

          <div className="content-w-[11%]">
            <Skeleton height={16} width={100} />
          </div>
          <div className="content-w-[11%]">
            <Skeleton height={16} width={100} />
          </div>
          <div className="content-w-[11%]">
            <Skeleton height={16} width={100} />
          </div>
        </div>
        {[...Array(count)].map((_, index) => (
          <div
            key={index}
            className="content-flex content-items-center content-gap-10 content-border-b content-border-card-border content-p-4"
          >
            <div className="content-w-[17%]">
              <Skeleton height={16} />
            </div>

            <div className="content-w-[17%]">
              <Skeleton height={16} />
            </div>

            <div className="content-w-[11%]">
              <Skeleton height={16} />
            </div>

            <div className="content-w-[11%]">
              <Skeleton height={16} />
            </div>

            <div className="content-w-[11%]">
              <Skeleton height={16} />
            </div>

            <div className="content-w-[11%]">
              <Skeleton height={16} />
            </div>
            <div className="content-w-[11%]">
              <Skeleton height={16} />
            </div>
            <div className="content-w-[11%]">
              <Skeleton height={16} />
            </div>
          </div>
        ))}
      </div>
    </SkeletonTheme>
  );
};

export default PerformanceReportLoader;
