import Skeleton, { SkeletonTheme } from 'react-loading-skeleton';

const PolicyLoadingSkeleton = ({ count = 6 }: { count?: number }) => {
  return (
    <SkeletonTheme baseColor="#7C7C7C" highlightColor="#9A9A9A" duration={1}>
      <div className="grid grid-cols-3 gap-6">
        {[...Array(count)].map((_, index) => (
          <div
            key={index}
            className="flex flex-col justify-between overflow-hidden rounded-2xl border border-slate-700/40 bg-slate-900/40 p-5 shadow-lg ring-1 ring-slate-800/50 transition-all duration-300 hover:border-slate-600/60 hover:shadow-xl hover:ring-slate-700/50"
          >
            <div className="pb-3">
              <div className="flex items-center justify-between">
                <Skeleton height={16} width={100} />
                <Skeleton height={16} width={100} />
                <Skeleton height={16} width={100} />
              </div>
            </div>
            <div>
              <Skeleton height={20} width="100%" />
              <div className="mt-6">
                <Skeleton height={16} width="100%" />
                <Skeleton height={16} width="100%" />
                <Skeleton height={16} width="100%" />
              </div>
              <Skeleton height={40} width="100%" className="mt-6" />
            </div>
          </div>
        ))}
      </div>
    </SkeletonTheme>
  );
};

export default PolicyLoadingSkeleton;
