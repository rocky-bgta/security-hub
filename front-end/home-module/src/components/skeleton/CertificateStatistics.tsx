import SpinnerLoader from 'common/loader/SpinnerLoader';
import Skeleton, { SkeletonTheme } from 'react-loading-skeleton';

const CertificateStatisticsLoader = () => {
  return (
    <SkeletonTheme baseColor="#7C7C7C" highlightColor="#9A9A9A" duration={1}>
      <div className="home-grid home-h-[300px] home-w-full home-grid-cols-2 home-items-center">
        <div>
          <SpinnerLoader />
        </div>
        <div className="home-flex home-flex-col home-gap-2">
          <Skeleton height={20} width={'80%'} />
          <Skeleton height={20} width={'80%'} />
          <Skeleton height={20} width={'80%'} />
          <Skeleton height={20} width={'80%'} />
        </div>
      </div>
    </SkeletonTheme>
  );
};

export default CertificateStatisticsLoader;
