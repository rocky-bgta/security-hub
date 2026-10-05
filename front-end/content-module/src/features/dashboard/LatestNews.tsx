import NoDataText from 'common/NoDataText';
import Border from 'components/UserBorder';
import LatestNewsSkeleton from 'components/skeleton/dashboard/LatestNews';
import { useAPI } from 'hooks/UseAPI';
import { IList, IResponse } from 'models/Global';
import { Fragment, useEffect, useState } from 'react';
import { FaThumbsDown, FaThumbsUp } from 'react-icons/fa';
import { Link } from 'react-router-dom';
import { toast } from 'react-toastify';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { FILE_PATH_PREFIX } from 'utils/Constants';
import { isSuccessResponse, sliceWords } from 'utils/Helper';

interface ILatestNewsCategory {
  id: string;
  name: string;
  description: string;
  status: string;
}

interface ILatestNews {
  id: string;
  name: string;
  slug: string;
  categoryId: string;
  content: string;
  sequence: number;
  imageUrl: string;
  videoUrl: string;
  publishedDate: string;
  expireDate: string;
  status: string;
  likeCount: number;
  dislikeCount: number;
  category: ILatestNewsCategory;
  userLikeStatus: boolean | null; // true = liked, false = disliked, null = no reaction
  comments: any;
}

const LatestNews = () => {
  const [leaderBoardData, setLeaderBoardData] = useState<IList<ILatestNews>>({
    items: [],
    offset: 0,
    pageSize: 0,
    total: 0,
  });
  const [loading, setLoading] = useState<boolean>(false);
  const apiClient = useAPI();

  useEffect(() => {
    fetchLatestNewsData();
  }, []);

  const fetchLatestNewsData = async () => {
    setLoading(true);

    try {
      const response: IResponse<IList<ILatestNews>> = await apiClient.get(
        API_END_POINTS.USER_NEWS,
      );

      setLeaderBoardData(response.data);
    } catch (error) {
      console.error('Error Latest News:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleReaction = async (itemId: string, isLike: boolean) => {
    // Find the current news item
    const newsItem = leaderBoardData.items.find(item => item.id === itemId);
    if (!newsItem) return;

    const currentStatus = newsItem.userLikeStatus;
    let newLikeStatus: boolean;
    let newLikeCount = newsItem.likeCount;
    let newDislikeCount = newsItem.dislikeCount;

    // Determine new state based on current state and action
    if (isLike) {
      if (currentStatus === true) {
        // User already liked - do nothing (can't undo)
        return;
      } else if (currentStatus === false) {
        // User is changing from dislike to like
        newLikeStatus = true;
        newLikeCount += 1;
        newDislikeCount -= 1;
      } else {
        // User is adding a new like
        newLikeStatus = true;
        newLikeCount += 1;
      }
    } else {
      // Dislike action
      if (currentStatus === false) {
        // User already disliked - do nothing (can't undo)
        return;
      } else if (currentStatus === true) {
        // User is changing from like to dislike
        newLikeStatus = false;
        newDislikeCount += 1;
        newLikeCount -= 1;
      } else {
        // User is adding a new dislike
        newLikeStatus = false;
        newDislikeCount += 1;
      }
    }

    // Optimistically update the UI
    const updatedData = leaderBoardData.items.map(item => {
      if (item.id === itemId) {
        return {
          ...item,
          likeCount: newLikeCount,
          dislikeCount: newDislikeCount,
          userLikeStatus: newLikeStatus,
        };
      }
      return item;
    });

    setLeaderBoardData({
      ...leaderBoardData,
      items: updatedData,
    });

    // Prepare API payload
    const payload = {
      liked: newLikeStatus === null ? null : newLikeStatus,
    };

    try {
      const response = await apiClient.post(
        API_END_POINTS.USER_NEWS_REACTION.replace(':id', itemId),
        {
          data: payload,
        },
      );
      if (isSuccessResponse(response.statusCode)) {
        toast.success('Reaction updated successfully');
      }
    } catch (error) {
      fetchLatestNewsData();
      console.error('Error updating reaction:', error);
      toast.error('Failed to update reaction');
    }
  };

  return (
    <Border>
      <div className="content-p-4 sm:content-p-6">
        <h2 className="content-border-b content-border-card-border content-pb-2 content-text-lg content-font-semibold content-text-white sm:content-text-xl">
          Latest News
        </h2>
        {loading ? (
          <LatestNewsSkeleton count={2} />
        ) : (
          <Fragment>
            {leaderBoardData?.items?.length > 0 &&
            leaderBoardData?.items?.length > 0 ? (
              <Fragment>
                <div className="content-mt-3 content-space-y-3 sm:content-mt-4 sm:content-space-y-4 lg:content-max-h-[300px] lg:content-overflow-y-auto">
                  {leaderBoardData?.items?.map(
                    (item: ILatestNews, index: number) => (
                      <div
                        key={index}
                        className="content-flex content-items-start content-gap-3 sm:content-gap-4"
                      >
                        <div className="content-shrink-0">
                          <img
                            src={FILE_PATH_PREFIX + item.imageUrl}
                            alt="News thumbnail"
                            className="content-size-16 content-rounded content-object-cover sm:content-size-24"
                          />
                        </div>

                        <div className="content-min-w-0 content-flex-1">
                          <div className="content-mb-1 content-inline-block content-text-white content-text-opacity-75">
                            <p>
                              {(() => {
                                const text = sliceWords(item.name, 10);
                                return text.length > 65 ? (
                                  <>{text.slice(0, 65)}...</>
                                ) : (
                                  text
                                );
                              })()}{' '}
                              <Link
                                to={item.slug}
                                target="_blank"
                                className="content-text-sm content-text-green-500 hover:content-text-green-500/80"
                              >
                                Read More
                              </Link>
                            </p>
                          </div>

                          <div className="content-flex content-flex-wrap content-items-center content-gap-2 content-gap-y-2 sm:content-gap-4">
                            {item?.category?.name &&
                              item?.category?.name
                                .split(',')
                                .map((tag: string, index: number) => (
                                  <div
                                    className="content-mt-1 content-text-sm content-text-gray-400"
                                    key={index}
                                  >
                                    <span className="content-rounded content-bg-gray-700 content-px-2 content-py-1 content-text-sm content-text-white">
                                      {tag}
                                    </span>
                                  </div>
                                ))}

                            <div className="content-flex content-items-center content-gap-2 content-text-sm content-text-gray-400 sm:content-gap-4">
                              <div
                                onClick={() => handleReaction(item.id, true)}
                                className={`content-flex content-cursor-pointer content-items-center content-gap-1 ${
                                  item.userLikeStatus === true
                                    ? 'content-text-green-500'
                                    : ''
                                }`}
                              >
                                <FaThumbsUp className="content-size-4" />
                                <span>{item.likeCount}</span>
                              </div>
                              <div
                                onClick={() => handleReaction(item.id, false)}
                                className={`content-flex content-cursor-pointer content-items-center content-gap-1 ${
                                  item.userLikeStatus === false
                                    ? 'content-text-red-500'
                                    : ''
                                }`}
                              >
                                <FaThumbsDown className="content-size-4" />
                                <span>{item.dislikeCount}</span>
                              </div>
                            </div>
                          </div>
                        </div>
                      </div>
                    ),
                  )}
                </div>
              </Fragment>
            ) : (
              <NoDataText className="content-mt-4" text="No News Found" />
            )}
          </Fragment>
        )}
      </div>
    </Border>
  );
};

export default LatestNews;
