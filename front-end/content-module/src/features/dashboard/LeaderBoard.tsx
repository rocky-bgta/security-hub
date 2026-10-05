import NoDataText from 'common/NoDataText';
import Border from 'components/UserBorder';
import LeaderBoardSkeleton from 'components/skeleton/dashboard/LeaderBoard';
import { useAPI } from 'hooks/UseAPI';
import { IResponse, Status } from 'models/Global';
import { useEffect, useRef, useState } from 'react';
import { FaPlay } from 'react-icons/fa';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { FILE_PATH_PREFIX } from 'utils/Constants';

interface IProps {
  id: string;
  title: 'string';
  name: string;
  designation: string;
  videoType: 'UPLOAD_FILE';
  videoUrl: string;
  thumbnailUrl: string;
  status: Status;
  createdDate: string;
  createdBy: string;
  updatedBy: string | null;
  updatedAt: string;
  clientId: string | null;
  clientName: string | null;
  isDefault: boolean;
}

const LeaderBoard = () => {
  const [leaderBoardData, setLeaderBoardData] = useState<IProps[]>([]);
  const [loading, setLoading] = useState<boolean>(false);
  const apiClient = useAPI();

  const [activePlayingIndex, setActivePlayingIndex] = useState<number | null>(
    null,
  );
  const videoRefs = useRef<{ [key: number]: HTMLVideoElement | null }>({});

  useEffect(() => {
    fetchCertificateData();
  }, []);

  const fetchCertificateData = async () => {
    setLoading(true);

    try {
      const response: IResponse<IProps[]> = await apiClient.get(
        API_END_POINTS.USER_LEADERBOARD,
      );

      setLeaderBoardData(response.data);
    } catch (error) {
      console.error('Error Certificate Statistics:', error);
    } finally {
      setLoading(false);
    }
  };

  const pauseOtherVideos = (currentIndex: number): void => {
    Object.entries(videoRefs.current).forEach(([key, video]) => {
      if (Number(key) !== currentIndex && video && !video.paused) {
        video.pause();
      }
    });
  };

  const handleVideoPlay = (index: number): void => {
    setActivePlayingIndex(index);
    pauseOtherVideos(index);
  };

  const handleVideoPause = (index: number): void => {
    setActivePlayingIndex(prev => (prev === index ? null : prev));
  };

  const handlePlayPause = (index: number): void => {
    const video = videoRefs.current[index];
    if (!video) return;

    if (video.paused) {
      setActivePlayingIndex(index);
      pauseOtherVideos(index);
      void video.play();
    } else {
      video.pause();
    }
  };

  const handleVideoClick = (index: number): void => {
    handlePlayPause(index);
  };

  const handleVideoEnded = (index: number): void => {
    setActivePlayingIndex(prev => (prev === index ? null : prev));
  };

  return (
    <Border>
      <div className="content-p-4 sm:content-p-6">
        <h2 className="content-border-b content-border-card-border content-pb-2 content-text-lg content-font-semibold content-text-white sm:content-text-xl">
          Leader Message
        </h2>
        {loading ? (
          <LeaderBoardSkeleton count={2} />
        ) : leaderBoardData?.length > 0 ? (
          <div className="content-mt-3 content-grid content-grid-cols-1 content-items-stretch content-gap-2 sm:content-mt-4 sm:content-gap-4 sm:content-grid-cols-2 lg:content-max-h-[300px] lg:content-overflow-y-auto">
            {leaderBoardData?.map((item, index) => (
              <div
                key={index}
                className="content-flex content-h-full content-flex-col content-rounded content-bg-dark"
              >
                <div className="content-mb-auto content-px-2 content-py-2 sm:content-px-3 sm:content-py-3">
                  <p className="content-line-clamp-2 content-text-sm content-text-white content-text-opacity-75">
                    {item.title}
                  </p>
                </div>
                <div
                  className="content-relative content-h-36 content-w-full content-overflow-hidden"
                  style={{ aspectRatio: '16 / 9' }}
                >
                  <video
                    ref={(el: HTMLVideoElement | null) => {
                      videoRefs.current[index] = el;
                    }}
                    className="content-h-36 content-w-full content-cursor-pointer content-object-cover"
                    onClick={() => handleVideoClick(index)}
                    onPlay={() => handleVideoPlay(index)}
                    onPause={() => handleVideoPause(index)}
                    onEnded={() => handleVideoEnded(index)}
                    preload="metadata"
                    poster={FILE_PATH_PREFIX + item.thumbnailUrl}
                    controls
                    controlsList="nodownload"
                  >
                    <source
                      src={FILE_PATH_PREFIX + item.videoUrl}
                      type="video/mp4"
                    />
                    Your browser does not support the video tag.
                  </video>

                  {/* Play/Pause Button Overlay */}
                  {activePlayingIndex !== index && (
                    <div
                      className="content-absolute content-inset-0 content-flex content-cursor-pointer content-items-center content-justify-center content-bg-black content-bg-opacity-30"
                      onClick={() => handlePlayPause(index)}
                      style={{
                        backgroundImage: `url(${FILE_PATH_PREFIX + item.thumbnailUrl})`,
                        backgroundSize: 'cover',
                        backgroundPosition: 'center',
                        backgroundRepeat: 'no-repeat',
                      }}
                    >
                      <div className="shadow-lg content-rounded-full content-bg-white content-bg-opacity-50 content-p-4 content-transition-all content-duration-200 hover:content-bg-opacity-50">
                        <FaPlay size={16} color="#fff" />
                      </div>
                    </div>
                  )}

                  {/* Pause Button (visible when playing and hovering) */}
                  {/* {activePlayingIndex === index && (
                    <div
                      className="content-absolute content-inset-0 content-flex content-cursor-pointer content-items-center content-justify-center content-bg-black content-bg-opacity-20 content-opacity-0 content-transition-opacity content-duration-200 hover:content-opacity-100"
                      onClick={() => handlePlayPause(index)}
                    >
                      <div className="shadow-lg content-rounded-full content-bg-white content-bg-opacity-35 content-p-4 content-transition-all content-duration-200 hover:content-bg-opacity-50">
                        <FaPause size={16} color="#fff" />
                      </div>
                    </div>
                  )} */}
                </div>
                <div className="content-rounded-b content-bg-primary content-px-2 content-py-2 content-text-white sm:content-px-3 sm:content-py-3">
                  <p className="content-line-clamp-1 content-text-sm content-font-semibold">
                    {item.name}
                  </p>
                  <p className="content-mt-1 content-line-clamp-1 content-text-xs content-text-white content-text-opacity-75">
                    {item.designation}
                  </p>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <NoDataText className="content-mt-4" text="No Leader Found" />
        )}
      </div>
    </Border>
  );
};

export default LeaderBoard;
