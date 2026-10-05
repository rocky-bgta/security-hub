import Border from 'components/UserBorder';
import Skeleton, { SkeletonTheme } from 'react-loading-skeleton';

const CertificateCardLoader = ({ count = 4 }: { count?: number }) => {
  return (
    <div className="content-grid content-grid-cols-1 content-gap-4 sm:content-gap-6 sm:content-grid-cols-2 lg:content-grid-cols-4">
      <SkeletonTheme baseColor="#7C7C7C" highlightColor="#9A9A9A" duration={1}>
        {[...Array(count)].map((_, index) => (
          <Border key={index}>
            <div className="content-m-1">
              <Skeleton height={190} containerClassName="content-flex" />
              <div className="content-p-3 content-pt-1">
                <Skeleton height={10} />
                <div className="content-flex content-items-center content-justify-between content-gap-4">
                  <Skeleton height={30} containerClassName="content-w-full" />
                  <Skeleton height={30} containerClassName="content-w-full" />
                </div>
              </div>
            </div>
          </Border>
        ))}
      </SkeletonTheme>
    </div>
  );
};

export default CertificateCardLoader;
