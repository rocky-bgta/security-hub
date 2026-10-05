import Border from 'components/UserBorder';
import Skeleton, { SkeletonTheme } from 'react-loading-skeleton';

const ExamCompleteLoader = () => {
  return (
    <SkeletonTheme baseColor="#7C7C7C" highlightColor="#9A9A9A" duration={1}>
      <div className="content-mx-auto content-w-full">
        <div className="content-flex content-flex-col content-items-center content-text-center">
          <Skeleton height={36} width={240} className="content-mb-2 content-max-w-full" />
          <Skeleton height={24} width={280} className="content-mb-2 content-max-w-full" />
          <Skeleton height={18} width={320} className="content-mb-4 content-max-w-full sm:content-mb-6" />
        </div>

        <Border>
          <div className="content-p-4 sm:content-p-6">
            <div className="content-mb-4 content-text-center">
              <Skeleton height={28} width={120} className="content-mb-2" />
              <Skeleton height={40} width={100} className="content-mb-2" />
              <Skeleton height={18} width={200} />
            </div>

            <div className="content-flex content-flex-col content-gap-4">
              <Skeleton height={18} width={160} />
              <Skeleton height={12} />
              <div className="content-flex content-items-center content-justify-between">
                <Skeleton height={16} width={24} />
                <Skeleton height={16} width={24} />
              </div>
              <div className="content-flex content-items-center content-justify-between">
                <Skeleton height={20} width={60} />
                <Skeleton height={28} width={90} />
              </div>
            </div>
          </div>
        </Border>

        <div className="content-mt-4 sm:content-mt-6">
          <Border>
            <div className="content-p-4 sm:content-p-6">
              <div className="content-text-center">
                <Skeleton height={28} width={200} className="content-mb-2 content-max-w-full" />
                <Skeleton height={18} width={256} className="content-max-w-full" />
              </div>

              <div className="content-mt-4 content-grid content-grid-cols-1 content-gap-3 sm:content-mt-6 sm:content-grid-cols-3 sm:content-gap-4">
                <div className="content-flex content-flex-col content-items-center content-rounded-lg content-border content-border-card-border content-p-4 sm:content-p-6">
                  <Skeleton height={20} width={28} className="content-mb-2" />
                  <Skeleton height={18} width={110} />
                </div>
                <div className="content-flex content-flex-col content-items-center content-rounded-lg content-border content-border-card-border content-p-4 sm:content-p-6">
                  <Skeleton height={20} width={28} className="content-mb-2" />
                  <Skeleton height={18} width={110} />
                </div>
                <div className="content-flex content-flex-col content-items-center content-rounded-lg content-border content-border-card-border content-p-4 sm:content-p-6">
                  <Skeleton height={20} width={28} className="content-mb-2" />
                  <Skeleton height={18} width={110} />
                </div>
              </div>
            </div>
          </Border>
        </div>

        <div className="content-mt-5 content-flex content-flex-col content-items-stretch content-justify-center content-gap-3 sm:content-mt-7 sm:content-flex-row sm:content-items-center sm:content-gap-4">
          <Skeleton height={44} className="content-w-full content-rounded-full sm:content-w-40" />
          <Skeleton height={44} className="content-w-full content-rounded-full sm:content-w-40" />
        </div>
      </div>
    </SkeletonTheme>
  );
};

export default ExamCompleteLoader;
