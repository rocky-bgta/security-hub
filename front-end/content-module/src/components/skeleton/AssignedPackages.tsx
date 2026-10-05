import Skeleton, { SkeletonTheme } from 'react-loading-skeleton';

const AssignedPackagesLoader = ({ count = 10 }: { count?: number }) => {
  return (
    <SkeletonTheme baseColor="#7C7C7C" highlightColor="#9A9A9A" duration={1}>
      <div className="content-border content-border-card-border">
        <div className="content-flex content-items-center content-gap-4 content-bg-white content-bg-opacity-25 content-p-3">
          {/* Package Name */}
          <div className="content-w-[18%]">
            <Skeleton height={16} width={100} />
          </div>

          {/* Product Name */}
          <div className="content-w-[25%]">
            <Skeleton height={16} width={100} />
          </div>

          {/* Assigned Users */}
          <div className="content-w-[15%]">
            <Skeleton height={16} width={100} />
          </div>

          {/* License Used */}
          <div className="content-w-[15%]">
            <Skeleton height={16} width={100} />
          </div>

          {/* Status */}
          <div className="content-w-[12%]">
            <Skeleton height={16} width={100} />
          </div>

          {/* License Expiry */}
          <div className="content-w-[15%]">
            <Skeleton height={16} width={100} />
          </div>
        </div>
        {[...Array(count)].map((_, index) => (
          <div
            key={index}
            className="content-flex content-items-center content-gap-10 content-border-b content-border-card-border content-p-4"
          >
            {/* Package Name */}
            <div className="content-w-[18%]">
              <Skeleton height={16} />
            </div>

            {/* Product Name */}
            <div className="content-w-[25%]">
              <Skeleton height={16} />
            </div>

            {/* Assigned Users */}
            <div className="content-w-[15%]">
              <Skeleton height={16} />
            </div>

            {/* License Used */}
            <div className="content-w-[15%]">
              <Skeleton height={16} />
            </div>

            {/* Status */}
            <div className="content-w-[12%]">
              <Skeleton height={16} containerClassName="content-rounded-full" />
            </div>

            {/* License Expiry */}
            <div className="content-w-[15%]">
              <Skeleton height={16} />
            </div>
          </div>
        ))}
      </div>
    </SkeletonTheme>
  );
};

export default AssignedPackagesLoader;
