import Skeleton, { SkeletonTheme } from 'react-loading-skeleton';

import Border from 'components/UserBorder';

const TopicCardLoader = ({ count = 10 }: { count?: number }) => {
  return (
    <SkeletonTheme baseColor="#7C7C7C" highlightColor="#9A9A9A" duration={1}>
      {[...Array(count)].map((_, index) => (
        <Border key={index} className="content-h-full content-p-2">
          <div className="content-flex content-h-full content-flex-col content-justify-between content-gap-4 content-overflow-hidden">
            <div>
              <Skeleton height={224} containerClassName="content-flex" />
              <div className="content-mt-2 content-flex content-items-start content-justify-between content-gap-2">
                <Skeleton height={16} containerClassName="content-w-2/3" />
                <Skeleton height={24} width={70} borderRadius={6} />
              </div>
            </div>
            <Skeleton height={14} count={2} containerClassName="content-w-full" />
            <div className="content-mb-1 content-flex content-justify-between content-gap-2">
              <Skeleton height={32} width={72} borderRadius={6} />
              <Skeleton height={32} width={52} borderRadius={6} />
              <Skeleton height={32} width={48} borderRadius={6} />
            </div>
          </div>
        </Border>
      ))}
    </SkeletonTheme>
  );
};

export default TopicCardLoader;
