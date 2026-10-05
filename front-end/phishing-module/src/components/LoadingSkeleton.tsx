import { Card } from 'common/Card';
import { cn } from 'utils/Helper';

interface SkeletonProps {
  className?: string;
}

export const Skeleton = ({ className }: SkeletonProps) => {
  return (
    <div className={cn('animate-pulse rounded bg-card-border', className)} />
  );
};

export const TableSkeleton = ({ count = 10 }: { count?: number }) => {
  return (
    <div className="border border-card-border">
      <div className="flex items-center gap-4 bg-white/25 p-4">
        {/* Title */}
        <div className="w-[18%]">
          <Skeleton className="h-4 w-full" />
        </div>

        {/* Leader Name */}
        <div className="w-1/4">
          <Skeleton className="h-4 w-full" />
        </div>

        {/* Designation */}
        <div className="w-[15%]">
          <Skeleton className="h-4 w-full" />
        </div>

        {/* Video */}
        <div className="w-[15%]">
          <Skeleton className="h-4 w-full" />
        </div>

        {/* Status */}
        <div className="w-[12%]">
          <Skeleton className="h-4 w-full" />
        </div>

        {/* Created Date */}
        <div className="w-[15%]">
          <Skeleton className="h-4 w-full" />
        </div>
      </div>
      {[...Array(count)].map((_, index) => (
        <div
          key={index}
          className="flex items-center gap-10 border-b border-card-border p-4"
        >
          {/* Title */}
          <div className="w-[18%]">
            <Skeleton className="h-4 w-full" />
          </div>

          {/* Leader Name */}
          <div className="w-1/4">
            <Skeleton className="h-4 w-full" />
          </div>

          {/* Designation */}
          <div className="w-[15%]">
            <Skeleton className="h-4 w-full" />
          </div>

          {/* Video */}
          <div className="w-[15%]">
            <Skeleton className="h-4 w-full" />
          </div>

          {/* Status */}
          <div className="w-[12%]">
            <Skeleton className="h-4 w-full" />
          </div>

          {/* Created Date */}
          <div className="w-[15%]">
            <Skeleton className="h-4 w-full" />
          </div>
        </div>
      ))}
    </div>
  );
};

/**
 * Card loading skeleton
 */
export const CardSkeleton = ({ count = 8 }: { count?: number }) => {
  return (
    <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
      {[...Array(count)].map((_, index) => (
        <div
          key={index}
          className="overflow-hidden rounded-lg border border-card-border shadow-sm"
        >
          <Skeleton className="h-40" />
          <div className="p-4">
            <Skeleton className="mb-2 h-5 w-3/4" />
            <Skeleton className="mb-3 h-4 w-full" />
            <div className="flex gap-2">
              <Skeleton className="h-5 w-16" />
              <Skeleton className="h-5 w-20" />
            </div>
          </div>
        </div>
      ))}
    </div>
  );
};

export const CampaignCardSkeleton = () => {
  return Array.from({ length: 6 }).map((_, i) => (
    <div
      key={i}
      className="animate-pulse rounded-lg border border-card-border p-4"
    >
      <div className="mb-3 h-5 w-3/4 rounded bg-card-border" />
      <div className="mb-4 h-4 w-1/2 rounded bg-card-border" />
      <div className="mb-4 grid grid-cols-4 gap-2">
        {[1, 2, 3, 4].map(j => (
          <div key={j} className="h-10 rounded bg-card-border" />
        ))}
      </div>
      <div className="h-5 w-full rounded bg-card-border" />
    </div>
  ));
};

export const KPICardsSkeleton = () => {
  return (
    <div className="grid grid-cols-2 gap-4 md:grid-cols-3">
      {[...Array(6)].map((_, i) => (
        <Card key={i}>
          <div className="p-6">
            <Skeleton className="mb-2 h-5 w-3/4" />
            <div className="flex gap-2">
              <Skeleton className="h-4 w-16" />
              <Skeleton className="h-5 w-16" />
              <Skeleton className="h-4 w-20" />
            </div>
          </div>
        </Card>
      ))}
    </div>
  );
};
