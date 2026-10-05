import Skeleton, { SkeletonTheme } from 'react-loading-skeleton';

import Border from 'components/UserBorder';

const ClientAdminProductLoader = ({ count = 3 }: { count?: number }) => {
  return (
    <div className="content-grid content-grid-cols-3 content-gap-4">
      <SkeletonTheme baseColor="#7C7C7C" highlightColor="#9A9A9A" duration={1}>
        {[...Array(count)].map((_, index) => (
          <Border key={index}>
            <div className="content-m-1 content-p-6">
              <div className="content-flex content-items-center content-justify-between content-gap-8">
                <Skeleton height={16} containerClassName="content-w-full" />
                <Skeleton height={16} width={90} />
              </div>
              <div className="content-mt-2">
                <Skeleton height={16} />
                <div className="content-mt-2 content-flex content-flex-col content-gap-2">
                  <Skeleton height={16} width={160} />
                </div>
                <div className="content-mt-2 content-flex content-items-center content-gap-2">
                  <Skeleton
                    height={16}
                    width={120}
                    containerClassName="content-w-full"
                  />
                  <Skeleton height={16} width={40} />
                </div>
                <Skeleton height={16} containerClassName="content-w-full" />
                <div className="content-mt-2 content-flex content-items-center content-gap-2">
                  <Skeleton height={40} width={120} />
                  <Skeleton height={40} width={120} />
                </div>
              </div>
            </div>
          </Border>
        ))}
      </SkeletonTheme>
    </div>
  );
};

export default ClientAdminProductLoader;
