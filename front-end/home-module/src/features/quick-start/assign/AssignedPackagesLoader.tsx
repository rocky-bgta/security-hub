import Skeleton, { SkeletonTheme } from 'react-loading-skeleton';

const AssignedPackagesLoader = ({ count = 10 }: { count?: number }) => {
  return (
    <SkeletonTheme baseColor="#7C7C7C" highlightColor="#9A9A9A" duration={1}>
      <div className="home-border home-border-card-border">
        <div className="home-flex home-items-center home-gap-4 home-bg-white home-bg-opacity-25 home-p-3">
          <div className="home-w-[18%]">
            <Skeleton height={16} width={100} />
          </div>
          {/* Package Name */}
          <div className="home-w-[18%]">
            <Skeleton height={16} width={100} />
          </div>

          {/* Product Name */}
          <div className="home-w-[25%]">
            <Skeleton height={16} width={100} />
          </div>

          {/* Assigned Users */}
          <div className="home-w-[15%]">
            <Skeleton height={16} width={100} />
          </div>

          {/* License Used */}
          <div className="home-w-[15%]">
            <Skeleton height={16} width={100} />
          </div>
        </div>
        {[...Array(count)].map((_, index) => (
          <div
            key={index}
            className="home-flex home-items-center home-gap-10 home-border-b home-border-card-border home-p-4"
          >
            <div className="home-w-[18%]">
              <Skeleton height={16} width={100} />
            </div>
            {/* Package Name */}
            <div className="home-w-[18%]">
              <Skeleton height={16} />
            </div>

            {/* Product Name */}
            <div className="home-w-[25%]">
              <Skeleton height={16} />
            </div>

            {/* Assigned Users */}
            <div className="home-w-[15%]">
              <Skeleton height={16} />
            </div>

            {/* License Used */}
            <div className="home-w-[15%]">
              <Skeleton height={16} />
            </div>
          </div>
        ))}
      </div>
    </SkeletonTheme>
  );
};

export default AssignedPackagesLoader;
