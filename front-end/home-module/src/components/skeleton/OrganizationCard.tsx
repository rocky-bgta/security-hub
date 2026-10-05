import Skeleton, { SkeletonTheme } from 'react-loading-skeleton';
import { Button } from 'common/Button';

interface IProps {
  count?: number;
}

const OrganizationCardLoader = ({ count = 4 }: IProps) => {
  return (
    <div className={`home-grid-cols-${count} home-grid home-gap-4`}>
      <SkeletonTheme baseColor="#7C7C7C" highlightColor="#9A9A9A" duration={1}>
        {[...Array(count)].map((_, index) => (
          <div
            key={index}
            className="home-border home-border-card-border home-p-4"
          >
            {/* Top Section: Icon + Info + Status */}
            <div className="home-mb-4 home-flex home-items-center home-justify-between">
              <div className="home-flex home-w-full home-items-center home-gap-3">
                {/* Icon Skeleton */}
                <div className="home-size-14 home-rounded-lg">
                  <Skeleton height="100%" width="100%" />
                </div>

                {/* Brand Info Skeleton */}
                <div className="home-w-full">
                  <Skeleton
                    height={20}
                    width="70%"
                    style={{ marginBottom: 6 }}
                  />
                  <Skeleton height={16} width="40%" />
                </div>
              </div>

              {/* Status Skeleton */}
              <Skeleton height={24} width={60} style={{ borderRadius: 999 }} />
            </div>

            <div className="home-h-10 home-w-full home-rounded-full">
              <Skeleton height="100%" width="100%" />
            </div>
          </div>
        ))}
      </SkeletonTheme>
    </div>
  );
};

export default OrganizationCardLoader;
