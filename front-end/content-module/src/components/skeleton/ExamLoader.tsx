import Border from 'components/UserBorder';
import Skeleton, { SkeletonTheme } from 'react-loading-skeleton';

const ExamLoader = () => {
  return (
    <SkeletonTheme baseColor="#7C7C7C" highlightColor="#9A9A9A" duration={1}>
      <div className="content-text-center">
        <Skeleton height={10} width={'80%'} />
        <Skeleton height={10} width={'60%'} />
      </div>

      <Border className="content-mt-6 content-p-6">
        <div className="content-mb-4 content-flex content-justify-between content-border-b content-border-card-border content-pb-4">
          <div>
            <Skeleton height={16} width={100} />
            <Skeleton height={10} width={140} />
          </div>
          <div className="content-flex content-items-center content-gap-5">
            <div className="content-font-medium">
              <Skeleton height={10} width={100} />
              <Skeleton height={10} width={100} />
            </div>
            <div>
              <Skeleton height={10} width={20} />
              <Skeleton height={10} width={20} />
            </div>
          </div>
        </div>

        <div className="content-mb-4 content-text-white">
          <p className="content-mb-4 content-text-xl content-font-semibold">
            <Skeleton height={20} width={140} />
          </p>
          <ul className="content-grid content-grid-cols-2 content-gap-x-10 content-gap-y-4">
            {Array.from({ length: 10 }).map((_, idx) => (
              <Skeleton key={idx} height={28} />
            ))}
          </ul>
        </div>
      </Border>
      <div className="content-text-center">
        <Skeleton
          height={44}
          width={136}
          className="content-mt-8 content-rounded-full"
        />
      </div>
    </SkeletonTheme>
  );
};

export default ExamLoader;
