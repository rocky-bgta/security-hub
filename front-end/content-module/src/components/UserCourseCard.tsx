import clsx from 'clsx';
import { Fragment, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';

import { CardCalendarIcon, ListIcon, SaveIcon } from 'assets/icons';
import { Button } from 'common/Button';
import Border from 'components/UserBorder';
import { useAPI } from 'hooks/UseAPI';
import { useStore } from 'hooks/UseStore';
import { IUserSubPackageTopic } from 'models/Package';
import { toast } from 'react-toastify';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { FILE_PATH_PREFIX } from 'utils/Constants';
import { HumanizeDate } from 'utils/Helper';

interface IProps {
  data: IUserSubPackageTopic;
  reFetchData: () => void;
  path: string;
  packageId?: string;
}

const UserCourseCard = ({ data, reFetchData, path }: IProps) => {
  const { userInfo } = useStore();
  const navigate = useNavigate();
  const [isHovered, setIsHovered] = useState<boolean>(false);
  const [loading, setLoading] = useState<boolean>(false);

  const apiClient = useAPI();

  const handleBookmark = async () => {
    setLoading(true);
    const payload = {
      userId: userInfo.userId,
      topicId: data.id,
      subPackageId: data?.subPackageId,
      bookmarked: !data?.isBookmarked,
    };

    try {
      const response = await apiClient.post(API_END_POINTS.USER_BOOKMARK, {
        data: payload,
      });
      toast.success(response.message);
      reFetchData();
    } catch (error) {
      console.log(error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div
      className="content-group content-relative"
      onMouseEnter={() => setIsHovered(true)}
      onMouseLeave={() => setIsHovered(false)}
    >
      <Border className="content-h-full">
        <div className="content-h-full content-overflow-hidden">
          <div className="content-relative content-m-1 content-flex content-h-full content-flex-col content-justify-between content-overflow-hidden content-rounded-lg">
            <Fragment>
              <img
                src={
                  FILE_PATH_PREFIX + data?.thumbnailUrl ||
                  'https://placehold.co/600x400'
                }
                alt={data?.topicName}
                className="content-h-[192px] content-w-full content-rounded-b-none content-rounded-t-md content-object-cover"
              />
              <div className="content-p-3 content-pt-1">
                <h2 className="content-mb-3 content-line-clamp-2 content-text-lg content-font-semibold content-text-cloudy-white">
                  {data?.topicName || 'N/A'}
                </h2>
                <div className="content-mb-3 content-flex content-items-center content-justify-between">
                  <div className="content-flex content-items-center content-gap-1">
                    <CardCalendarIcon fill="#FFFFFF80" />
                    <span className="content-text-sm content-leading-5 content-text-cloudy-white">
                      {data?.subPackageValidity
                        ? HumanizeDate(data?.subPackageValidity)
                        : 'N/A'}
                    </span>
                  </div>
                  {/* <div className="content-flex content-items-center content-gap-1">
                    <UserGroupsIcon />
                    <span className="content-text-sm content-leading-5 content-text-cloudy-white">
                      32
                    </span>
                  </div> */}
                </div>
                <div>
                  <div className="content-flex content-items-center content-gap-2">
                    <span className="content-text-xs content-text-cloudy-white">
                      {Math.round(data?.topicProgress) || 0}%
                    </span>
                    <div className="content-h-2 content-grow content-rounded-full content-bg-gray-600">
                      <div
                        style={{ width: `${data?.topicProgress}%` }}
                        className="content-h-2 content-rounded-full content-bg-primary"
                      ></div>
                    </div>
                  </div>
                </div>
              </div>
            </Fragment>
            <div
              className={`${clsx(
                'content-absolute content-bottom-2 content-left-0 content-w-full content-transform content-bg-dark-blue content-p-3 content-transition-all content-duration-300 content-ease-in-out',
                isHovered
                  ? 'content-translate-y-0 content-opacity-100'
                  : 'content-pointer-events-none content-translate-y-full content-opacity-0',
              )}`}
            >
              <Link to={path}>
                <h2 className="content-mb-3 content-line-clamp-2 content-text-lg content-font-semibold content-text-cloudy-white content-transition content-duration-300 hover:content-text-primary">
                  {data?.topicName || 'N/A'}
                </h2>
              </Link>
              <div className="content-mb-3 content-flex content-items-center content-justify-between">
                <div className="content-flex content-items-center content-gap-1">
                  <CardCalendarIcon fill="#FFFFFF80" />
                  <span className="content-text-sm content-text-cloudy-white">
                    {data?.subPackageValidity
                      ? HumanizeDate(data?.subPackageValidity)
                      : 'N/A'}
                  </span>
                </div>
                {/* <div className="content-flex content-items-center content-gap-1">
                  <UserGroupsIcon />
                  <span className="content-text-sm content-text-cloudy-white">
                    32
                  </span>
                </div> */}
              </div>
              <div className="content-mb-3 content-flex content-items-center content-gap-1">
                <ListIcon fill="#FFFFFF80" />
                <span className="content-text-sm content-text-cloudy-white">
                  {data?.chapterIds?.length || 0} Chapter,{' '}
                  {data?.totalContentCount || 0} Lesson
                </span>
              </div>
              <div className="content-mb-3 content-flex content-items-center content-gap-1">
                <span className="content-text-xs content-text-cloudy-white">
                  {Math.round(data?.topicProgress) || 0}%
                </span>
                <div className="content-h-2 content-grow content-rounded-full content-bg-gray-600">
                  <div
                    style={{ width: `${Math.round(data?.topicProgress)}%` }}
                    className="content-h-2 content-rounded-full content-bg-primary"
                  ></div>
                </div>
              </div>

              <div className="content-flex content-gap-2">
                <Button
                  onClick={() => {
                    navigate(path);
                  }}
                  variant="outline"
                  className="content-grow"
                >
                  View Details
                </Button>
                <Button
                  variant="outline"
                  title={
                    data?.isBookmarked
                      ? 'Remove from bookmarks'
                      : 'Add to bookmarks'
                  }
                  onClick={handleBookmark}
                  disabled={loading}
                >
                  {loading ? (
                    <div className="content-flex content-items-center content-justify-center">
                      <div className="content-size-5 content-animate-spin content-rounded-full content-border-2 content-border-white content-border-t-transparent"></div>
                    </div>
                  ) : (
                    <SaveIcon fill={data?.isBookmarked ? '#00d4aa' : 'none'} />
                  )}
                </Button>
              </div>
            </div>
          </div>
        </div>
      </Border>
    </div>
  );
};

export default UserCourseCard;
