import SpinnerLoader from 'common/loader/SpinnerLoader';
import Skeleton, { SkeletonTheme } from 'react-loading-skeleton';

const TopicsStatisticsLoader = () => {
  return (
    <SkeletonTheme baseColor="#7C7C7C" highlightColor="#9A9A9A" duration={1}>
      <div className="content-h-[250px]">
        <SpinnerLoader />
      </div>
      <div className="content-flex content-items-center content-justify-center content-gap-3">
        <Skeleton height={30} width={100} />
        <Skeleton height={30} width={100} />
        <Skeleton height={30} width={100} />
      </div>
    </SkeletonTheme>
  );
};

export default TopicsStatisticsLoader;
