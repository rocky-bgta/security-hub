import { Card } from 'common/Card';
import Skeleton, { SkeletonTheme } from 'react-loading-skeleton';

const NotificationSettingsSkeleton = ({ count = 10 }: { count?: number }) => {
  return (
    <SkeletonTheme baseColor="#7C7C7C" highlightColor="#9A9A9A" duration={1}>
      <Card className="p-6">
        {[...Array(count)].map((_, index) => (
          <div key={index} className="mb-6">
            <div>
              <Skeleton height={20} width={200} />
              <Skeleton height={16} width={300} className="mt-2" />
            </div>

            <div className="mt-6 rounded-md border border-card-border p-4">
              <div className="mb-3 flex items-center justify-between border-b border-card-border pb-3">
                <Skeleton height={20} width={200} />
                <Skeleton height={20} width={100} />
              </div>
              <div className="grid grid-cols-4 gap-3">
                <Skeleton height={16} width="25%" />
                <Skeleton height={16} width="25%" />
                <Skeleton height={16} width="25%" />
                <Skeleton height={16} width="25%" />
              </div>
            </div>
          </div>
        ))}
      </Card>
    </SkeletonTheme>
  );
};

export default NotificationSettingsSkeleton;
