import Skeleton, { SkeletonTheme } from 'react-loading-skeleton';
import { Card } from 'common/Card';

const GRID_COLS_CLASS: Record<number, string> = {
  2: 'home-grid-cols-1 sm:home-grid-cols-2',
  3: 'home-grid-cols-1 sm:home-grid-cols-2 lg:home-grid-cols-3',
  4: 'home-grid-cols-1 sm:home-grid-cols-2 lg:home-grid-cols-4',
  5: 'home-grid-cols-1 sm:home-grid-cols-2 lg:home-grid-cols-5',
};

const TrackersCardLoader = ({ count = 5 }: { count?: number }) => {
  const gridColsClass =
    GRID_COLS_CLASS[count] ?? 'home-grid-cols-1 sm:home-grid-cols-2';

  return (
    <div className={`home-grid ${gridColsClass} home-gap-4 md:home-gap-5 lg:home-gap-6`}>
      <SkeletonTheme baseColor="#7C7C7C" highlightColor="#9A9A9A" duration={1}>
        {[...Array(count)].map((_, index) => (
          <Card key={index}>
            <div className="home-flex home-items-center home-gap-x-4 home-px-4 home-py-[22px]">
              <Skeleton height={50} width={50} circle />
              <div className="home-w-full">
                <Skeleton height={16} />
                <Skeleton height={18} />
              </div>
            </div>
          </Card>
        ))}
      </SkeletonTheme>
    </div>
  );
};

export default TrackersCardLoader;
