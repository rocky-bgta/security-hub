import Border from 'components/UserBorder';
import Skeleton, { SkeletonTheme } from 'react-loading-skeleton';

const CourseTableLoader = ({ count = 6 }: { count?: number }) => {
  return (
    <Border className="content-h-full">
      <div>
        <SkeletonTheme
          baseColor="#7C7C7C"
          highlightColor="#9A9A9A"
          duration={1}
        >
          <div className="content-flex content-px-4 content-pt-3">
            <Skeleton height={24} width={100} />
          </div>
          <div className="content-space-y-3.5 content-p-4 content-py-[23px]">
            {[...Array(count)].map((_, index) => (
              <div
                key={index}
                className="content-flex content-items-center content-gap-4"
              >
                <div className="content-w-2/5">
                  <Skeleton height={22} />
                </div>

                <div className="content-w-1/5">
                  <Skeleton height={22} />
                </div>

                <div className="content-w-[10%]">
                  <Skeleton height={22} />
                </div>

                <div className="content-w-[30%]">
                  <Skeleton
                    height={22}
                    containerClassName="content-rounded-full"
                  />
                </div>
              </div>
            ))}
          </div>
        </SkeletonTheme>
      </div>
    </Border>
  );
};

export default CourseTableLoader;
