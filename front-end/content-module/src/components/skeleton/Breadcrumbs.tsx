import Skeleton, { SkeletonTheme } from 'react-loading-skeleton';

const BreadcrumbsLoader = ({ count = 4 }: { count?: number }) => {
  return (
    <SkeletonTheme baseColor="#7C7C7C" highlightColor="#9A9A9A" duration={1}>
      <Skeleton height={10} width="60%" />
    </SkeletonTheme>
  );
};

export default BreadcrumbsLoader;
