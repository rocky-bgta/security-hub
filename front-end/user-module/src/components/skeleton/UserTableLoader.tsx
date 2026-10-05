import Skeleton, { SkeletonTheme } from 'react-loading-skeleton';

const UserTableLoader = ({ count = 10 }: { count?: number }) => {
  return (
    <SkeletonTheme baseColor="#7C7C7C" highlightColor="#9A9A9A" duration={1}>
      <div className="border border-card-border">
        <div className="flex items-center gap-4 bg-white bg-opacity-25 p-3">
          <div className="w-[15%]">
            <Skeleton height={16} width={100} />
          </div>

          <div className="w-[24%]">
            <Skeleton height={16} width={100} />
          </div>

          <div className="w-[12%]">
            <Skeleton height={16} width={100} />
          </div>

          <div className="w-[12%]">
            <Skeleton height={16} width={100} />
          </div>

          <div className="w-[12%]">
            <Skeleton height={16} width={100} />
          </div>

          <div className="w-[10%]">
            <Skeleton height={16} width={100} />
          </div>

          <div className="w-[15%]">
            <Skeleton height={16} width={100} />
          </div>
        </div>
        {[...Array(count)].map((_, index) => (
          <div
            key={index}
            className="flex items-center gap-10 border-b border-card-border p-4"
          >
            <div className="w-[15%]">
              <Skeleton height={16} />
            </div>

            <div className="w-[24%]">
              <Skeleton height={16} />
            </div>
            <div className="w-[12%]">
              <Skeleton height={16} width={100} />
            </div>

            <div className="w-[12%]">
              <Skeleton height={16} />
            </div>

            <div className="w-[12%]">
              <Skeleton height={16} />
            </div>

            <div className="w-[10%]">
              <Skeleton height={16} containerClassName="rounded-full" />
            </div>

            <div className="w-[15%]">
              <Skeleton height={16} />
            </div>
          </div>
        ))}
      </div>
    </SkeletonTheme>
  );
};

export default UserTableLoader;
