import Skeleton, { SkeletonTheme } from 'react-loading-skeleton';

const UserChaptersLoader = () => {
  return (
    <SkeletonTheme baseColor="#7C7C7C" highlightColor="#9A9A9A" duration={1}>
      <Skeleton height={40} />
      <Skeleton height={40} />
      <Skeleton height={40} />
      <Skeleton height={40} />
      <Skeleton height={40} />
      <Skeleton height={40} />
      <Skeleton height={40} />
      <Skeleton height={40} />
    </SkeletonTheme>
  );
};

export default UserChaptersLoader;
